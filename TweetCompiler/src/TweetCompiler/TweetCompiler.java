package TweetCompiler;

import twitter4j.RawStreamListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import TweetCompiler.TweetFile;

public class TweetCompiler {
	private static StringBuilder tweets = new StringBuilder("");
	private static int tweetCount = 0;
	private static int totalTweets = 0;

	public static void main(String[] args) throws TwitterException {		
		TwitterStream twitterStream = new TwitterStreamFactory().getInstance().addListener(new RawStreamListener() {
            @Override
            public void onMessage(String rawJSON) {
            	if(rawJSON.startsWith("{\"created_at\"")) {
	            	tweets.append(rawJSON + "\n");
	            	++tweetCount;
	            	if(tweetCount == 1000)
	            	{
	            		TweetFile.saveTweets(tweets.toString());
	            		tweets.setLength(0);
	            		tweetCount = 0;
	            		totalTweets += 1000;
	            		System.out.println(totalTweets + " Tweets");
	            		if(totalTweets >= 100000) {
	            			System.out.println("100K tweets collected successfully.");
	            			System.exit(0);
	            		}
	            	}
            	}
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        }).sample();
	}

}
