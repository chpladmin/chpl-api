#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# parse command line inputs
#   default to not initializing the triggers
init=''

while getopts 'ih?' flag; do
    case "${flag}" in
        i) init='init' ;;
        *) printf 'Usage: %s: [-i]
   -i: initialize the triggers
   -h, -?: print this message\n' $0; exit 0 ;;
    esac
done

# create timestamp and filename
TIMESTAMP=$(date "+%Y.%m.%d-%H.%M.%S")
log=logs/log.runScheduler.$TIMESTAMP.txt

# put header info into log, then output application info into log file
echo "Starting Scheduler at: " $TIMESTAMP >> $log
echo "####################################" >> $log
java -Xmx800m -cp target/chpl-service-jar-with-dependencies.jar gov.healthit.chpl.scheduler.ChplScheduler $init >&1 >> $log
echo "####################################" >> $log
