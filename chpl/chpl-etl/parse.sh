#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

TIMESTAMP=$(date "+%Y.%m.%d-%H.%M.%S")
log=input/parsed/log.$TIMESTAMP.txt # log file location
echo $log # provided for tail -f, if script is run in background

if ls input/*.xlsx 1> /dev/null 2>&1; then # if data input files exist
    for i in $(ls input/*.xlsx); do # loop through them
        echo "####################################" >> $log
        echo $i >> $log # data file name goes into log
        java -jar target/chpl-etl-0.0.1-SNAPSHOT-jar-with-dependencies.jar ./$i ./src/main/resources/plugins >> $log # run ETL, pipe output into log
        echo $i >> $log # data file name goes into log
        echo "####################################" >> $log
        mv ./$i input/parsed/ # move file out of parsing directory
    done
else
    echo "No input files found" > $log
fi
