from scipy.stats import mannwhitneyu
import csv
import sys

with open(sys.argv[1], 'rb') as csvfileOriginalData:
	readerO = csv.reader(csvfileOriginalData, delimiter=',')
	dataO=[]
	for row in readerO:
		dataO=dataO+[row[0]]
with open(sys.argv[2], 'rb') as csvfileRefactoredData:
	readerR = csv.reader(csvfileRefactoredData, delimiter=',')
	dataR=[]
	for row in readerR:
		dataR=dataR+[row[0]]
result = mannwhitneyu(dataO,dataR,alternative='two-sided')
testFlag=0
if result.pvalue < 0.01:
	testFlag=1
testFlag5=0
if result.pvalue < 0.05:
	testFlag5=1
print result.statistic,",",testFlag,",",testFlag5
