#!/bin/bash

print "This script has an error in which it copies policies from"
print "all experiments in /tmp/extracted to a destination directory."
print "It works, but you'll end up with extra files and directories."

cwd=$(pwd)

mkdir /tmp/extracted/
cd /tmp/extracted/

# extraction
for i in $1/*.tar.gz; do
	echo "Extracting from $i"
	tar xvf $i --no-anchored q_learner_final.txt
done

# renaming
for i in ./*; do
	mkdir "$1/$i"
	echo "Renaming inside $i"
	for j in $(seq -f "%02g" 1 $2); do
		mv "$i/rep$j/q_learner_final.txt" "$1/$i/q_final_rep$j.xml"
	done
done

cd $cwd

echo "Done. Resulting files are in directories inside $1"
