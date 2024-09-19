# Couchbase Spring Starter Kit
[![Open in GitHub Codespaces](https://github.com/codespaces/badge.svg)](https://codespaces.new/couchbase-starter-kit/couchbase-spring-starter)
[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.com/couchbase-starter-kit/couchbase-spring-starter)


## Configuration

| Variable Name                      | Description                                                 |      Default value       |
|:-----------------------------------|:------------------------------------------------------------|:------------------------:|
| SPRING_APPLICATION_NAME            | The name of your application, used for OTLP as well         | couchbase-spring-starter |
| SPRING_COUCHBASE_CONNECTION_STRING | A couchbase connection string                               |            -             |
| SPRING_COUCHBASE_USERNAME          | Username for authentication with Couchbase                  |            -             |
| SPRING_COUCHBASE_PASSWORD          | Password for authentication with Couchbase                  |            -             |
| COUCHBASE_USE_CAPELLA              | Use to change the connection profile                        |          false           |
| COUCHBASE_DEFAULT_BUCKET           | The name of the Couchbase Bucket, parent of the scope       |         default          |
| COUCHBASE_DEFAULT_SCOPE            | The name of the Couchbase scope, parent of the collection   |         _default         |
| COUCHBASE_DEFAULT_COLLECTION       | The name of the Couchbase collection to store the Documents |         _default         |
| COUCHBASE_OTLP_ENABLED             | Enable traces and metrics OTLP export                       |          false           |
| COUCHBASE_OTLP_ENADPOINT           | The OTLP server endpoint to send metrics and traces         |            -             |


## Unit Tests

Unit test in the `com.exqmple.integration` package are running against a cluster configured with env variables. Unit tests in the `com.exqmple.testcontainers` package are running with a Test Containers.

## OpenTelemetry tests

The current test setup assumes there is an OTLP endpoint available on http://localhost:4317. The fastest way to start one is to use https://github.com/CtrlSpice/otel-desktop-viewer