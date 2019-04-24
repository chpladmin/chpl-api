#!/bin/bash
a2dissite 000-default.conf
service apache2 reload
service tomcat8 stop

# extract running queries
#psql -Uopenchpl -h $DB -c "\copy (SELECT pid, age(query_start, clock_timestamp()), usename, query FROM pg_stat_activity WHERE query != '<IDLE>' AND query NOT ILIKE '%pg_stat_activity%' ORDER BY query_start desc) to active_queries.csv with csv header"

service tomcat8 start
a2ensite 000-default.conf
until [[ "$(curl --silent --fail http://localhost:8080/chpl-service/system-status)" =~ "{\"running\":\"OK\",\"cache\":\"OK\"" ]]; do printf '.'; sleep 5; done && service apache2 reload
