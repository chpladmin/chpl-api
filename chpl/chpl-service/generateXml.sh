#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# to generate the XML file regularly, add a line to a crontab on the machine hosting the application that looks something like:
# 15 5 * * * cd /some/directory/chpl-api/chpl/chpl-service && ./generateXml.sh
# This will run it at 0515 UTC, which (depending on DST) is 0015 EST

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
