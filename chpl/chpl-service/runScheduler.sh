#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# create timestamp and filename
TIMESTAMP=$(date "+%Y.%m.%d-%H.%M.%S")
log=logs/log.runScheduler.$TIMESTAMP.txt

# put header info into log, then output application info into log file
echo "Starting Scheduler at: " $TIMESTAMP >> $log
echo "####################################" >> $log
java -Xmx800m -cp target/chpl-service-jar-with-dependencies.jar gov.healthit.chpl.scheduler.ChplScheduler >&1 >> $log
echo "####################################" >> $log
