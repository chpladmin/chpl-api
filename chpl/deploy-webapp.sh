#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

rm ~/bin/apache-tomcat-8.0.24/webapps/chpl-service.war
rm -r ~/bin/apache-tomcat-8.0.24/webapps/chpl-service/
mvn clean
mvn package -DskipTests
cp chpl-service/target/chpl-service.war ~/bin/apache-tomcat-8.0.24/webapps/
