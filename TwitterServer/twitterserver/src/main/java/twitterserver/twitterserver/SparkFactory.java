package twitterserver.twitterserver;

import org.apache.spark.sql.SparkSession;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
	long totalRetweets;
	long totalReplies;
	long totalTweets;
	long totalStatuses;
	
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
    	
    	totalRetweets = data.filter("retweeted_status is not null").count();
		totalReplies = data.filter("in_reply_to_status_id is not null").count();
		totalTweets = data.filter("retweeted_status is null and in_reply_to_status_id is null").count();
		totalStatuses = data.count(); 
		
		this.getBotsData();
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
	
	public class BotsClass {
		String name;
		int percentage;
		public BotsClass(String string, int botPct) {
			name = string;
			percentage = botPct;
		}
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
		
		long botCount = botsTweets.count();
		
		long retweets = botsTweets.filter("retweeted_status is not null").count();
		long replies = botsTweets.filter("in_reply_to_status_id is not null").count();

		JsonObject jsonObject = new JsonObject();
		
		int botPct = Math.round((botCount / (float)totalStatuses) * 100);
		List<BotsClass> totals = new ArrayList<BotsClass>();
		totals.add(new BotsClass("Bots", botPct));
		totals.add(new BotsClass("User", 100 - botPct));
		
		int retweetPct = Math.round((retweets / (float)botCount) * 100);
		int repliesPct = Math.round((replies / (float)botCount) * 100);
		List<BotsClass> freq = new ArrayList<BotsClass>();
		freq.add(new BotsClass("Retweets", retweetPct));
		freq.add(new BotsClass("Replies", repliesPct));
		freq.add(new BotsClass("Tweets", 100 - repliesPct - retweetPct));
		
		Gson gson = new GsonBuilder().create();
		jsonObject.add("Total", gson.toJsonTree(totals));
		jsonObject.add("Frequency", gson.toJsonTree(freq));
		return jsonObject.toString();
	}
	
	public String getInfluencers() {		
		Dataset<Row> users = data.filter("user.followers_count is not null")
				.filter("user.verified is not null").filter("user.name is not null");
		
		Dataset<Row> influencers = users.select("user.name", "user.followers_count", "user.verified")
		.groupBy("name").agg(max("followers_count").alias("followers_count"))
		.orderBy(desc("followers_count")).limit(50);
		String influencerStr = influencers.toJSON().toJavaRDD().collect().toString();
		
		JsonObject jsonObject = new JsonObject();
		JsonElement jsonElement =  JsonParser.parseString(influencerStr);
    	jsonObject.add("Influencers", jsonElement);
		return jsonObject.toString();
	}
	
	public String getGeoData() {
		Dataset<Row> coordsData = data.filter("geo.coordinates is not null")
				.selectExpr("id", "coordinates.coordinates");
		Dataset<Row> placeData = data.filter("place.bounding_box.coordinates is not null and geo.coordinates is null")
				.selectExpr("id", "place.bounding_box.coordinates[0][0] as coordinates");
		coordsData = coordsData.union(placeData);
		Dataset<Row> longData = coordsData.selectExpr("id","coordinates[0] as longitude");
		Dataset<Row> latData = coordsData.selectExpr("id","coordinates[1] as latitude");
		coordsData = coordsData.join(longData, coordsData.col("id").equalTo(longData.col("id"))).drop(longData.col("id"));
		coordsData = coordsData.join(latData, coordsData.col("id").equalTo(latData.col("id"))).drop(latData.col("id"));
		coordsData = coordsData.withColumn("geoJson", 
				concat(lit("{\"type\": \"Feature\",\"geometry\": {\"type\": \"Point\",\"coordinates\": ["), 
						coordsData.col("latitude"),
						lit(','),
						coordsData.col("longitude"),
						lit("]},\"properties\":{}}")));
		String geoJson = coordsData.select("geoJson").javaRDD().map(r -> r.get(0)).collect().toString();
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "FeatureCollection");
		JsonElement jsonElement =  JsonParser.parseString(geoJson);
    	jsonObject.add("features", jsonElement);
		return jsonObject.toString();
	}
	
	public String getNewsUsers() {
		Dataset<Row> filteredData = data.filter("user.description is not null");
		Dataset<Row> selData = filteredData.select(filteredData.col("user.name"), lower(filteredData.col("user.description")).alias("description"));
		Dataset<Row> newsOrgs = selData.filter("description like '% news %' or description like 'news %' or description like '% news' or description like '% news%'");
		
		long retweets = newsOrgs.filter("retweeted_status is not null").count();
		long replies = newsOrgs.filter("in_reply_to_status_id is not null").count();
		long tweets = newsOrgs.filter("retweeted_status is null and in_reply_to_status_id is null").count();

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("NewsRetweets", retweets);
		jsonObject.addProperty("NewsReplies", replies);
		jsonObject.addProperty("NewsTweets", tweets);
		jsonObject.addProperty("TotalRetweets", totalRetweets);
		jsonObject.addProperty("TotalReplies", totalReplies);
		jsonObject.addProperty("TotalTweets", totalTweets);
		return jsonObject.toString();
	}
	
	public String getTweetFreq() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("Retweets", totalRetweets);
		jsonObject.addProperty("Replies", totalReplies);
		jsonObject.addProperty("Tweets", totalTweets);
		return jsonObject.toString();
	}
	
	public String getCountryData() {
		Dataset<Row> countryData = data.filter("place.country_code is not null").select("place.country_code");
		Dataset<Row> countryCounts = countryData.groupBy("country_code").agg(count(lit(1)).alias("count"));
		String counts =  countryCounts.toJSON().toJavaRDD().collect().toString();
		
		JsonObject jsonObject = new JsonObject();
		JsonElement jsonElement =  JsonParser.parseString(counts);
    	jsonObject.add("CountryCounts", jsonElement);
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
