package twitterserver.twitterserver;

import org.apache.spark.sql.SparkSession;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import twitterserver.twitterserver.Config;

public class SparkFactory {	
	private SparkSession spark;
	private Dataset<Row> data;
	
	private SparkFactory() {
		spark = SparkSession.builder().appName(Config.appname)
				.master(Config.sparkMaster).getOrCreate();
    	
    	data = spark.read().json(Config.input);
	}
	
	private static class SparkFactorySingleton {
		private static final SparkFactory instance = new SparkFactory();
	}
	
	public static SparkFactory getInstance() {		
		return SparkFactorySingleton.instance;
	}

	public String getWordCount() {
		
		Dataset<Row> hashtag = data.select(functions.explode(data.col("entities.hashtags")));
    	hashtag.createOrReplaceTempView("thash");
    	Dataset<Row> hashtagCount = spark.sql("Select col.text as text, count(col.text) as count from thash group by text order by count desc limit 10");
    	return hashtagCount.toJSON().toJavaRDD().collect().toString();    	
	}
	
	public void stop() {
		spark.stop();
	}
}
