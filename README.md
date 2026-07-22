# Task Manager - iOS App With Appium Framework

This repository is a compact iOS task manager application with a production-style mobile UI automation framework around it. The project demonstrates both application delivery and SDET ownership: testability-focused SwiftUI implementation, Appium real-device automation, Page Object Model structure, Allure reporting, logging, and CI/CD readiness.

## Project Delivery Story

This project started from a full product delivery flow, not only from test automation. I used Xcode together with AI Codex as an engineering assistant to plan, design, develop, and iterate on a SwiftUI task manager iOS application. After the app was implemented, it was installed and validated on a real iPhone before the automation framework was built around it.

The delivery path was:

1. Planned the task manager scope and core user flows.
2. Designed a simple SwiftUI experience for creating and managing tasks.
3. Developed the iOS application in Xcode.
4. Added automation-friendly accessibility identifiers during app development.
5. Installed and opened the app on a real iPhone.
6. Built the Appium Java automation framework against the installed iOS app.
7. Stabilized the framework through real-device execution, reporting, cleanup, and CI/CD readiness.

This is important because the automation was not written against an abstract sample app. The application and the test framework evolved together, which reflects practical SDET ownership across product quality, testability, tooling, and delivery.

## Highlights

| Area | Implementation |
| --- | --- |
| iOS app | SwiftUI task manager with local persistence |
| Automation | Java, Maven, JUnit 5, Appium, Selenium, XCUITest, API tests |
| Design pattern | Layered framework with Page Object Model and business flows |
| Hybrid testing | Single test that exercises the API and iOS UI in one flow |
| Reporting | Allure reports with nested step flow, screenshots, page source, and logs |
| Logging | Logback test logs attached to failures |
| CI/CD | GitHub Actions for app build, test compile, and self-hosted real-device execution |
| Device target | Real iPhone execution through WebDriverAgent |
| Backend | Local mock REST API for API-layer testing |

## Application Scope

The iOS app covers a realistic task workflow:

- Create tasks with title and notes.
- Select priority: Low, Medium, High.
- Set a due date.
- Complete and reopen tasks.
- Filter tasks by All, Open, and Done.
- Delete a single task.
- Clear the entire task list with confirmation.
- Persist tasks locally with `@AppStorage`.
- Expose stable accessibility identifiers for UI automation.

Main app files:

```text
rabin-task-manager/
  ContentView.swift
  rabin_task_managerApp.swift
  Assets.xcassets/
```

## SDET Automation Architecture

The automation code is intentionally split into layers so tests stay readable and maintenance stays isolated.

```text
java-ui-tests/src/test/java/com/rabin/taskmanager/
  config/      Runtime configuration from Maven/system properties
  driver/      Appium IOSDriver and XCUITest capabilities
  core/        Low-level waits, locators, scrolling, gestures, diagnostics
  screens/     Page Object Model layer
  flows/       Business workflows used by tests
  hybrid/      API client and mock server support for hybrid tests
  tests/       JUnit test classes and assertions
  reporting/   Allure attachments and failure handling
```

Layer responsibilities:

| Layer | Purpose |
| --- | --- |
| `config` | Centralizes Appium, iOS, WDA, signing, and wait settings |
| `driver` | Builds the `IOSDriver` session and XCUITest capabilities |
| `core` | Provides reusable Appium actions with strong timeout diagnostics |
| `screens` | Owns screen locators and UI operations through POM |
| `flows` | Models business behavior such as add, complete, reopen, delete, clear |
| `hybrid` | Lightweight mock API client and server lifecycle for cross-layer tests |
| `tests` | Keeps test intent clean with JUnit and Allure metadata |
| `reporting` | Captures screenshots, page source, failure messages, and logs |

## Mock Backend And API Test Layer

The project also includes a local mock REST API so the portfolio demonstrates more than mobile UI automation.

```text
mock-api/
  server.js

api-tests/src/test/java/com/rabin/taskmanager/api/
  config/    API runtime configuration
  client/    Java HTTP client wrapper
  models/    API request and response DTOs
  support/   Mock server lifecycle and health checks
  tests/     API smoke and feature tests
```

