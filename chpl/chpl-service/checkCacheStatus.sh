#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# create timestamp and filename
TIMESTAMP=$(date "+%Y.%m.%d-%H.%M.%S")
log=logs/log.checkCacheStatus.$TIMESTAMP.txt

# put header info into log, then output application info into log file
echo "Check cache status: " $TIMESTAMP >> $log
echo "####################################" >> $log
java -Xmx800m -cp target/chpl-service-jar-with-dependencies.jar gov.healthit.chpl.app.CacheStatusAgeApp 2>&1 >> $log
echo "####################################" >> $log
