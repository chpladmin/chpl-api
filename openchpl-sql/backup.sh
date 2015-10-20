#!/bin/bash
(set -o igncr) 2>/dev/null && set -o igncr; # this comment is required to trick cygwin into dealing with windows vs. linux EOL characters

psql --username=openchpl -c "delete from acl_entry using acl_object_identity where acl_object_identity = acl_object_identity.id and acl_object_identity.object_id_class = 3;"
psql --username=openchpl -c "delete from acl_object_identity where object_id_class = 3;"
pg_dump --dbname=openchpl --username=openchpl --data-only --column-inserts --attribute-inserts --table=user --table=contact --table=global_user_permission_map --table=invited_user --table=invited_user_permission --table=acl_sid --table=acl_object_identity --table=acl_entry > user_dump.sql

#reload the backup after database reset with
#psql --username=openchpl openchpl < user_dump.sql
