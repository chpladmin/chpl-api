#!/bin/bash
a2dissite 000-default.conf
service apache2 reload
service tomcat8 stop
service tomcat8 start
a2ensite 000-default.conf
until [[ "$(curl --silent --fail http://localhost:8080/chpl-service/cache_status)" =~ "{\"status\": \"OK\"" ]]; do printf '.'; sleep 5; done && service apache2 reload
