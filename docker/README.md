# Running the chpl-api Docker image

<!-- TEMP MARKER (ONC-5395, 2026-07-29): verifying docker-publish.yml triggers on push to development. Safe to revert. -->

`docker/Dockerfile` builds a **secret-free** image: the same image is used for
every environment (development/qa/staging/production). Nothing sensitive is
baked in at build time - all of it is supplied as container environment
variables when the image is actually run. See `docker/Dockerfile` and
`docker/tomcat-conf/` for how that works mechanically (Tomcat's
`EnvironmentPropertySource` for `server.xml`, Spring's `Environment` for
everything else).

This doc lists every environment variable the image needs or accepts. It does
**not** contain real values for any environment - only what each variable is
for and what kind of value it expects. Real values for existing environments
currently live in [AudaciousInquiry/chpl-build](https://github.com/AudaciousInquiry/chpl-build)
(`chpl-build-{dev,qa,stg,prod}/src/main/resources/override-api-properties*.sh`)
- that repo should stay private, and its values should never be copied into
this one.

## Quick start

```sh
docker run -d --name chpl-api \
  -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://<host>:5432/openchpl" \
  -e DB_USERNAME="<db-username>" \
  -e DB_PASSWORD="<db-password>" \
  -e chplUrlBegin="https://chpl-dev.healthit.gov" \
  -e SPRING_REDIS_HOST="<redis-host>" \
  -e SPRING_REDIS_PORT="6379" \
  -e SPRING_REDIS_PASSWORD="<redis-password>" \
  ghcr.io/<owner>/chpl-api:latest-development
```

Add `-e` flags for whichever of the feature-area variables below apply to
your environment - omitted ones just leave that feature unconfigured.

Note: property keys that contain dots (like `spring.redis.host`) are **not**
valid POSIX environment variable names on every shell/platform - Spring will
also match the all-caps/underscore form (`SPRING_REDIS_HOST`). Use whichever
form your deployment tooling handles more easily; both resolve to the same
property.

## Required to start at all

| Env var | Used for |
|---|---|
| `DB_URL` | JDBC URL for the `jdbc/openchpl` datasource (`server.xml`) |
| `DB_USERNAME` | DB username (`server.xml`) |
| `DB_PASSWORD` | DB password (`server.xml`) |

## Filesystem paths (need a mounted volume, not just an env var)

These properties are file/directory paths the app reads or writes at runtime.
Mount a volume at the path you set, or the app will fail writing to a
read-only container filesystem.

| Env var | Property | Purpose |
|---|---|---|
| `downloadFolderPath` | `downloadFolderPath` | Where generated downloadable files are written |
| `auditDataFilePath` | `auditDataFilePath` | Where audit data backups are written |

## Environment-dependent (not secret, but should differ per environment)

| Env var | Property | Purpose | Example shape |
|---|---|---|---|
| `chplUrlBegin` | `chplUrlBegin` | Public base URL for this environment | `https://chpl-qa.healthit.gov` |
| `EMAILBUILDER_CONFIG_EMAILSUBJECTSUFFIX` | `emailBuilder.config.emailSubjectSuffix` | Tag appended to outgoing email subjects | `[QA]` |
| `SERVER_ENVIRONMENT` | `server.environment` | `non-production` or `production` | |
| `REPORT_ENVIRONMENT` | `report.environment` | Label used on generated reports | `DEV`, `QA`, `STAGE`, `PROD` |

## Feature-area secrets

Every property below is marked `SECRET` in
[`environment.properties`](../chpl/chpl-resources/src/main/resources/environment.properties)
or [`email.properties`](../chpl/chpl-resources/src/main/resources/email.properties)
- meaning the app has no usable default and one of these env vars must be set
for that feature to work. If a feature isn't used in a given environment, its
variables can be omitted.

**Database / cache**
| Env var | Property |
|---|---|
| `SPRING_REDIS_HOST` | `spring.redis.host` |
| `SPRING_REDIS_PORT` | `spring.redis.port` |
| `SPRING_REDIS_PASSWORD` | `spring.redis.password` |

**AWS Cognito (authentication)**
| Env var | Property |
|---|---|
| `COGNITO_ACCESSKEY` | `cognito.accessKey` |
| `COGNITO_SECRETKEY` | `cognito.secretKey` |
| `COGNITO_REGION` | `cognito.region` |
| `COGNITO_USERPOOLID` | `cognito.userPoolId` |
| `COGNITO_USERPOOLCLIENTSECRET` | `cognito.userPoolClientSecret` |
| `COGNITO_CLIENTID` | `cognito.clientId` |
| `COGNITO_ENVIRONMENT_GROUPNAME` | `cognito.environment.groupName` |
| `COGNITO_SYSTEMUSERUUID` | `cognito.systemUserUuid` |
| `COGNITO_ANONYMOUSUSERUUID` | `cognito.anonymousUserUuid` |

**ONC Azure AD**
| Env var | Property |
|---|---|
| `AZURE_USER_ONC` | `azure.user.onc` |
| `AZURE_CLIENTID_ONC` | `azure.clientId.onc` |
| `AZURE_CLIENTSECRET_ONC` | `azure.clientSecret.onc` |
| `AZURE_TENANTID_ONC` | `azure.tenantId.onc` |

**JIRA**
| Env var | Property |
|---|---|
| `JIRA_USERNAME` | `jira.username` |
| `JIRA_PASSWORD` | `jira.password` |

**Datadog**
| Env var | Property |
|---|---|
| `DATADOG_APIKEY` | `datadog.apiKey` |
| `DATADOG_APPKEY` | `datadog.appKey` |

**AIA (Real World Testing validation)**
| Env var | Property |
|---|---|
| `AIA_AUTHENTICATE_CLIENTSECRET` | `aia.authenticate.clientSecret` |
| `AIA_AUTHENTICATE_CLIENTID` | `aia.authenticate.clientId` |

**FF4J admin console**
| Env var | Property |
|---|---|
| `FF4J_WEBCONSOLE_USERNAME` | `ff4j.webconsole.username` |
| `FF4J_WEBCONSOLE_PASSWORD` | `ff4j.webconsole.password` |

**Email / notifications**
| Env var | Property |
|---|---|
| `internalErrorEmailRecipients` | `internalErrorEmailRecipients` |
| `internalFutureCertificationStatusEmailRecipients` | `internalFutureCertificationStatusEmailRecipients` |
| `emailBuilder_config_forwardAddress` | `emailBuilder_config_forwardAddress` |
| `DIRECTREVIEW_CHPLCHANGES_EMAIL` | `directReview.chplChanges.email` |
| `DIRECTREVIEW_UNKNOWNCHANGES_EMAIL` | `directReview.unknownChanges.email` |

## Anything not listed here

`environment.properties`, `email.properties`, `lookup.properties`, and
`errors.properties` (all in `chpl/chpl-resources/src/main/resources/`) have
sensible non-secret defaults for everything else, and can still be overridden
the same way (env var name = property name, dots -> underscores, upper-cased)
if a particular deployment needs to tune something not listed above.
