# Activity 1 Word Count:
*	Used R to extract tweets for input of word count file [DIC Lab-4 Part-1.ipynb]
*	Tweets were pass to the virtual box through a shared folder and then loaded to the Hadoop file system using the “put” command.
*	TweetWordCount.jar file is used to get the Word Count output.
*	The output which is of format: [Word Count] is used as input for word cloud. [DIC Lab-4 Part-1.ipynb]

# Commands:
*	hdfs dfs -put Tweets InputTweets
*	hadoop jar TweetWordCount.jar InputTweets WordCountOutput
*	hdfs dfs -get WordCountOutput
