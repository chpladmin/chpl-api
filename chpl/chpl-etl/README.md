[![Build Status](http://54.213.57.151:9090/job/andlar_chpl-api_chpl-etl/badge/icon)](http://54.213.57.151:9090/job/andlar_chpl-api_chpl-etl)

# Extract, Transform, Load Component of OpenDataCHPL

This ETL extracts the current CHPL data from a provided Excel file, transforms it as necessary to fit the OpenDataCHPL data model, and loads that data into a PostgreSQL database.

The process diagram is such: ![process diagram](process.png)

# Installation

## Getting the code

```sh
$ git clone https://github.com/andlar/chpl-api.git
$ cd chpl-api/chpl/chpl-etl
```

## Data model load

Edit `openchpl-role.sql` to set the password for the `openchpl` role. The role can be changed as well, but then make sure the other `.sql` files are adjusted appropriately.

```sh
$ psql -Upostgres -f openchpl-sql/openchpl-role.sql
$ psql -Upostgres -f openchpl-sql/openchpl.sql
$ psql -Upostgres -f openchpl-sql/preload-openchpl.sql
```

## ETL

Edit the parameters-template.prm file to fill in the JDBC database connection URL, username, and password for your database, and rename it to `parameters.prm`.

```sh
$ vi parameters-template.prm
$ mv parameters-template.prm parameters.prm
$ mvn package
```

# Running the ETL

```sh
$ cd chpl-api/chpl/chpl-etl
$ java -jar target/chpl-etl-0.0.1-SNAPSHOT-jar-with-dependencies.jar 'path-to-excel-file' 'path-to-plugins-directory'
```

The default parameters are:
 - Excel file: `./src/main/resources/chpl-large.xlsx`
 - Plugins directory: `./src/main/resources/plugins`
