package TweetCompiler;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class TweetFile {
	private static int fileCount = 1;
	private static File tweetFile = new File("TweetFile" + fileCount + ".txt");
	
	TweetFile() { createFile(); }
	
	private static void createFile() {
		try {
			if (tweetFile.createNewFile()) {
		        System.out.println("File created: " + tweetFile.getName());
		      } 
			else {
		        System.out.println("File already exists.");
		      }
			} 
		catch (IOException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
	}
	
	public static void saveTweets(String tweets) {
		try {
		      FileWriter fileWriter = new FileWriter(tweetFile, true);
		      BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
		      bufferWriter.write(tweets);
		      bufferWriter.close();
		      if(tweetFile.length() >= 59392000) {
		    	  ++fileCount;
		    	  tweetFile = new File("TweetFile" + fileCount + ".txt");
		    	  createFile();
		      }
		    } 
		catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }
	}
}