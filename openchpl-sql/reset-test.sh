#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

psql -Upostgres -f drop-openchpl.sql openchpl_test
psql -Upostgres -f openchpl-role.sql
psql -Upostgres -f create-database-test.sql
psql -Upostgres -f openchpl.sql openchpl_test
psql -Upostgres -f audit-openchpl.sql openchpl_test
psql -Upostgres -f preload-openchpl.sql openchpl_test
psql -Upostgres -f preload-user_contact_acl.sql openchpl_test
