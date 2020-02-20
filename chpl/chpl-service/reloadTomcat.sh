#!/bin/bash
a2dissite 000-default.conf
systemctl reload apache2
systemctl stop tomcat8

# extract running queries
#psql -Uopenchpl -h $DB -c "\copy (SELECT pid, age(query_start, clock_timestamp()), usename, query FROM pg_stat_activity WHERE query != '<IDLE>' AND query NOT ILIKE '%pg_stat_activity%' ORDER BY query_start desc) to active_queries.csv with csv header"

systemctl start tomcat8
a2ensite 000-default.conf
until [[ "$(curl --silent --fail http://localhost:8080/chpl-service/system-status)" =~ "{\"running\":\"OK\",\"cache\":\"OK\"" ]]; do printf '.'; sleep 5; done && systemctl reload apache2
