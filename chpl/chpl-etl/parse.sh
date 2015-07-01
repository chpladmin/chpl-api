#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# deal with spaces in filenames by saving off the default file separator (including spaces)
# and using a different one for this application
SAVEIFS=$IFS
IFS=$(echo -en "\n\b")

# create timestamp and filename for log file
# echo that file name to the console, so a manual user may run tail -f to follow the log
TIMESTAMP=$(date "+%Y.%m.%d-%H.%M.%S")
log=input/parsed/log.$TIMESTAMP.txt

# create TIMESTAMP and filename for monthly log of 'nothing found' runs
TIMESTAMP_NOACTIVITY=$(date "+%Y.%m")
no_action=input/parsed/log.noaction.$TIMESTAMP_NOACTIVITY.txt

# loop through input files that end with .xlsx
# for any given file,
#   put the file name into the log file
#   parse the file
#   bracket the log with the filename again
#   move the file to the /parsed subdirectory so it's not parsed again
if ls input/*.xlsx 1> /dev/null 2>&1; then
    echo $log
    for i in $(ls input/*.xlsx); do
        echo "####################################" >> $log
        echo "$i" >> $log
        java -jar target/chpl-etl-0.0.1-SNAPSHOT-jar-with-dependencies.jar "./$i" ./src/main/resources/plugins >> $log
        echo "$i" >> $log
        echo "####################################" >> $log
        mv "./$i" input/parsed/
    done
else
    echo "No files found at:" $TIMESTAMP >> $no_action
fi

# restore filename delimiters
IFS=$SAVEIFS
