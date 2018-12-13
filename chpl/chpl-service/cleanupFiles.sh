#!/bin/bash
#(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# to clean up generated files regularly, add a line to a crontab on the machine hosting the application that looks something like:
# 15 5 * * * cd /some/directory/chpl-api/chpl/chpl-service && ./cleanupFiles.sh
# This will run it at 0515 UTC, which (depending on DST) is 0015 EST

# parse command line inputs
#   default to 30 days, and not deleting "first of the month" files
#   missing file type is a fail
#   missing file location is a fail
numdays=30
fileType=''
dflag='false'
fileLocation=''

while getopts 'dt:n:f:h?' flag; do
    case "${flag}" in
        d) dflag='true' ;;
        t) fileType="${OPTARG}" ;;
        n) numdays="${OPTARG}" ;;
        f) fileLocation="${OPTARG}" ;;
        *) printf 'Usage: %s: [-d] [-n numdays]
   -d: delete the first of the month files
   -t: type of files to remove [2011|2014|2015|surveillance|sed]
   -n: number of days to keep
   -f: folder where files are located
   -h, -?: print this message\n' $0; exit 0 ;;
    esac
done

# verify fileType is valid
if [ "$fileType" = "" ]; then
    printf "\nError: file type is required\n\n"
    exit 1
elif [ ! "$fileType" = "2011" ] && [ ! "$fileType" = "2014" ] && [ ! "$fileType" = "2015" ] && [ ! "$fileType" = "surveillance" ] && [ ! "$fileType" = "sed" ]; then
    printf "\nError: fileType must be one of 2011|2014|2015|surveillance|sed\n\n"
    exit 1
fi
# create regex using fileType
if [ "$fileType" = "2011" ] || [ "$fileType" = "2014" ] || [ "$fileType" = "2015" ]; then
    expression=".*chpl-$fileType-.*"
elif [ "$fileType" = "surveillance" ]; then
    expression=".*surveillance-.*csv"
elif [ "$fileType" = "sed" ]; then
    expression=".*chpl-sed-all-details.*csv"
fi

echo "Regex: $expression"

# find the folder that has the files to be removed
if [ "$fileLocation" = "" ]; then
    printf "\nError: file location is required\n\n"
    exit 1
fi

# execute
echo "Deleting $fileType files more than $numdays days old from $fileLocation";
if [ "$dflag" = true ]; then
    echo "  including those from the first of the month"
    echo " before:"
    find $fileLocation -type f -mtime +$numdays -regex $expression
    find $fileLocation -type f -mtime +$numdays -regex $expression \
         -delete
    echo " after:"
    find $fileLocation -type f -mtime +$numdays -regex $expression
else
    echo "  excepting those from the first of the month"
    echo " before:"
    find $fileLocation -type f -mtime +$numdays -regex $expression
    find $fileLocation -type f -mtime +$numdays -regex $expression \
         -exec sh -c 'test $(LC_TIME=C date +%d -r "$1") = 01 || rm "$1"' -- {} \;
    echo " after:"
    find $fileLocation -type f -mtime +$numdays -regex $expression
fi
