# chpl-api

The CHPL api

# Installation instructions

## Clone the repository

```sh
$ git clone https://github.com/chpladmin/chpl-api.git
```

## Load the data model

See the instructions in [the Open Data CHPL data model README](openchpl-sql/README.md).

## ETL

See the instructions in [the Open Data CHPL ETL README](chpl/chpl-etl/README.md).

## Authentication tokens

Modify three files that define the location of the JWT authentication tokens to point at a 'local to the webapp server' location

```
chpl/chpl-auth/src/main/resources/environment.auth.properties
chpl/chpl-auth/src/test/resources/environment.auth.properties
chpl/chpl-service/src/main/resources/environment.auth.properties
```

Change the `keyLocation` key to something local

## Tomcat server

Modifiy `server.xml` to add a Resource to the GlobalNamingResources

`<Resource auth="Container" driverClassName="org.postgresql.Driver" maxActive="100" maxIdle="30" maxWait="10000" name="YOUR NAME HERE" password="PASSWORD HERE" type="javax.sql.DataSource" url="jdbc:postgresql://WHEREVER YOUR DATABASE IS" username="DATABASE PASSWORD"/>`

And add a ResourceLink to the Context in `context.xml`:

`<ResourceLink global="jdbc/DATABASE NAME" name="jdbc/DATABASE NAME" type="javax.sql.DataSource"/>`

## Package everything

```sh
$ cd chpl
$ mvn package
```

## Deploy webapp

```sh
$ cp chpl-service/target/chpl-service.war TOMCAT-INSTALLATION/webapps
```
