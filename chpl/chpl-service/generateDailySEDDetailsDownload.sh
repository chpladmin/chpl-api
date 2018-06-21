#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

# to generate the SED detail file regularly, add a line to a crontab on the machine hosting the application that looks something like:
# 15 5 * * * cd /some/directory/chpl-api/chpl/chpl-service && ./generateDailySEDDetailsDownload.sh
# This will run it at 0515 UTC, which (depending on DST) is 0015 EST

java -Dchpl.home=/opt/chpl -Dchpl.appName=generateDailySEDDetailsDownload -Dlog4j.configurationFile=log4j2-app.xml -cp target/chpl-service-jar-with-dependencies.jar gov.healthit.chpl.app.resource.G3Sed2015ResourceCreatorApp