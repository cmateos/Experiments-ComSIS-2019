#!/bin/bash

# $1 appFolder
function cleanFolder {
	rm ~/PowerMeter/Applications/output/$1/*.txt 2> clean.log
	rm ~/PowerMeter/Applications/output/$1/times/original-singleThread/original_* 2> clean.log
	rm ~/PowerMeter/Applications/output/$1/times/original-multiThread/original_* 2> clean.log
	rm ~/PowerMeter/Applications/output/$1/times/refactored-singleThread/refactored_* 2> clean.log
	rm ~/PowerMeter/Applications/output/$1/times/refactored-multiThread/refactored_* 2> clean.log
}

cleanFolder Bayes
cleanFolder FFT
cleanFolder GD
cleanFolder KP
cleanFolder Mmult
cleanFolder NQ
cleanFolder SA
