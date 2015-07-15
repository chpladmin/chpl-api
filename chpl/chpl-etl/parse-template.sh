#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# create timestamp and filename for log file
TIMESTAMP=$(date "+%Y.%m.%d-%H.%M.%S")
log=input/parsed/log.$TIMESTAMP.txt

# create TIMESTAMP and filename for running (monthly) log action taken
#   will output "no files found at $TIMESTAMP" if no files found
#   otherwise file name and timestamp for each file found
TIMESTAMP_MONTH=$(date "+%Y.%m")
monthly=input/parsed/log.monthly.$TIMESTAMP_MONTH.txt

# email parameters
E_TO="to@example.com to-also@example.com"
E_FROM=notifier@example.com
E_SUBJ='"CHPL Updates found"'
E_MSG='"Updates were found during the run"'
E_SMTP=smtp.example.com:25
E_UN=username
E_PW=password

# ensure output folders exist
mkdir -p input/parsed/input

# deal with spaces in filenames by saving off the default file separator (including spaces)
# and using a different one for this application
SAVEIFS=$IFS
IFS=$(echo -en "\n\b")

# loop through input files that end with .xlsx
# for any given file,
#   echo file name to the console, so a manual user may run tail -f to follow the log
#   put the file name into the log file
#   parse the file
#   bracket the log with the filename again
#   move the file to the /parsed subdirectory so it's not parsed again
#   copy the to-update.csv, prepended with the source filename, into the /parsed directory
#   email the to-update file to interested parties
if ls input/*.xlsx 1> /dev/null 2>&1; then
    echo $log
    for i in $(ls input/*.xlsx); do
        echo $i "found at:" $TIMESTAMP >> $monthly
        echo "####################################" >> $log
        echo "$i" >> $log
        java -jar target/chpl-etl-0.0.1-SNAPSHOT-jar-with-dependencies.jar "./$i" ./src/main/resources/plugins >> $log
        echo "$i" >> $log
        echo "####################################" >> $log
        mv "./$i" input/parsed/input/
        cp to-update.csv input/parsed/$i-to-update.csv

        sendemail -f $E_FROM -t $E_TO -u $E_SUBJ -m $E_MSG -s $E_SMTP -xu $E_UN -xp $E_PW -a '"input/parsed/'$i'-to-update.csv"' '"input/parsed/'$i'"' $log
    done
else
    echo "No files found at:" $TIMESTAMP >> $monthly
fi

# restore filename delimiters
IFS=$SAVEIFS
