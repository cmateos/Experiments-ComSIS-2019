#!/bin/bash

ROOT_OUTPUT_FOLDER=$HOME/PowerMeter/Applications/output
ITERATIONS=10

# $1 iteration
# $2 mode (singleThread, multiThread)
function getPerformanceForIterationAndMode {
	LINE="$1"
	for app in NQ GD SA Bayes FFT Mmult KP; do
		ORIGINAL_TIME=0
		REFACTORED_TIME=0
		if [ -f $ROOT_OUTPUT_FOLDER/$app/times/original-$2/original_$1 ]; then
			ORIGINAL_TIME=$(cat $ROOT_OUTPUT_FOLDER/$app/times/original-$2/original_$1 | grep Segundos | cut -d':' -f2)
		fi
		if [ -f $ROOT_OUTPUT_FOLDER/$app/times/refactored-$2/refactored_$1 ]; then
			REFACTORED_TIME=$(cat $ROOT_OUTPUT_FOLDER/$app/times/refactored-$2/refactored_$1 | grep Segundos | cut -d':' -f2)
		fi
		LINE=$LINE","$ORIGINAL_TIME","$REFACTORED_TIME
	done
	echo $LINE
}

# $1 measurement file (active power column is considered)
# VEF,IEF,Pact,Pap,FP,KWh
function average {
	if [ -f $1 ]; then
		AVERAGE=$(tail -n +2 $1 | awk -F"," '{sum+=$3} END {print sum/NR}')
		echo $AVERAGE
	else
		echo "0"
	fi
}

# $1 measurement file (active power column is considered)
# VEF,IEF,Pact,Pap,FP,KWh
function stdev {
	if [ -f $1 ]; then
		STDEV=$(tail -n +2 $1 | cut -d"," -f3 | awk '{x[NR]=$0; s+=$0; n++} END{a=s/n; for (i in x){ss += (x[i]-a)^2} sd = sqrt(ss/n); print sd}')
		echo $STDEV
	else
		echo "0"
	fi
}

# Times
echo "Iteration,NQ_Original,NQ_Refactored,GD_Original,GD_Refactored,SA_Original,SA_Refactored,Bayes_Original,Bayes_Refactored,FFT_Original,FFT_Refactored,Mmult_Original,Mmult_Refactored,KP_Original,KP_Refactored" > $ROOT_OUTPUT_FOLDER/singleThreadTimeResults.csv
echo "Iteration,NQ_Original,NQ_Refactored,GD_Original,GD_Refactored,SA_Original,SA_Refactored,Bayes_Original,Bayes_Refactored,FFT_Original,FFT_Refactored,Mmult_Original,Mmult_Refactored,KP_Original,KP_Refactored" > $ROOT_OUTPUT_FOLDER/multiThreadTimeResults.csv
let ITERATIONS=ITERATIONS-1
for iteration in $(seq 0 $ITERATIONS); do
	LINE_SINGLET=$(getPerformanceForIterationAndMode $iteration singleThread)
	LINE_MULTIT=$(getPerformanceForIterationAndMode $iteration multiThread)
	echo $LINE_SINGLET >> $ROOT_OUTPUT_FOLDER/singleThreadTimeResults.csv
	echo $LINE_MULTIT >> $ROOT_OUTPUT_FOLDER/multiThreadTimeResults.csv
done

# Energy
printf "Metric\nAverage\nStdev" > $ROOT_OUTPUT_FOLDER/singleThreadEnergyResults.csv
printf "Metric\nAverage\nStdev" > $ROOT_OUTPUT_FOLDER/multiThreadEnergyResults.csv

#echo "NQ,NQ_Refactored,GD_Original,GD_Refactored,SA_Original,SA_Refactored,Bayes_Original,Bayes_Refactored" > $ROOT_OUTPUT_FOLDER/singleThreadEnergyResults.csv
#echo "NQ,NQ_Refactored,GD_Original,GD_Refactored,SA_Original,SA_Refactored,Bayes_Original,Bayes_Refactored" > $ROOT_OUTPUT_FOLDER/multiThreadEnergyResults.csv
for app in NQ GD SA Bayes FFT Mmult KP; do
	# Single thread
	echo "Processing: "$ROOT_OUTPUT_FOLDER/$app/$app-original-singleThread.txt
	AVERAGE_O_S=$(average $ROOT_OUTPUT_FOLDER/$app/$app-original-singleThread.txt)
	STDEV_O_S=$(stdev $ROOT_OUTPUT_FOLDER/$app/$app-original-singleThread.txt)
	echo "Processing: "$ROOT_OUTPUT_FOLDER/$app/$app-refactored-singleThread.txt
	AVERAGE_R_S=$(average $ROOT_OUTPUT_FOLDER/$app/$app-refactored-singleThread.txt)
	STDEV_R_S=$(stdev $ROOT_OUTPUT_FOLDER/$app/$app-refactored-singleThread.txt)

	cat $ROOT_OUTPUT_FOLDER/singleThreadEnergyResults.csv > /tmp/tmp.txt
	printf $app"_Original,"$app"_Refactored\n"$AVERAGE_O_S","$AVERAGE_R_S"\n"$STDEV_O_S","$STDEV_R_S | paste -d, /tmp/tmp.txt - > $ROOT_OUTPUT_FOLDER/singleThreadEnergyResults.csv

	# Multi thread
	echo "Processing: "$ROOT_OUTPUT_FOLDER/$app/$app-original-multiThread.txt
	AVERAGE_O_M=$(average $ROOT_OUTPUT_FOLDER/$app/$app-original-multiThread.txt)
	STDEV_O_M=$(stdev $ROOT_OUTPUT_FOLDER/$app/$app-original-multiThread.txt)
	echo "Processing: "$ROOT_OUTPUT_FOLDER/$app/$app-refactored-multiThread.txt
	AVERAGE_R_M=$(average $ROOT_OUTPUT_FOLDER/$app/$app-refactored-multiThread.txt)
	STDEV_R_M=$(stdev $ROOT_OUTPUT_FOLDER/$app/$app-refactored-multiThread.txt)

	cat $ROOT_OUTPUT_FOLDER/multiThreadEnergyResults.csv > /tmp/tmp.txt
	printf $app"_Original,"$app"_Refactored\n"$AVERAGE_O_M","$AVERAGE_R_M"\n"$STDEV_O_M","$STDEV_R_M | paste -d, /tmp/tmp.txt - > $ROOT_OUTPUT_FOLDER/multiThreadEnergyResults.csv
done

