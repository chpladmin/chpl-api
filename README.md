# chpl-api

The CHPL api

# Installation instructions

## Install required software

Java 1.8.0
Postgres 9.4.4
mvn 3.3.3
Tomcat 8.0.24
Eclipse (latest)

## Clone the repository

```sh
$ git clone https://github.com/chpladmin/chpl-api.git
```

## Load the data model

See the instructions in [the Open Data CHPL data model README](https://github.com/chpladmin/chpl-data-model/blob/master/README.md).

## Authentication token & testing databases

The JSON token definition needs to be set in a file named `chpl/chpl-auth/src/main/resources/environment.auth.properties`. There is a file named `chpl/chpl-auth/src/main/resources/environment.auth.properties.template` that has the format. Copy that file and change the `keyLocation` key to something local.

Two files are used for testing purposes. There are template files for each of them that will need to be copied and renamed, with valid local data inserted:

```
chpl/chpl-auth/src/test/resources/environment.auth.test.properties
chpl/chpl-service/src/test/resources/environment.test.properties
```

## Properties files

Copy over the two template files and fill in the keylocation, downloadedFolderPath, and datasourcepassword

```
chpl/chpl-auth/src/main/resources/environment.auth.properties

chpl/chpl-service/src/main/resources/environment.properties
```

## Tomcat server

Modifiy `server.xml` to add a Resource to the GlobalNamingResources

`<Resource auth="Container" driverClassName="org.postgresql.Driver" maxActive="100" maxIdle="30" maxWait="10000" name="YOUR NAME HERE" password="PASSWORD HERE" type="javax.sql.DataSource" url="jdbc:postgresql://WHEREVER YOUR DATABASE IS" username="DATABASE PASSWORD"/>`

And add a ResourceLink to the Context in `context.xml`:

`<ResourceLink global="jdbc/DATABASE NAME" name="jdbc/DATABASE NAME" type="javax.sql.DataSource"/>`

Change the port number from 8080 to 8181 in the Connector of the server.xml file.

Copy the postgres-9.2 jar under Ai_PUBLIC\CHPL_Public\DB_Dump into the tomcat install /lib directory

## Package everything

```sh
$ cd chpl
$ mvn package
```

## Deploy webapp

```sh
$ cp chpl-service/target/chpl-service.war TOMCAT-INSTALLATION/webapps
```

## Run E2E autoamtion tests on API
1. npm install -g newman
2. newman run e2e/acb-controller.postman_collection.json --env-var "url=ENVURL" --env-var "apiKey=YOURAPIKEY"

