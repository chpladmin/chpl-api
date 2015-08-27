psql -Upostgres -f drop-openchpl.sql openchpl
psql -Upostgres -f openchpl-role.sql
psql -Upostgres -f create-database.sql
psql -Upostgres -f openchpl.sql openchpl
psql -Upostgres -f audit-openchpl.sql openchpl
psql -Upostgres -f preload-openchpl.sql openchpl
psql -Upostgres -f preload-user_contact_acl.sql openchpl
psql -Upostgres -f openchpl_create_view_search.sql openchpl
psql -Upostgres -f openchpl_create_view_cert.sql openchpl
psql -Upostgres -f openchpl_create_view_cqm.sql openchpl