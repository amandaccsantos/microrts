#!/bin/bash

echo "Usage: ./create-policytest.sh directory abstraction-model reward-function"

for i in {Heavy,Light,Ranged,Worker}; do
	current="$1/policy-vs-$i.xml"
	echo "<experiment>" >> $current
	
	echo "<parameters>" >> $current
	echo "	<episodes value='60000' />" >> $current
	echo "	<game-duration value='3000' /> " >> $current
	echo "	<abstraction-model value='$2' />" >> $current
	echo "	<output-dir value='results' />" >> $current
	echo "	<reward-function value='$3' />" >> $current
	echo "	<quiet-learning value='true' />" >> $current
	echo "</parameters>" >> $current

	echo "<player name='learner' type='SGQLearningAdapter'>" >> $current
	echo "	<discount value='0' />" >> $current
	echo "	<learning-rate type='constant' value='0' />" >> $current
	echo "	<initial-q value='0' />" >> $current
	echo "	<epsilon value='0' />" >> $current
	echo "</player>

	echo "<player name='dummy' type='Dummy'>" >> $current
	echo "	<dummy-policy value='$iRush' />" >> $current
	echo "</player>" >> $current
	
	echo "</experiment>" >> $current
