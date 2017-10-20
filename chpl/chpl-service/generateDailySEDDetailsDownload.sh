#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# to generate the SED detail file regularly, add a line to a crontab on the machine hosting the application that looks something like:
# 15 5 * * * cd /some/directory/chpl-api/chpl/chpl-service && ./generateDailySEDDetailsDownload.sh
# This will run it at 0515 UTC, which (depending on DST) is 0015 EST

# create timestamp and filename
TIMESTAMP=$(date "+%Y.%m.%d-%H.%M.%S")
log=logs/log.generateSEDDetailsDownload.$TIMESTAMP.txt

# put header info into log, then output application info into log file
echo "SED Details generation at: " $TIMESTAMP >> $log
echo "####################################" >> $log
java -cp target/chpl-service-jar-with-dependencies.jar gov.healthit.chpl.app.resource.G3Sed2015ResourceCreatorApp 2>&1 >> $log
echo "####################################" >> $log