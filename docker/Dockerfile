# Deploy to Tomcat.
#
# The WAR is built by the "compile" job in .github/workflows/docker-publish.yml
# (the same `mvn clean package -Denv=<env>` command this image used to run
# itself, but with maven's ~/.m2 dependency cache already warm there) and
# downloaded into docker/build-context/ before this build runs, so the image
# build never needs to touch Maven Central.
#
# This image contains no secrets, and only differs across environments in
# the -Denv value the compile job built the WAR with (log4j2 config
# naming/routing). Tomcat config below only has ${ENV_VAR}
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
FROM tomcat:11.0.29-jdk25

# Java4Shell (CVE-2026-114938) mitigation: turn off the vulnerable C2 Base64
# decode intrinsic. Set through JDK_JAVA_OPTIONS (read by the `java` launcher
# itself) rather than a setenv.sh or CATALINA_OPTS, because chpl-build
# bind-mounts its own per-environment setenv.sh over
# /usr/local/tomcat/bin/setenv.sh (and can mount over conf/ too), so any file
# baked into the image there would be hidden at runtime. Those setenv.sh
# scripts only ever append to CATALINA_OPTS/JAVA_OPTS, and nothing in the
# deploy sets JDK_JAVA_OPTIONS, so this survives every environment's
# `docker run`. Drop it (or pass --build-arg JVM_SECURITY_OPTS=) once the
# base image ships a patched JDK (25.0.5.1 or later).
ARG JVM_SECURITY_OPTS="-XX:-UseBASE64Intrinsics"
ENV JDK_JAVA_OPTIONS="${JVM_SECURITY_OPTS}"

COPY docker/tomcat-conf/server.xml /usr/local/tomcat/conf/server.xml
COPY docker/tomcat-conf/context.xml /usr/local/tomcat/conf/context.xml
COPY docker/tomcat-conf/catalina.properties /usr/local/tomcat/conf/catalina.properties

COPY docker/build-context/chpl-service.war /usr/local/tomcat/webapps/chpl-service.war

# Matches the port every other environment's Tomcat listens on (the real
# dev box's server.xml, and chpl-build's start-tomcat-containers.sh, which
# hard-codes `-p host:8080` for the docker run container-port mapping).
EXPOSE 8080
