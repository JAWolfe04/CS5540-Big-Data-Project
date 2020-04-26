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
    	data = data.withColumn("created_at", unix_timestamp(data.col("created_at"), "EEE MMM dd HH:mm:ss ZZZZZ yyyy").cast("timestamp"));
    	data = data.withColumn("Hourly_Time", date_format(data.col("created_at"), "MM-dd-HH"));
    	hashtag = data.select(explode(data.col("entities.hashtags")), data.col("Hourly_Time"));
    	
    	totalRetweets = data.filter("retweeted_status is not null").count();
		totalReplies = data.filter("in_reply_to_status_id is not null").count();
		totalTweets = data.filter("retweeted_status is null and in_reply_to_status_id is null").count();
		totalStatuses = data.count(); 
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
		Dataset<Row> users = data.filter("user.statuses_count is not null and user.created_at is not null");
		Dataset<Row> adjUsers = users.withColumn("user_created_at", 
				unix_timestamp(users.col("user.created_at"), 
				"EEE MMM dd HH:mm:ss ZZZZZ yyyy").cast("timestamp"));
		adjUsers = adjUsers.withColumn("Days_since_started", 
				datediff(adjUsers.col("created_at"), 
				adjUsers.col("user_created_at")));
		adjUsers = adjUsers.withColumn("Tweets_per_day", 
				adjUsers.col("user.statuses_count")
				.divide(adjUsers.col("Days_since_started")));
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
	
	public class MostRetweetClass {
		String Time;
		long Retweet_Count;
		long Followers_Count;
		long Listed_Count;
		public MostRetweetClass(String time, long retweets, long followers, long lists) {
			this.Time = time;
			this.Retweet_Count = retweets;
			this.Followers_Count = followers;
			this.Listed_Count = lists;
		}
	}

	public String getMostRetweeted() {
		Dataset<Row> filtDates = data.filter("Hourly_Time is not null and retweet_count is not null and "
				+ "user.listed_count is not null and user.followers_count is not null");
		List<Row> dateRange = filtDates.groupBy("Hourly_Time").agg(count(lit(1)).alias("Count"))
								.filter("Count > 1000").agg(min("Hourly_Time"), max("Hourly_Time")).collectAsList();
		
		List<MostRetweetClass> data = new ArrayList<MostRetweetClass>();
		String startTime = dateRange.get(0).get(0).toString();
		String endTime = dateRange.get(0).get(1).toString();
		int day = Integer.parseInt(startTime.subSequence(3, 5).toString());
		int hour = Integer.parseInt(startTime.subSequence(6, 8).toString());
		int maxTime = Integer.parseInt(endTime.subSequence(3, 5).toString() + endTime.subSequence(6, 8).toString());
		while(maxTime >= (day * 100) + hour)
		{
			String time = "03-" + String.valueOf(day) + "-"  + (hour < 10 ? "0" : "") + String.valueOf(hour);
			Dataset<Row> timedSet = filtDates.filter(filtDates.col("Hourly_Time").equalTo(lit(time)))
							.select("Hourly_Time", "retweet_count", "user.followers_count", "user.listed_count");
			long maxRetweet = (long)timedSet.groupBy().max("retweet_count").collectAsList().get(0).get(0);
			Dataset<Row> maxRetweetDF = timedSet.filter(timedSet.col("retweet_count").equalTo(lit(maxRetweet)));
			long maxFollow =  (long)maxRetweetDF.groupBy().max("followers_count").collectAsList().get(0).get(0);
			Dataset<Row> maxFollowDF = maxRetweetDF.filter(maxRetweetDF.col("followers_count").equalTo(lit(maxFollow)));
			long maxListed = (long)maxFollowDF.groupBy().max("listed_count").collectAsList().get(0).get(0);			
			data.add(new MostRetweetClass(time, maxRetweet, maxFollow, maxListed));
			
			++hour;
			if(hour == 24) {
				hour = 0;
				++day;
			}
		}
		
		Gson gson = new GsonBuilder().create();
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("TimePoints", gson.toJsonTree(data));
		return jsonObject.toString();
	}
	
	public class TopHashTimeClass {
		String Time;
		String Hashtag;
		long Count;
		public TopHashTimeClass(String time, String hashtag, long count) {
			this.Time = time;
			this.Hashtag = hashtag;
			this.Count = count;
		}
	}
	public String getTopHashTime() {
		Dataset<Row> filtDates = hashtag.filter("Hourly_Time is not null");		
		List<Row> dateRange = filtDates.groupBy("Hourly_Time").agg(count(lit(1)).alias("Count"))
					.filter("Count > 1000").agg(min("Hourly_Time"), max("Hourly_Time")).collectAsList();
		
		List<TopHashTimeClass> data = new ArrayList<TopHashTimeClass>();
		String startTime = dateRange.get(0).get(0).toString();
		String endTime = dateRange.get(0).get(1).toString();
		int day = Integer.parseInt(startTime.subSequence(3, 5).toString());
		int hour = Integer.parseInt(startTime.subSequence(6, 8).toString());
		int maxTime = Integer.parseInt(endTime.subSequence(3, 5).toString() + endTime.subSequence(6, 8).toString());
		
		while(maxTime >= (day * 100) + hour) {
			String time = "03-" + String.valueOf(day) + "-"  + (hour < 10 ? "0" : "") + String.valueOf(hour);
			Dataset<Row> timedSet = filtDates.filter(filtDates.col("Hourly_Time").equalTo(lit(time)));
			List<Row> top10 = timedSet.groupBy("col.text").agg(count(lit(1)).alias("count"))
								.orderBy(desc("count")).takeAsList(10);
			top10.forEach(r -> data.add(new TopHashTimeClass(time, r.get(0).toString(), (long)r.get(1))));
			
			++hour;
			if(hour == 24) {
				hour = 0;
				++day;
			}
		}
		

		Gson gson = new GsonBuilder().create();
		JsonObject jsonObject = new JsonObject();
		jsonObject.add("TimePoints", gson.toJsonTree(data));
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
