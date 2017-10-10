#!/bin/bash

echo "Running script for Lemmatization"
mainInputDir=$1
hadoopInputDir=$2
lemmaCsvPath=$3
outputDirPath=$4
echo $outputFolderName
for (( fileCount=2; fileCount <= 20; fileCount = fileCount + 5 ))
do
	echo "For $fileCount files"
	rm $hadoopInputDir/*
	cd $mainInputDir
	for file in $(ls -p | grep -v / | tail -$fileCount)
	do
		mv $file $hadoopInputDir
	done
	cd $outputDirPath
	hdfs dfs -put $hadoopInputDir LemmaInput$fileCount
	START=$(date +%s)
	hadoop jar LemmetizationCoOccurence.jar LemmaInput$fileCount Lemma2GramOutput$fileCount $lemmaCsvPath
	END=$(date +%s)
	DIFF=$(( $END - $START ))
	echo "2 $fileCount $DIFF" >> Output.txt
	hdfs dfs -get Lemma2GramOutput$fileCount $outputDirPath
	START=$(date +%s)
	hadoop jar Lemmetization3Gram.jar LemmaInput$fileCount Lemma3GramOutput$fileCount $lemmaCsvPath
	END=$(date +%s)
	DIFF=$(( $END - $START ))
	echo "3 $fileCount $DIFF" >> Output.txt
	hdfs dfs -get Lemma3GramOutput$fileCount $outputDirPath
done