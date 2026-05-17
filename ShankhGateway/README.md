# ShankhGateway

Spring Boot application containing only the PO-portal / Shankh-related HTTP APIs extracted from employeeportal. Uses the same database and business logic as the main portal.

## URL prefix (configurable)

Default context path matches the main app:

```properties
server.servlet.context-path=${SHANKH_GATEWAY_CONTEXT_PATH:/employeeportal}
```

Override without code changes, for example:

```bash
export SHANKH_GATEWAY_CONTEXT_PATH=/employeeportal
java -jar shankhgateway.war
# or root context:
export SHANKH_GATEWAY_CONTEXT_PATH=/
```

API paths are then `{context-path}/api/...` (e.g. `/employeeportal/api/getAllEmployeeInfo`).

`EmployeePortalInterceptor` builds its whitelist from the same `server.servlet.context-path`, so security stays aligned when the prefix changes.

## Build

```bash
cd ShankhGateway
mvn clean package -DskipTests
```

WAR: `target/shankhgateway.war`

## Configuration

Copy or align `application.properties` / `application-uat.properties` with your environment (datasource, JWT secrets, file paths for timesheet documents, etc.)—same keys as employeeportal.

## Smoke checks

After deploy, call (with Po-portal JWT where required):

- `GET {context}/api/getAllEmployeeInfo`
- `GET {context}/api/healthCheck`
- `POST {context}/api/poProjectTimesheetSync` with body `Set<Long>` of PO ids
