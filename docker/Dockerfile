# Stage 1: Build the WAR file using Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy the pom.xml files first to leverage Docker layer caching
# This ensures that if only source code changes, dependencies are not re-downloaded
COPY chpl/pom.xml .
COPY chpl/chpl-api/pom.xml chpl-api/
COPY chpl/chpl-resources/pom.xml chpl-resources/
COPY chpl/chpl-service/pom.xml chpl-service/

# Copy the rest of the project files
COPY chpl/chpl-api/lombok.config chpl-api/lombok.config
COPY chpl/chpl-api/src chpl-api/src
COPY chpl/chpl-resources/src chpl-resources/src
COPY chpl/chpl-service/lombok.config chpl-service/lombok.config
COPY chpl/chpl-service/src chpl-service/src

# Activates chpl-resources' "environment-specific" Maven profile, which layers
# src/main/resources-${MAVEN_ENV} (log4j2 config, notably the catch-all
# gov.healthit.chpl logger/chplserviceJson appender that Datadog's chplservice
# service tag depends on) over the base resources. Matches the -Denv=<env>
# flag chpl-build-common's CompileApiTask already passes for the
# build-from-source path in every non-GHCR environment. Set via --build-arg
# from docker-publish.yml, mapped from the branch being built.
ARG MAVEN_ENV=dev
RUN mvn clean package -DskipTests -Denv=${MAVEN_ENV}

# Stage 2: Deploy to Tomcat
#
# This image contains no secrets, and only differs across environments in
# the -Denv build arg above (log4j2 config naming/routing). Tomcat config
# below only has ${ENV_VAR}
# placeholders (resolved at container startup - see catalina.properties'
# org.apache.tomcat.util.digester.PROPERTY_SOURCE) and the checked-in
# environment.properties/email.properties already mark every sensitive key
# as SECRET, relying on Spring's Environment to prefer OS environment
# variables over those classpath files.
#
# Required environment variables at `docker run` time:
#   DB_URL, DB_USERNAME, DB_PASSWORD        - jdbc/openchpl datasource
#   plus every property marked SECRET in
#   chpl/chpl-resources/src/main/resources/environment.properties and email.properties
#   (e.g. SPRING_REDIS_PASSWORD, COGNITO_SECRETKEY, AZURE_CLIENTSECRET_ONC, JIRA_PASSWORD, ...)
FROM tomcat:11.0.24-jdk21

COPY docker/tomcat-conf/server.xml /usr/local/tomcat/conf/server.xml
COPY docker/tomcat-conf/context.xml /usr/local/tomcat/conf/context.xml
COPY docker/tomcat-conf/catalina.properties /usr/local/tomcat/conf/catalina.properties
# Empty Quartz job-scheduling-data template. Only the dev/qa/staging
# resources-${MAVEN_ENV} quartz.properties (now actually applied - see the
# -Denv build above) set
# org.quartz.plugin.jobInitializer.fileNames=jobs.xml,system-triggers.xml,user-triggers.xml
# with failOnFileNotFound=true; jobs.xml/system-triggers.xml ship on the
# classpath via chpl-resources, but this one doesn't. Quartz loads it via
# ClassLoadHelper.getResourceAsStream (classloader lookup only - no
# filesystem fallback, confirmed by decompiling quartz-2.5.2's
# XMLSchedulingDataProcessor), so it must be somewhere actually on Tomcat's
# common classloader. catalina.properties' common.loader only lists
# ${catalina.home}/lib (as a bare directory - Tomcat adds a whole
# directory's contents as a class repository, not just its jars, verified
# against this image's own bootstrap.jar ClassLoaderFactory); conf/ is not
# on any loader list. Putting it in conf/ (as a prior attempt did) silently
# never gets found - production's quartz.properties doesn't reference this
# file at all, so it's a harmless no-op there either way.
COPY docker/tomcat-conf/user-triggers.xml /usr/local/tomcat/lib/user-triggers.xml

COPY --from=build /app/chpl-api/target/chpl-service.war /usr/local/tomcat/webapps/chpl-service.war

# Matches the port every other environment's Tomcat listens on (the real
# dev box's server.xml, and chpl-build's start-tomcat-containers.sh, which
# hard-codes `-p host:8080` for the docker run container-port mapping).
EXPOSE 8080
