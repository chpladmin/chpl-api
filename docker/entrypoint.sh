#!/bin/sh
# Materializes the JWK signing key from an environment variable into the file
# path chpl-api expects (environment.properties: keyLocation). Everything else
# needed at runtime (DB creds, Cognito/Azure/Jira/Datadog/Redis secrets, etc.)
# is read directly from environment variables by Spring/Tomcat - see
# docker/tomcat-conf/ and chpl/chpl-resources/src/main/resources/environment.properties.
set -e

if [ -n "$JWK_KEY" ]; then
  printf '%s\n' "$JWK_KEY" > /usr/local/tomcat/conf/JSONRsaJoseJWebKey.txt
fi

exec catalina.sh run
