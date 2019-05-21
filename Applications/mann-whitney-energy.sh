#!/bin/bash

OUTPUT_ROOT_PATH=$HOME/PowerMeter/Applications/output

for app in FFT Mmult KP NQ SA GD Bayes; do
	sampleA_S=$OUTPUT_ROOT_PATH/$app/$app-original-singleThread.txt
	sampleB_S=$OUTPUT_ROOT_PATH/$app/$app-refactored-singleThread.txt
	if [ -f $sampleA_S -a -f $sampleB_S ]; then
		tail -n +2 $sampleA_S | cut -d"," -f3 > /tmp/sampleA.txt
		tail -n +2 $sampleB_S | cut -d"," -f3 > /tmp/sampleB.txt
 		testS=$(python mann-whitney-energy.py /tmp/sampleA.txt /tmp/sampleB.txt | tr -d " ")
		echo $app,single-thread,$testS

	else
		echo $app,single-thread,undecided
	fi

	sampleA_M=$OUTPUT_ROOT_PATH/$app/$app-original-multiThread.txt
	sampleB_M=$OUTPUT_ROOT_PATH/$app/$app-refactored-multiThread.txt
	if [ -f $sampleA_M -a -f $sampleB_M ]; then
		tail -n +2 $sampleA_M | cut -d"," -f3 > /tmp/sampleA.txt
		tail -n +2 $sampleB_M | cut -d"," -f3 > /tmp/sampleB.txt
 		testM=$(python mann-whitney-energy.py /tmp/sampleA.txt /tmp/sampleB.txt | tr -d " ")
		echo $app,multi-thread,$testM
	else
		echo $sampleA_M -a -f $sampleB_M >> /tmp/tmp.txt
		echo $app,multi-thread,undecided
	fi
done
