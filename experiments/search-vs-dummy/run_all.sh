#!/bin/bash

for i *.xml; do 
	filename=$(basename "$i")
	extension="${filename##*.}"
	basename="${filename%.*}"
	./experimentmanager.py -n 5 -s 1 -c $i -o results/search-vs-dummy/"$basename" &2>1
done;

