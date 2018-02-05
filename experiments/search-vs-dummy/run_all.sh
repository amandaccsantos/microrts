#!/bin/bash

# consider that the script is running in the project root dir ;)

for i in experiments/search-vs-dummy/*.xml; do 
	filename=$(basename "$i")
	extension="${filename##*.}"
	basename="${filename%.*}"
	echo $basename
	./experimentmanager.py -n 5 -s 1 -c "$i" -o results/search-vs-dummy/"$basename" &2>1
done;

