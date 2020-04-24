# **Project Phase 2 - Analyzing Tweets**
## **Principles of Big Data Management \(CS 5540\)**
----------------
>## **Authors**: 
>- ## **Jonathan Wolfe**
>- ## **Rod Skoglund**
>### Date: April 25, 2020
----------------


# **Table Of Contents**
1. [Design](#Design)
2. [Tweet Storage](#Tweet-Storage)
3. [Queries](#Queries)
4. [Visualizations](#Visualizations)
5. [Code](#Code)
6. [Demo and Setup](#Demo-and-Setup)
7. [Work Assignments](#Work-Assignments)
8. [References](#References)
----------------

# Design
### The design involved several steps to develop this analysis.
  1. Installed Cloudera which includes the following:
      * Hadoop
      * Spark
      * Yarn
  2. Developed Java script to pull Tweet information and extract Hashtags and URLS
      * Captured 100K tweets, approximately 10 hours of stream sampling
      * Used Java library, twitter4j-4.0.7, to access Twitter Streaming API
      * Selected tweets over all other messages
      * Excluded retweets and replies
      * Only captured tweets containing Hashtags or URLs
      * Saved the collected tweets into files with each tweet JSON per line
      * Extracted Hashtags and URLs from the collected tweets
      * Saved the Hashtags and URLs into a text file with 10 Hashtags or URLs per line
      * All files were limited to approximately 64MB size by writting a new file if it may exceed the size
  3. Used the Spark Word Counter capability to produce the word count analysis data.
		   * Used Cloudera default configuration on a pseudo-distributed system
		   * Transferred files with extracted Hashtags/URLs into hadoop file system within the VM
		   * Transferred files were saved in a input folder to be used by Spark
		   * Once Spark was finished, the results were extracted from hadoop and transferred out of the VM
  4. Developed and implemented queries to provide data for visualations
  5. Used D3 to create visualations including 
      * A Bubble Chart to compare the different Hashtags associated with the corona virus and how often each Hashtag is used.
      * A Bar Chart to show the top Hashtags in use.
---

# Tweet Storage
### A folder that contains multiple tweet files \(less than 64MB\) in a text format with each line that contains one tweet. Each line is a JSON format for a single tweet, but because it is not in a single named array with commas at the end of each line and it does not have keys associated with each line, the file is not a true JSON formatted file.
---

# Queries
### Here are the queries we used to get and analyze the data:
1. Bubble Chart Data - a hashtagCount query is defined to capture the number of times a a tweet mentiones each Hashtag. We remove all Hashtags that are mentioned in less than 10 tweetss. 
2. Influencers - the Influencers query will capture the users with the most followers \(these are verified followers\). The query is ordered by the number of followers and we limit the data to the top 50 influencers. 
3. Top Hashtags Overall - This query is used to capture the top ten Hashtags based on the number of tweets that reference the Hashtag.
4. Bots Data - made up from multiple queries. This is developed by capturing the users where the "user.statuses_count", "user.created_at" and tweet "created_at" fields all have data (they are not null fields). This data is used to determine the users with more than 50 tweets per day. The data is grouped by users and captures the counts for Tweets per day, Days since started, user name and description
---

# Visualizations
### Our analysis included a Bubble Chart with a sample of 100k corona relatrd tweets. Each bubble is a different corona virus Hashtag with the number of tweets associated with that Hashtag. This only shows Hashtags with more than 10 tweets.
## Corona Virus Hashtag Bubble Chart:
![Bubble Chart](./Screenshots/BubbleChart.png)

### We also included a Bar Chart with the top 10 Hashtags.
## Corona Virus Top 10 Hashtag Bar Chart:
![Bubble Chart](./Screenshots/Top10HashtagsBars.png)

---

# Code
The code is stored and managed via GitHub. It is available at [Wolfe-Skoglund GitHub code](https://github.com/JAWolfe04/CS5540-Big-Data-Project.git)
---

# Demo and Setup
### The Demo will be shown to the instructor and TA's at a convenient date/time.

### Here are the instructions for setting up and displaying the data analysis.
1. # \<Need to define these instructions\>
2.
---

# Work Assignments

- Installations & Setup: 
  *	Wolfe
  * Skoglund
- Coding:
  * Wolfe \(99%\)
  * Skoglund \(1%\)
- Phase #2 documentation:
  *	Wolfe
  * Skoglund
 
---

# References

1. [GitHub REST API | Get remote repo files list & download file content programmatically without cloning in local](https://itsallbinary.com/github-rest-api-get-remote-github-repo-files-list-download-file-content-programmatically-without-cloning-in-local/)
2. [Building Real-time interactions with Spark](https://spoddutur.github.io/spark-notes/build-real-time-interations-with-spark.html)
3. [How to send HTTP request GET/POST in Java](https://mkyong.com/java/how-to-send-http-request-getpost-in-java/)
4. [Embedding Jetty](https://www.eclipse.org/jetty/documentation/current/embedding-jetty.html)
5. [Twitter search API- Get tweets and tweets count of hashtag using JAVA twitter client Twitter4j](http://jkoder.com/twitter-search-api-get-tweets-and-tweets-count-hashtag-java-client-twitter4j/)
6. [Docker and Java Application examples](https://mkyong.com/docker/docker-and-java-application-examples/)
7. [#BotSpot: Twelve Ways to Spot a Bot](https://medium.com/dfrlab/botspot-twelve-ways-to-spot-a-bot-aedc7d9c110c)
8. [D3-cloud Github](https://github.com/jasondavies/d3-cloud)
9. [Create a simple Donut Chart using D3.js](http://www.adeveloperdiary.com/d3-js/create-a-simple-donut-chart-using-d3-js/)
