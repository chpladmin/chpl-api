# chpl-api

The CHPL api

# Installation instructions

## Install required software

* Java 17
* Postgres 14.8
* Redis 6.0
* mvn 3.6.3 or higher
* Tomcat 8.5.x -- latest version
* Eclipse (latest)

## Clone the repository

```sh
$ git clone https://github.com/chpladmin/chpl-api.git
```

## Load the data model

See the instructions in [the Open Data CHPL data model README](https://github.com/chpladmin/chpl-data-model/blob/master/README.md).

## Properties files

The following Properties Files exist within the CHPL Resources project.

```
chpl/chpl-resources/src/main/resources/email.properties
chpl/chpl-resources/src/main/resources/environment.properties
chpl/chpl-resources/src/main/resources/errors.properties
chpl/chpl-resources/src/main/resources/lookup.properties
```
Any value in the above files can be overridden by creating an "override" file.  The file should be name `originalFileName-override.properties` and be placed somewhere on the classpath.  In the "override" file, you can redefine any existing key with a new value.

Each of the above files has "default" values for each key, though a few keys within the files have a value of "SECRET".  This applies to certain values that only apply to a particular instance of CHPL - things like server names, usernames, passwords, etc.

| File                   | Key                                | Description                                                                                                  |
|------------------------|------------------------------------|--------------------------------------------------------------------------------------------------------------|
| email.properties       | directReview.chplChanges.email     | Email address to be notified when changes are made to CHPL entities that involve Direct Reviews              |
| email.properties       | directReviewunknownChanges.email   | Email address to be notified when changes are made to CHPL entities that possibly involve Direct Reviews     |
| environment.properties | downloadFolderPath                 | Path to the downloadable files in CHPL                                                                       |
| environment.properties | keyLocation                        | Path and filename of file representing RSA JSON key                                                          |
| environment.properties | uploadErrorEmailRecipients         | Email address or user or group who should be notified if there is an error uploading a file                  |
| environment.properties | splitDeveloperErrorEmailRecipients | Email address or user or group who should be notified if there is an error splittinng a developer            |
| environment.properties | mergeDeveloperErrorEmailRecipients | Email address or user or group who should be notified if there is an error merging a developer               |
| environment.properties | smtpFrom                           | Email address that CHPL generated emails should be "from"                                                    |
| environment.properties | smtpHost                           | SMTP email server to send emails                                                                             |
| environment.properties | smtpPassword                       | User's password for authenticating with the email server                                                     |
| environment.properties | smtpPort                           | Email server's port                                                                                          |
| environment.properties | smtpUsername                       | Username for authenticating with the email server                                                            |
| environment.properties | emailBuilder_config_forwardAddress | For Non-PROD environments, emails will be sent to this address for verification                              |
| environment.properties | jira.username                      | Username for authenticating with JIRA                                                                        |
| environment.properties | jira.password                      | Password for authenticating with JIRA                                                                        |
| environment.properties | spring.redis.host                  | Name or ip address where Redis server exists                                                                 |
| environment.properties | spring.redis.port                  | Port used by Redis                                                                                           |
| environment.properties | spring.redis.password              | Password used for authenbtication with Redis                                                                 |


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

# E2E automation tests

API automation tests are run via newman, using yarn as an installation and execution system

To set up:

1. `yarn install` needs to be run once on any given system
1. copy the `e2e/env/sample.postman_environment.json` file to various formats of `e2e/env/environment.postman_environment.json` and change the url and apiKey values to be valid

To run tests, use `yarn e2e:environment`

NOTE: environment is case sensitive to the script in package.json file. so make sure to have environments file saved as in lowercase as well as yarn commands will be run with lowercase environments

E2E code can be analyzed for consisitency using ESLint, using the command `yarn e2e:lint`