Mock API endpoints:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/health` | Service health check |
| `GET` | `/tasks` | List tasks |
| `POST` | `/tasks` | Create task |
| `GET` | `/tasks/{id}` | Read task by id |
| `PATCH` | `/tasks/{id}` | Update task completion, notes, or priority |
| `DELETE` | `/tasks/{id}` | Delete one task |
| `DELETE` | `/tasks` | Clear all tasks |

API tests start the mock API automatically, validate the contract, and stop it after the suite.

## Test Coverage

Smoke coverage:

- App opens to the Tasks screen.
- User can add a task with notes.
- User can complete a task and verify filter behavior.

Feature coverage:

- Core controls are visible.
- Add Task is disabled until a title is entered.
- Default priority is Medium.
- High priority task creation.
- Task completion and reopen.
- Done/Open/All filtering.
- Persistence after app restart.
- Single-task deletion.
- Clear all tasks.
- Cleanup after test class execution.

API coverage:

- Health check.
- Create task contract.
- Required title validation.
- Read task by id.
- Complete task through PATCH.
- Delete task.
- Clear all tasks.

Hybrid coverage:

- API and iOS UI independently enforce the same default priority (Medium) and open state for a new task.
- UI task completion is verified through the Done filter.
- API task remains retrievable by id after the UI flow completes.

Latest local validation:

```text
Smoke:   Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
Feature: Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
```

## Allure Reporting

The framework produces Allure reports with readable process flow. Steps are generated from:

- Test metadata: `@Epic`, `@Feature`, `@Story`, `@Tag`.
- Business flow methods in `TaskManagerFlow`.
- Page Object methods in `TasksScreen`.
- Low-level actions in `AppiumActions`.
- JUnit assertions through `allure-jupiter-assert`.

On failure, the report includes:

- Failure message.
- Screenshot.
- Appium page source XML.
- `ui-tests.log`.

Generate and open a report:

```sh
cd java-ui-tests
mvn allure:report
mvn allure:serve
```

## CI/CD Strategy

GitHub Actions workflow:

```text
.github/workflows/ios-ui-tests.yml
```

Pipeline layers:

| Job | Runner | Purpose |
| --- | --- | --- |
| `build-and-compile-tests` | GitHub-hosted `macos-latest` | Build the iOS app without signing, run API tests against the mock backend, and compile the Java UI test project |
| `real-device-smoke` | Self-hosted Mac with iPhone | Start Appium, run smoke/feature tests, generate Allure, upload artifacts |

Real-device Appium execution needs a self-hosted runner because GitHub-hosted macOS runners do not provide access to your physical iPhone.

Required self-hosted runner labels:

```text
self-hosted
macOS
ios-device
```

Required GitHub secrets:

- `IOS_UDID`
- `IOS_XCODE_ORG_ID`

Optional GitHub variables:

- `IOS_DEVICE_NAME`
- `IOS_PLATFORM_VERSION`
- `IOS_BUNDLE_ID`
- `IOS_XCODE_SIGNING_ID`
- `IOS_UPDATED_WDA_BUNDLE_ID`

## Local Setup

Prerequisites:

- macOS with Xcode installed.
- Real iPhone connected, unlocked, trusted, and with Developer Mode enabled.
- Java 17.
- Maven.
- Node.js and npm.
- Appium with XCUITest driver.

## Tool Installation

Install or verify Xcode command line access:

```sh
xcode-select -p
sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
```

Install Java and Maven. With Homebrew:

```sh
brew install openjdk@17 maven
java -version
mvn -version
```

Install Node.js and npm. With Homebrew:

```sh
brew install node
node -v
npm -v
```

Install Appium server and the XCUITest driver:

```sh
npm install -g appium
appium driver install xcuitest
appium -v
appium driver list --installed
```

Allure is configured through Maven, so no global Allure installation is required for normal project usage:

```sh
cd java-ui-tests
mvn allure:report
```

Run API tests:

```sh
cd api-tests
mvn test
mvn allure:report
```

Start Appium:

```sh
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer \
appium --address 127.0.0.1 --port 4723
```

Run smoke tests:

```sh
cd java-ui-tests
mvn test -Psmoke \
  -Dios.device.name="iPhone" \
  -Dios.platform.version="<ios-version>" \
  -Dios.udid="<device-udid>" \
  -Dios.bundle.id="rubisoft.rabin-task-manager" \
  -Dios.derived.data.path="/tmp/rabin-task-manager-wda-derived-data" \
  -Dios.show.xcode.log="true" \
  -Dios.xcode.org.id="<apple-team-id>" \
  -Dios.xcode.signing.id="Apple Development" \
  -Dios.xcode.config.file="$PWD/wda-signing.xcconfig" \
  -Dios.updated.wda.bundle.id="<unique-wda-bundle-id>" \
  -Dios.allow.provisioning.device.registration="true" \
  -Dios.wda.launch.timeout="180000" \
  -Dios.wda.connection.timeout="180000" \
  -Dios.wait.seconds="30"
```

Run feature tests:

```sh
mvn test -Pfeature
```

Run hybrid tests:

```sh
mvn test -Phybrid
```

The hybrid suite requires a real device, Appium server, and Node.js (for the mock API, which the test starts automatically).

## Why This Project Is Interview-Ready

This is not only a set of UI tests. It shows end-to-end SDET thinking:

- The app was built with automation-friendly accessibility identifiers.
- Tests are isolated from locator details through POM.
- Business workflows are reusable and readable.
- The framework supports real-device WebDriverAgent signing and runtime configuration.
- A hybrid test validates the same business contract through both the API and the iOS UI in one run.
- Failures produce actionable diagnostics instead of only stack traces.
- Allure gives a step-by-step execution story for debugging and demos.
- CI/CD separates what can run on hosted runners from what requires physical device infrastructure.

## Repository Map

```text
.
├── .github/workflows/ios-ui-tests.yml
├── api-tests/
│   ├── pom.xml
│   ├── README.md
│   └── src/test/java/com/rabin/taskmanager/api/
├── java-ui-tests/
│   ├── pom.xml
│   ├── README.md
│   ├── src/test/java/com/rabin/taskmanager/
│   │   ├── hybrid/
│   │   └── tests/
│   └── src/test/resources/
├── mock-api/
│   ├── README.md
│   └── server.js
├── rabin-task-manager/
│   ├── ContentView.swift
│   ├── rabin_task_managerApp.swift
│   └── Assets.xcassets/
└── rabin-task-manager.xcodeproj
```
