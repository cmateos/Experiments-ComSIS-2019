#!/bin/bash

CLASSPATH=./bin:./lib/allocation.jar:./lib/caliper-0.0.jar

./a.out > measurement-$1.csv &
# get the id of the process attached to Power Meter
pid=$(echo $!)
java -cp $CLASSPATH $1
# Once the application has finished, we kill the monitoring process
kill -TERM $pid
