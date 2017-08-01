#!/bin/bash


if [ "$#" -lt 3 ]; then
echo "Usage: ./extract-knowledge.sh gzfile num-reps original_name destdir dest_prefix"
	exit
fi

cwd=$(pwd)

gzfile="$1"
num_reps=$2
original_name=$3
destdir=$4
dest_name=$5

# creates temp. dir to extract files
mkdir /tmp/extracted
cd /tmp/extracted

# extraction
echo "Extracting from $gzfile"
tar xvf "$gzfile" --no-anchored $original_name

# goes to destination directory
cd $cwd
mkdir $destdir
cd $destdir

echo "Moving files to $destdir"
for j in $(seq -f "%02g" 1 $num_reps); do
	# using * because I assume there's only one directory inside extracted
	mv /tmp/extracted/*/rep"$j"/"$original_name" "$dest_name$j.xml"
done

# cleanup
echo "Cleaning up"
rm -rf /tmp/extracted

# gzip
echo "Packing files..."
tar cfz "$destdir.tar.gz" *

cd $cwd

echo "Done. Resulting files are in $destdir. They are also packed in a directory above $destdir"
