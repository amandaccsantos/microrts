#!/bin/bash

for i *.xml; do 
	filename=$(basename "$i")
	extension="${filename##*.}"
	basename="${filename%.*}"
	for j in {01..05}; do
		echo $i
		python/count_victories.py results/search-vs-dummy/"$basename"/rep"$j"
		tar cfz results/seach-vs-dummy/"$basename".tar.gz results/seach-vs-dummy/"$basename"
	done
done

