#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

job=''
group=''

while getopts 'j:g:h?' flag; do
    case "${flag}" in
        j) job="${OPTARG}" ;;
        g) group="${OPTARG}" ;;
        *) printf 'Usage: %s: [-j jobName] [-g groupName ]
   -j: job name (required if -g flag is used)
   -g: group name
   -h, -?: print this message\n' $0; exit 0 ;;
    esac
done

# job / group don't mismatch
if [ "$job" = "" ] && [ ! "$group" = "" ]; then
    printf "\nError: job must be specified if group is\n\n"
    exit 1
fi

java -Xmx800m -Dchpl.home=/opt/chpl -Dchpl.appName=TriggerJob -Dlog4j.configurationFile=log4j2-app.xml -cp target/chpl-service-jar-with-dependencies.jar gov.healthit.chpl.app.TriggerJob $job $group
