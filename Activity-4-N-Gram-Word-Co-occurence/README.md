/* Unzip the zip files to the same directory */
# Activity 4A:
* 	New_Lemmetization.csv file is used to identify the lemmas for each word in the .tess files and is its path is passed to the jar file as an argument through command line.
* 	Brief Code Logic:
	1.	A global lemma map is created in main function and used throughout the execution.
	2.	A Inmap combiner is used for better processing of Mapper output.
	3.	For every word in the .tess file get all the neighbors i.e. words on same line and get the list of lemmas for both. [Normalized word is considered if no lemmas available]
	4.	Iterate over the lists of lemmas and create their pairs.
	5.	Link the location to the pair of lemmas.
	6.	A Location string is constructed in Inmap Combiner and Reducer by appending the new location identified.
	7.	Associative Array flushing logic is also added to avoid hitting the heap size of Hadoop Java execution.
*	LemmetizationCoOccurence.jar is used to identify the location of the co-occurring pairs from the .tess files and get its count. 
*	Output format: pair_of_normalized_word/lemma_of_word {<location1>,<location2>…} {count:#PairOccurences}

# Activity 4B:
*	The process and programming logic remains same, the only change is that 3 neighboring words are considered instead of 2.
*	Lemmetization3Gram.jar is used to identify the location of the co-occurring triples from the .tess files and get its count. 
*	Output format: triple_of_normalized_word/lemma_of_word {<location1>,<location2>…} {count:#PairOccurences}

# Shell Script [Lab4Activity4Script]:
*	To process on multiple number of files I have created a shell script which takes following parameters:
	1.	Location of all the Files
	2.	Location of directory which will be used to put the input to the Hadoop File System
	3.	Lemmetization csv file path
	4.	Output directory path
*	The Shell Script iterates on the multiple files incrementing from 2 to 20 files.
*	The Shell Script writes the time to an output file for execution of both 2-gram and 3-gram co-occurrence.

# Commands: 
*	chmod -x Lab4Activity4Script.sh
*	bash Lab4Activity4Script.sh LatinFiles/ LatinInputFiles/ new_lemmatizer.csv Output/

# Plotting:
*	The output of the Shell Script”Output.txt” is used to plot a line graph [number of files against time taken for execution]

# Results:
*	As the number of files increase the processing time for both 2 and 3 gram co-occurrence increase gradually.
*	The time is highly dependent on the size of files.
*	If one file is large it affects the overall execution of the mapreduce program as Hadoop initiates only one mapper for one file.
*	The difference between the execution times of 2 gram and 3 gram co-occurrence is huge. With latter taking large amount of time to execute.
*	Thus with just one increment in n the execution time increases exponentially.
