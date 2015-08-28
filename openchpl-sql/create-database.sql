DROP DATABASE IF EXISTS openchpl;

CREATE DATABASE openchpl
  WITH OWNER = openchpl
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;
