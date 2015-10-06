#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

psql -Upostgres -f drop-openchpl.sql openchpl_test
psql -Upostgres -f openchpl.sql openchpl_test
psql -Upostgres -f openchpl_certified_product_pending.sql openchpl_test
psql -Upostgres -f openchpl_invite_users.sql openchpl_test
psql -Upostgres -f audit-openchpl.sql openchpl_test
psql -Upostgres -f preload-openchpl.sql openchpl_test
psql -Upostgres -f preload-user_contact_acl.sql openchpl_test
psql -Upostgres -f openchpl_create_view_cert.sql openchpl_test
psql -Upostgres -f openchpl_create_view_cqm.sql openchpl_test
psql -Upostgres -f openchpl_create_view_search.sql openchpl_test
