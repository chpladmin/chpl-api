#!/bin/bash
#(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# to clean up the XML files regularly, add a line to a crontab on the machine hosting the application that looks something like:
# 15 5 * * * cd /some/directory/chpl-api/chpl/chpl-service && ./cleanupXml.sh
# This will run it at 0515 UTC, which (depending on DST) is 0015 EST

#find the folder that has the XML files
xmlFolder=$(cat target/chpl-service/WEB-INF/classes/environment.properties | grep downloadFolderPath | cut -d '=' -f2)

echo "Deleting files from $xmlFolder";

#delete chpl product listing files that are older than 30 days except for the first of each month
find $xmlFolder  -type f -mtime +30 \
    -exec sh -c 'test $(LC_TIME=C date +%d -r "$1") = 01 || rm "$1"' -- {} \;
