psql -Upostgres -f drop-openchpl.sql openchpl_test
psql -Upostgres -f openchpl.sql openchpl_test
psql -Upostgres -f openchpl_invite_users.sql openchpl_test
psql -Upostgres -f audit-openchpl.sql openchpl_test
psql -Upostgres -f preload-openchpl.sql openchpl_test
psql -Upostgres -f openchpl_create_view_cert.sql openchpl_test
psql -Upostgres -f openchpl_create_view_cqm.sql openchpl_test
psql -Upostgres -f openchpl_create_view_search.sql openchpl_test