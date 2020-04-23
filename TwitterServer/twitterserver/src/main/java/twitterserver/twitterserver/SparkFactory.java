package twitterserver.twitterserver;

import org.apache.spark.sql.SparkSession;

import java.io.File;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static org.apache.spark.sql.functions.*;

import twitterserver.twitterserver.Config;

public class SparkFactory {	
	private SparkSession spark;
	private Dataset<Row> data;
	Dataset<Row> hashtag;
	
	private SparkFactory() {		
		spark = SparkSession.builder().appName(Config.appname)
				.master(Config.sparkMaster).getOrCreate();
		Logger.getRootLogger().setLevel(Level.ERROR);
		
		File tmpDir = new File(Config.path);
		if(!tmpDir.exists()) {
			System.out.print(Config.path + " does not exist");
			System.exit(1);
		}
		
    	data = spark.read().json(Config.path);
    	hashtag = data.select(explode(data.col("entities.hashtags")));
	}

	public String getBubbleChartData() {
		
    	hashtag.createOrReplaceTempView("thash");
    	Dataset<Row> hashtagCount = spark.sql("Select col.text as text, count(col.text) as count from thash group by text");
    	hashtagCount = hashtagCount.filter("count > 10");
    	String counts = hashtagCount.toJSON().toJavaRDD().collect().toString();
    	
    	JsonObject jsonObject = new JsonObject();
    	JsonElement jsonElement =  JsonParser.parseString(counts);
    	jsonObject.add("children", jsonElement);
    	return jsonObject.toString();
	}
	
	public String getBotsData() {
		Dataset<Row> users = data.filter("user.statuses_count is not null")
				.filter("user.created_at is not null").filter("created_at is not null");
		Dataset<Row> adjUsers = users.withColumn("created_at", unix_timestamp(users.col("created_at"), "EEE MMM dd HH:mm:ss ZZZZZ yyyy").cast("timestamp"));
		adjUsers = adjUsers.withColumn("user_created_at", unix_timestamp(adjUsers.col("user.created_at"), "EEE MMM dd HH:mm:ss ZZZZZ yyyy").cast("timestamp"));
		adjUsers = adjUsers.withColumn("Days_since_started", datediff(adjUsers.col("created_at"), adjUsers.col("user_created_at")));
		adjUsers = adjUsers.withColumn("Tweets_per_day", adjUsers.col("user.statuses_count").divide(adjUsers.col("Days_since_started")));
		Dataset<Row> botsTweets = adjUsers.filter("Tweets_per_day > 50")
		.select("Tweets_per_day", "Days_since_started", "user.name", "user.description");
		Dataset<Row> botData = botsTweets.groupBy("name").agg(avg("Tweets_per_day").alias("Tweets_per_day"))
			.orderBy(desc("Tweets_per_day")).limit(50);
		String botCounts = botData.toJSON().toJavaRDD().collect().toString();
		
		long botCount = botsTweets.count();
		long tweetCount = users.count();
		int botPerc = Math.round(((float)botCount / (float)tweetCount) * 100);
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("BotCount", botCount);
		jsonObject.addProperty("UserCount", tweetCount);
		jsonObject.addProperty("BotPerc", botPerc);		
		JsonElement jsonElement =  JsonParser.parseString(botCounts);
    	jsonObject.add("TopBots", jsonElement);
		
		return jsonObject.toString();
	}
	
	public String getInfluencers() {		
		Dataset<Row> users = data.filter("user.followers_count is not null")
				.filter("user.verified is not null").filter("user.name is not null");
		
		Dataset<Row> influencers = users.select("user.name", "user.followers_count", "user.verified")
		.groupBy("name")
		.agg(max("followers_count").alias("followers_count"), max("verified").alias("verified"))
		.orderBy(desc("followers_count")).limit(50);
		String influencerStr = influencers.toJSON().toJavaRDD().collect().toString();
		
		JsonObject jsonObject = new JsonObject();
		JsonElement jsonElement =  JsonParser.parseString(influencerStr);
    	jsonObject.add("Influencers", jsonElement);
		return jsonObject.toString();
	}
	
	public String getTopHashtagsOverall() {
    	hashtag.createOrReplaceTempView("thash");
    	Dataset<Row> hashtagCount = spark.sql("Select col.text as text, count(col.text) as count from thash group by text order by count desc limit 10");
    	String hashtags =  hashtagCount.toJSON().toJavaRDD().collect().toString();
    	
    	JsonObject jsonObject = new JsonObject();
		JsonElement jsonElement =  JsonParser.parseString(hashtags);
    	jsonObject.add("Hashtags", jsonElement);
		return jsonObject.toString();
	}
	
	public void stop() {
		spark.stop();
	}
	
	private static class SparkFactorySingleton {
		private static final SparkFactory instance = new SparkFactory();
	}
	
	public static SparkFactory getInstance() {		
		return SparkFactorySingleton.instance;
	}
}
