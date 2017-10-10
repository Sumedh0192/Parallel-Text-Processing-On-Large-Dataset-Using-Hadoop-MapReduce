# Activity 2 Pair-Stripe:

Pairs Implementation:
*	Input Tweets fetched for Activity 1 have been used for this activity.
*	Input Tweets were loaded to the Hadoop File System using “put”
*	TweetPairsWordCount.jar file is used to get the count of Pairs from the tweets using the Hadoop Pairs approach.
*	Output format is: [Word NeighborWord Count]
Commands:
*	hdfs dfs -put Tweets InputTweets
*	hadoop jar TweetPairsWordCount.jar InputTweets PairsOutput
*	hdfs dfs -get PairsOutput

Stripes Implementation: [Similar step 1&2]
*	TweetStripeWordCount.jar file is used to get the count of Pairs from the tweets using the Hadoop Stripes approach.
*	Output format is: [Word NeighborWord Count]
Commands:
*	hdfs dfs -put Tweets InputTweets
*	hadoop jar TweetStripeWordCount.jar InputTweets StripeOutput
*	hdfs dfs -get StripeOutput


