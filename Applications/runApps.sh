#!/bin/bash

ROOT_FOLDER=$HOME/PowerMeter/Applications
ITERATIONS=10

# $1 processId
function terminateMonitor {
	kill -TERM $pid
	STATUS=1
	# 0 -> finished, 1 -> running
	while [ $STATUS -eq 1 ]; do
		sleep 1
		STATUS=$(ps p $1 | wc -l)
		let STATUS=STATUS-1
	done
}

# $1 outputFileName
# $2 application id
# Return: id of energy monitor process
function monitorEnergy {
	$HOME/PowerMeter/Micro-benchmarks/a.out > $ROOT_FOLDER/output/$2/$1 &
	pid=$(echo $!)
	echo $pid
}

# $1 appName
function switchToAppFolder {
	cd "$ROOT_FOLDER/$1"
	rm original_*
	rm refactored_*
}

function runApp {
	APP=$1
	CP=$2
	CLASS=$3
	EXTRA_ARGS="-w20 -i$ITERATIONS"
	if [ $APP = "GD" ]; then
		EXTRA_ARGS="-w20 -r10000 -i$ITERATIONS"
	fi
	switchToAppFolder $APP

	pid=$(monitorEnergy $APP-original-singleThread.txt $APP)
	java -cp $CP $CLASS original -t1 $EXTRA_ARGS
	terminateMonitor $pid
	mv original_* $ROOT_FOLDER/output/$APP/times/original-singleThread

	pid=$(monitorEnergy $APP-original-multiThread.txt $APP)
	java -cp $CP $CLASS original -t4 $EXTRA_ARGS
	terminateMonitor $pid 
	mv original_* $ROOT_FOLDER/output/$APP/times/original-multiThread

	pid=$(monitorEnergy $APP-refactored-singleThread.txt $APP)
	java -cp $CP $CLASS refactored -t1 $EXTRA_ARGS
	terminateMonitor $pid
	mv refactored_* $ROOT_FOLDER/output/$APP/times/refactored-singleThread

	pid=$(monitorEnergy $APP-refactored-multiThread.txt $APP)
	java -cp $CP $CLASS refactored -t4 $EXTRA_ARGS
	terminateMonitor $pid
	mv refactored_* $ROOT_FOLDER/output/$APP/times/refactored-multiThread
}

function runMultiProjectApp {
	APP=$1
	CP=$2
	CLASS=$3
	ORIGINAL_FOLDER=$4
	REFACTORED_FOLDER=$5
	EXTRA_ARGS="-w20 -i$ITERATIONS"
	if [ $APP = "SA" ]; then
		EXTRA_ARGS="-w20 -r5000 -i$ITERATIONS"
	fi 
	if [ $APP = "Bayes" ]; then
		EXTRA_ARGS="-w20 -i5000 -r$ITERATIONS"
	fi 
	switchToAppFolder $APP

	cd $ORIGINAL_FOLDER

	pid=$(monitorEnergy $APP-original-singleThread.txt $APP)
	java -cp $CP $CLASS -t1 $EXTRA_ARGS
	terminateMonitor $pid
	mv original_* $ROOT_FOLDER/output/$APP/times/original-singleThread

#	pid=$(monitorEnergy $APP-original-multiThread.txt $APP)
#	java -cp $CP $CLASS -t2 $EXTRA_ARGS
#	terminateMonitor $pid 
#	mv original_* $ROOT_FOLDER/output/$APP/times/original-multiThread
	
	cd ../$REFACTORED_FOLDER

	pid=$(monitorEnergy $APP-refactored-singleThread.txt $APP)
	java -cp $CP $CLASS -t1 $EXTRA_ARGS
	terminateMonitor $pid
	mv refactored_* $ROOT_FOLDER/output/$APP/times/refactored-singleThread

#	pid=$(monitorEnergy $APP-refactored-multiThread.txt $APP)
#	java -cp $CP $CLASS -t2 $EXTRA_ARGS
#	terminateMonitor $pid
#	mv refactored_* $ROOT_FOLDER/output/$APP/times/refactored-multiThread
}

