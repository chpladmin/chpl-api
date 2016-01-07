#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# create timestamp and filename
TIMESTAMP=$(date "+%Y.%m.%d-%H.%M.%S")
log=target/log.$TIMESTAMP.txt

# deal with spaces in filenames by saving off the default file separator (including spaces)
# and using a different one for this application
SAVEIFS=$IFS
IFS=$(echo -en "\n\b")

# put header info into log, then output application info into log file
echo "XML generation at: " $TIMESTAMP >> $log
echo "####################################" >> $log
java -jar target/chpl-service-jar-with-dependencies.jar >> $log
echo "####################################" >> $log

# restore filename delimiters
IFS=$SAVEIFS
