# Stage 1: Build the WAR file using Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS build
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
FROM tomcat:11.0.21-jdk17
# Copy the custom server.xml
COPY tomcat-config/* /usr/local/tomcat/conf
# Copy the WAR file from the build stage
COPY --from=build /app/chpl-api/target/chpl-service.war /usr/local/tomcat/webapps/chpl-service.war

# Expose our custom Tomcat port
EXPOSE 8181

# Start Tomcat
CMD ["catalina.sh", "run"]
