#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

action=''
job=''
group=''
username=''
password=''

while getopts 'a:j:g:u:h?' flag; do
    case "${flag}" in
        a) action="${OPTARG}" ;;
        j) job="${OPTARG}" ;;
        g) group="${OPTARG}" ;;
        u) username="${OPTARG}" ;;
        *) printf 'Usage: %s: [-a action] [-j jobName] [-g groupName] [-u username]
   -a: action (one of "list" "start" "help" "interrupt")
   -j: job name (required if -g flag is used)
   -g: group name (optional, required if username/password is needed)
   -u: username (required for certain jobs)
   -h, -?: print this message\n' $0; exit 0 ;;
    esac
done

# group missing
if [ "$group" = "" ] && [ ! "$username" = "" ]; then
    printf "\nError: group must be specified if username is\n\n"
fi

# prompt for password if username was provided
if [ ! "$username" = "" ]; then
    stty -echo
    printf 'If your password has spaces or weird characters I have no idea what will happen next. Oh, and if you Ctrl-C before entering your password your terminal will stop echoing whatever you type. Good luck!\n'
    printf 'Enter password: '
    read password
    stty echo
    printf '\n'
fi

# job / group don't mismatch
if [ "$job" = "" ] && [ ! "$group" = "" ]; then
    printf "\nError: job must be specified if group is\n\n"
    exit 1
fi

java -Xmx800m -Dchpl.home=/opt/chpl -Dchpl.appName=TriggerJob -Dlog4j.configurationFile=log4j2-app.xml -cp target/chpl-service-jar-with-dependencies.jar gov.healthit.chpl.app.TriggerJob $action $job $group $username $password
