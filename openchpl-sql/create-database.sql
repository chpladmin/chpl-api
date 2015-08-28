DROP DATABASE IF EXISTS openchpl;

CREATE DATABASE openchpl
  WITH OWNER = openchpl
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;

DROP DATABASE IF EXISTS openchpl_test;

CREATE DATABASE openchpl_test
  WITH OWNER = openchpl
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;