function runGD {
	runApp GD bin com.isistan.gradientdescent.Main
}

function runNQ {
	runApp NQ bin Main
}

function runSA {
	runMultiProjectApp SA bin:lib/jnlp.jar jaligner.example.SmithWatermanGotohExample JAlignmentMod JAlignment
}

function runBayes {
	WEKA_CP=lib/arpack_combined_all.jar:lib/bounce.jar:lib/commons-compress-1.10.jar:lib/core.jar:lib/java-cup.jar:lib/JFlex.jar:lib/junit.jar:lib/mtj.jar:bin
	runMultiProjectApp Bayes $WEKA_CP weka.MainEvaluation WekaOriginal WekaModified
}

function runFFT {
	runMimiApp FFT fft fftMod
}

function runMMult {
	runMimiApp Mmult mmult mmultMod
}

function runKP {
	runMimiApp KP knap knapMod
}

# $1 App Name (on disk)
# $2 App Name (original, as defined in the project)
# $3 App Name (refactored, as defined in the project)
function runMimiApp {
	APP=$1
	EXTRA_ARGS="-i$ITERATIONS -r1000000"

	pid=$(monitorEnergy $APP-original-singleThread.txt $APP)
	cd AppsSequential
	java -cp bin apps.main.Main $2 -e20 $EXTRA_ARGS
	terminateMonitor $pid
	#mv original_* $ROOT_FOLDER/output/$APP/times/original-singleThread
	outputFilename=${ARRAY[$2]}
	mv "$outputFilename" $ROOT_FOLDER/output/$APP/times/original-singleThread/original_0

	pid=$(monitorEnergy $APP-original-multiThread.txt $APP)
	cd ../AppsParallel
	java -cp bin apps.main.Main $2 -t4 -w20 $EXTRA_ARGS
	terminateMonitor $pid
	count=0
	for output in $(ls $2_*); do
		mv $output $ROOT_FOLDER/output/$APP/times/original-multiThread/original_$count
		let count=count+1
	done

	pid=$(monitorEnergy $APP-refactored-singleThread.txt $APP)
	cd ../AppsSequential
	java -cp bin apps.main.Main $3 -e20 $EXTRA_ARGS
	terminateMonitor $pid
	#mv refactored_* $ROOT_FOLDER/output/$APP/times/refactored-singleThread
	outputFilename=${ARRAY[$3]}
	mv "$outputFilename" $ROOT_FOLDER/output/$APP/times/refactored-singleThread/refactored_0

	pid=$(monitorEnergy $APP-refactored-multiThread.txt $APP)
	cd ../AppsParallel
	java -cp bin apps.main.Main $3 -t4 -w20 $EXTRA_ARGS
	terminateMonitor $pid
	count=0
	for output in $(ls $3_*); do
		mv $output $ROOT_FOLDER/output/$APP/times/refactored-multiThread/refactored_$count
		let count=count+1
	done

	cd ..
}

echo "Enter 'sudo' password below to enable serial port access:"
sudo chmod 666 /dev/ttyUSB0
sleep 5 # Enough time to let the CPU/disk to calm down and to turn the screen off

echo "**** Nqueens ****"
#runNQ

echo "**** Gradient descent ****"
#runGD

echo "**** Sequence alignment ****"
#runSA

echo "**** Bayes classifier ****"
runBayes

declare -A ARRAY
ARRAY[fft]="Medicion manual Fft"
ARRAY[fftMod]="Medicion manual Fft modificada"
ARRAY[knap]="Medicion manual Knapsack"
ARRAY[knapMod]="Medicion manual Knapsack modificada"
ARRAY[mmult]="Medicion manual Mmult"
ARRAY[mmultMod]="Medicion manual Mmult modificada"

echo "**** Fast Fourier Transform ****"
switchToAppFolder FFT-Mmult-KP
runFFT

echo "**** Matrix multiplication ****"
runMMult

echo "**** Knapsack ****"
runKP
cd ..

echo "**** Test finished ****"
