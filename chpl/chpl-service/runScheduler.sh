#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

java -Xmx800m -Dchpl.home=/opt/chpl -Dchpl.appName=runScheduler -Dlog4j.configurationFile=log4j2-app.xml -cp target/chpl-service-jar-with-dependencies.jar gov.healthit.chpl.scheduler.ChplScheduler
