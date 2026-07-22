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

RUN mvn clean package -DskipTests

# Stage 2: Deploy to Tomcat
#
# This image contains no secrets and is identical across every environment
# (dev/qa/stage/production). Tomcat config below only has ${ENV_VAR}
# placeholders (resolved at container startup - see catalina.properties'
# org.apache.tomcat.util.digester.PROPERTY_SOURCE) and the checked-in
# environment.properties/email.properties already mark every sensitive key
# as SECRET, relying on Spring's Environment to prefer OS environment
# variables over those classpath files. See entrypoint.sh for the one
# exception (the JWK signing key, which the app reads from a file path).
#
# Required environment variables at `docker run` time:
#   DB_URL, DB_USERNAME, DB_PASSWORD        - jdbc/openchpl datasource
#   JWK_KEY                                 - contents of the RSA JOSE JWK signing key
#   plus every property marked SECRET in
#   chpl/chpl-resources/src/main/resources/environment.properties and email.properties
#   (e.g. SPRING_REDIS_PASSWORD, COGNITO_SECRETKEY, AZURE_CLIENTSECRET_ONC, JIRA_PASSWORD, ...)
FROM tomcat:11.0.23-jdk21

COPY docker/tomcat-conf/server.xml /usr/local/tomcat/conf/server.xml
COPY docker/tomcat-conf/context.xml /usr/local/tomcat/conf/context.xml
COPY docker/tomcat-conf/catalina.properties /usr/local/tomcat/conf/catalina.properties
COPY docker/entrypoint.sh /usr/local/tomcat/bin/entrypoint.sh
RUN chmod +x /usr/local/tomcat/bin/entrypoint.sh

COPY --from=build /app/chpl-api/target/chpl-service.war /usr/local/tomcat/webapps/chpl-service.war

# Path (not a secret) where the JWK key gets written by entrypoint.sh
ENV keyLocation=/usr/local/tomcat/conf/JSONRsaJoseJWebKey.txt

# Expose our custom Tomcat port
EXPOSE 8181

ENTRYPOINT ["/usr/local/tomcat/bin/entrypoint.sh"]
