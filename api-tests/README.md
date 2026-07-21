# API Test Layer

This module adds API-level testing to the Task Manager portfolio project. The current iOS app stores data locally, so this module uses a local mock backend from `../mock-api` to demonstrate backend contract testing, REST validation, and API automation architecture.

## Layers

```text
src/test/java/com/rabin/taskmanager/api/
  config/    API runtime configuration
  client/    HTTP client and response wrapper
  models/    Request and response DTOs
  support/   Mock API process lifecycle and health probing
  tests/     JUnit smoke and feature API suites
```

## Coverage

- `GET /health`
- `POST /tasks`
- title validation
- `GET /tasks/{id}`
- `PATCH /tasks/{id}`
- `DELETE /tasks/{id}`
- `DELETE /tasks`

## Run

From this folder:

```sh
mvn test
```

The tests start the mock API automatically on a free local port and stop it after the suite.

Run only smoke API tests:

```sh
mvn test -Dtest=TaskApiSmokeTest
```

Run only feature API tests:

```sh
mvn test -Dtest=TaskApiFeatureTest
```

Use a fixed mock API port:

```sh
mvn test -Dmock.api.port=8081
```

## Allure

Generate report:

```sh
mvn allure:report
```

Open report:

```sh
mvn allure:serve
```

Raw results:

```text
target/allure-results
```

