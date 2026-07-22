# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SDET portfolio project: a SwiftUI iOS task manager app paired with a Java/Appium real-device UI automation framework, a Java REST API test suite, and a Node.js mock backend. CI/CD runs on GitHub Actions with a self-hosted Mac runner for real-device tests.

## Commands

### API Tests (Java/Maven)
```bash
cd api-tests
mvn test                                   # All API tests
mvn test -Dtest=TaskApiSmokeTest           # Smoke suite only
mvn test -Dtest=TaskApiFeatureTest         # Feature suite only
mvn test -Dmock.api.port=8081              # Custom API port
mvn allure:report                          # Generate HTML report
mvn allure:serve                           # Open report in browser
```

### UI Tests (Java/Maven — requires real iPhone + Appium server)
```bash
cd java-ui-tests
mvn test -Psmoke                           # 3 smoke tests
mvn test -Pfeature                         # 9 feature tests
mvn test -Dtest=TaskManagerFeatureTest     # Single test class
mvn test -DskipTests                       # Compile only
mvn allure:report && mvn allure:serve      # View results
```

Real-device parameters (required for UI tests):
```bash
mvn test -Psmoke \
  -Dios.device.name="iPhone" \
  -Dios.platform.version="<version>" \
  -Dios.udid="<device-udid>" \
  -Dios.bundle.id="rubisoft.rabin-task-manager" \
  -Dios.derived.data.path="/tmp/rabin-task-manager-wda-derived-data" \
  -Dios.xcode.org.id="<apple-team-id>" \
  -Dios.xcode.signing.id="Apple Development" \
  -Dios.xcode.config.file="$PWD/wda-signing.xcconfig" \
  -Dios.updated.wda.bundle.id="<unique-wda-bundle-id>" \
  -Dios.allow.provisioning.device.registration="true" \
  -Dios.wda.launch.timeout="180000" \
  -Dios.wda.connection.timeout="180000" \
  -Dios.wait.seconds="30"
```

### Appium Server (required before running UI tests)
```bash
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer \
appium --address 127.0.0.1 --port 4723
```

### Mock API (Node.js, no dependencies)
```bash
cd mock-api
MOCK_API_PORT=8081 node server.js
```

### iOS App Build
```bash
xcodebuild \
  -scheme rabin-task-manager \
  -configuration Debug \
  -destination 'generic/platform=iOS' \
  -derivedDataPath /tmp/rabin-task-manager-derived-data \
  CODE_SIGNING_ALLOWED=NO \
  build
```

## Architecture

### Three-Layer Test Automation Pattern (java-ui-tests)

```
tests/          JUnit 5 test classes — assertions only, no Appium calls
flows/          Business workflows (addTask, completeTask, deleteTask)
screens/        Page Object Model — all locators + low-level UI interactions
core/           AppiumActions.java — raw Appium primitives (find, click, swipe, scroll)
driver/         IosDriverFactory.java — capability configuration + IOSDriver instantiation
config/         TestConfig.java — reads all system properties (device params, timeouts)
reporting/      AllureArtifacts.java, AllureFailureExtension.java
```

Tests extend `BaseIosTest`, which uses `@TestInstance(PER_CLASS)` to create one Appium session per test class. `@BeforeEach` clears all task data between tests.

### API Test Pattern (api-tests)

```
tests/          JUnit 5 tests; extend BaseApiTest
client/         TaskApiClient.java — HTTP wrapper using java.net (no external HTTP lib)
models/         TaskRequest.java, TaskResponse.java — Jackson DTOs
support/        MockApiServer.java (process lifecycle) + ApiHealthProbe.java (polling)
config/         ApiTestConfig.java — port, base URL from system properties
```

`BaseApiTest.@BeforeAll` starts the mock API process and polls `/health`; `@AfterAll` stops it. The API test suite is entirely self-contained.

### Mock API (mock-api/server.js)

Pure Node.js, no dependencies. In-memory `Map` for task storage. Endpoints:
- `GET/POST /tasks`, `DELETE /tasks` (clear all)
- `GET /tasks/:id`, `PATCH /tasks/:id`, `DELETE /tasks/:id`
- `GET /health`

### iOS App (rabin-task-manager/)

SwiftUI with `@AppStorage` for persistence. Accessibility identifiers wired throughout for Appium targeting: `tasks-screen`, `task-title-field`, `task-notes-field`, `priority-picker`, `due-date-picker`, `add-task-button`, `filter-all`, `filter-open`, `filter-done`, `task-list`, `empty-state`. Task rows have dynamic IDs.

## Key Implementation Details

**Locator priority in TasksScreen.java:** accessibility ID → text XPath → iOS predicate string → iOS class chain → placeholder → element type.

**Scroll strategy:** repeated `mobile: swipe` gestures upward, polling for visibility after each swipe (handles SwiftUI lazy list rendering).

**Allure integration:** `@Epic`/`@Feature`/`@Story`/`@Step` used across all three layers. Failure extension captures screenshot + page source + log file. AspectJ agent is required (configured in Surefire argLine in pom.xml).

**Maven profiles:** `smoke` sets `junit.tags=smoke` (3 tests), `feature` sets `junit.tags=feature` (9 tests).

## CI/CD

`.github/workflows/ios-ui-tests.yml` defines two jobs:

1. **`build-and-compile-tests`** (GitHub-hosted `macos-latest`): builds iOS app (no signing), runs API tests against mock backend, compiles UI tests. Runs on every push/PR.

2. **`real-device-smoke`** (self-hosted Mac + iPhone): triggered manually via `workflow_dispatch`. Installs Appium, starts server, runs Maven with device params from GitHub Secrets/Variables (`IOS_UDID`, `IOS_XCODE_ORG_ID`, etc.), publishes Allure report links in step summary.

WebDriverAgent signing config: `java-ui-tests/wda-signing.xcconfig` (team ID `5J4252D7AH`).
