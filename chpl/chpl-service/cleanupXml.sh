#!/bin/bash
#(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# to clean up the XML files regularly, add a line to a crontab on the machine hosting the application that looks something like:
# 15 5 * * * cd /some/directory/chpl-api/chpl/chpl-service && ./cleanupXml.sh
# This will run it at 0515 UTC, which (depending on DST) is 0015 EST

# parse command line inputs
#   default to 30 days, and not deleting "first of the month" files
numdays=30
dflag='false'

while getopts 'dn:h?' flag; do
    case "${flag}" in
        d) dflag='true' ;;
        n) numdays="${OPTARG}" ;;
        *) printf 'Usage: %s: [-d] [-n numdays]
   -d: delete the first of the month files
   -n: number of days to keep
   -h, -?: print this message\n' $0; exit 0 ;;
    esac
done

#find the folder that has the XML files
if [ ! -f environment.properties ]; then
    printf "\nError: environment.properties file not found\n\n"
    exit 1
fi
xmlFolder=$(cat environment.properties | grep downloadFolderPath | cut -d '=' -f2)

echo "Deleting files more than $numdays days old from $xmlFolder";

if [ "$dflag" = true ]; then
    echo "  including those from the first of the month"
    find $xmlFolder -type f -mtime +$numdays -delete
else
    echo "  excepting those from the first of the month"
    find $xmlFolder -type f -mtime +$numdays \
         -exec sh -c 'test $(LC_TIME=C date +%d -r "$1") = 01 || rm "$1"' -- {} \;
fi
