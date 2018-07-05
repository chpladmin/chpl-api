#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# to generate the report, add a line to a crontab on the machine hosting the application that looks something like:
# 15 5 * * 1 cd /some/directory/chpl-api/chpl/chpl-service && ./generateQuestionableActivityReport.sh
# This will run it at 0515 UTC, which (depending on DST) is 0015 EST

# deal with spaces in filenames by saving off the default file separator (including spaces)
# and using a different one for this application
SAVEIFS=$IFS
IFS=$(echo -en "\n\b")

#get the start date as 7 days ago
STARTDATE=$(date -d "7 days ago" '+%F')
# get end date as current day
ENDDATE=$(date "+%F")

# QuestionableActivityReport application takes two parameters: startDate and endDate. 
# The dates have this format: yyyy-mm-dd
java -Xmx800m -Dchpl.home=/opt/chpl -Dchpl.appName=generateQuestionableActivityReport -Dlog4j.configurationFile=log4j2-app.xml -cp target/chpl-service-jar-with-dependencies.jar gov.healthit.chpl.app.questionableActivity.QuestionableActivityReportApp $STARTDATE $ENDDATE

# restore filename delimiters
IFS=$SAVEIFS
