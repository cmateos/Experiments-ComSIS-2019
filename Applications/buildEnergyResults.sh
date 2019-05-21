#!/bin/bash

ROOT_OUTPUT_FOLDER=$HOME/PowerMeter/Applications/output
ITERATIONS=10

function buildResults4Version {
	LINE_TIME="avgtime"
	LINE_ENERGY="avgenergy"
	LINE_JOULES="joules"

	for col in $(seq 2 15); do
		AVGTIME=$(tail -n +2 $ROOT_OUTPUT_FOLDER/$1TimeResults.csv | awk -F"," -v COL=$col '{sum+=$COL} END {print sum/NR}')
		AVGENERGY=$(tail -n +2 $ROOT_OUTPUT_FOLDER/$1EnergyResults.csv | head -1 | cut -d"," -f$col)
		LINE_TIME=$LINE_TIME","$AVGTIME
		LINE_ENERGY=$LINE_ENERGY","$AVGENERGY
		LINE_JOULES=$LINE_JOULES","$(echo "$AVGTIME*$AVGENERGY" | bc -l)
	done
	echo "Iteration,NQ_Original,NQ_Refactored,GD_Original,GD_Refactored,SA_Original,SA_Refactored,Bayes_Original,Bayes_Refactored,FFT_Original,FFT_Refactored,Mmult_Original,Mmult_Refactored,KP_Original,KP_Refactored"> $ROOT_OUTPUT_FOLDER/results-$1.csv 
	echo $LINE_TIME >> $ROOT_OUTPUT_FOLDER/results-$1.csv
	echo $LINE_ENERGY >> $ROOT_OUTPUT_FOLDER/results-$1.csv
	echo $LINE_JOULES >> $ROOT_OUTPUT_FOLDER/results-$1.csv
}

buildResults4Version singleThread
buildResults4Version multiThread
