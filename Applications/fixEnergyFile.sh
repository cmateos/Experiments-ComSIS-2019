#!/bin/bash

# $1 folder
# $2 file name

# This is to fix measurement errors from the Power Meter device itself
cp $1/$2 /tmp/tempfile-energy.txt
rm $1/$2
echo "VEF,IEF,Pact,Pap,FP,KWh" > $1/$2 
for line in $(tail -n +2 /tmp/tempfile-energy.txt); do
	measure=$(echo $line | cut -d, -f3)
	if [ $measure -lt 1000 ]; then
		echo $line >> $1/$2
	fi 
done
