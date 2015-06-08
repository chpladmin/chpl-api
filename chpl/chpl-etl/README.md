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

Edit `openchpl-role.sql` to set the password for the `openchpl` role. These instructions assume the role/username used for the openchpl database is `openchpl`, and that the password in `openchpl-role.sql`, currently recorded as "change this password" will be update to match your installation. If the installer chooses to change the username/role, make sure it's also changed in the `openchpl.sql` file wherever the role is used.

```sh
$ psql -Upostgres -f chpl-api/openchpl-sql/openchpl-role.sql
$ psql -Upostgres -f chpl-api/openchpl-sql/openchpl.sql
$ psql -Upostgres -f chpl-api/openchpl-sql/preload-openchpl.sql
```

## ETL

Edit the parameters-template.prm file to fill in the JDBC database connection URL, username, and password for your database, and rename it to `parameters.prm`. The username and password in the newly created `parameters.prm` file much match the username/role and password from the `openchpl-role.sql` file referenced in the previous section.

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

## Resetting

After running the ETL, if you want to clean the database to run it again, as if from scratch, go into the database, and `Truncate cascaded...` the `vendor` and `certified_product_checksum` tables. Don't Drop the tables, as that will remove them entirely from the database. Instead, we want to Truncate them, to empty them of data without destroying the structure. If a Drop command is accidentally used, just drop the entire database, and reload using the previously mentioned *.sql files.
