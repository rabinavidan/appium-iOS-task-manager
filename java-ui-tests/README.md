# Java UI Automation Framework

This module contains a real-device iOS automation framework for the Rabin Task Manager app. It is built as an SDET-style framework rather than a flat test suite: test intent is separated from locators, Appium actions, business flows, configuration, and reporting.

## Stack

| Tool | Purpose |
| --- | --- |
| Java 17 | Test framework language |
| Maven | Build, dependencies, profiles, Allure plugin |
| JUnit 5 | Test runner, tags, assertions |
| Appium Java Client | Mobile automation client |
| XCUITest/WebDriverAgent | iOS automation backend |
| Selenium | Waits, elements, support APIs |
| Allure | Test report, metadata, nested steps, attachments |
| Logback | Structured test logs |

## Framework Layers

```text
src/test/java/com/rabin/taskmanager/
  config/
    TestConfig.java
  driver/
    IosDriverFactory.java
  core/
    AppiumActions.java
  screens/
    TasksScreen.java
  flows/
    TaskManagerFlow.java
  tests/
    BaseIosTest.java
    TaskManagerSmokeTest.java
    TaskManagerFeatureTest.java
  reporting/
    AllureArtifacts.java
    AllureFailureExtension.java
```

| Layer | Responsibility |
| --- | --- |
| `config` | Reads runtime values from Maven/system properties |
| `driver` | Creates the Appium `IOSDriver` and XCUITest capabilities |
| `core` | Reusable actions, waits, scrolling, gestures, and timeout diagnostics |
| `screens` | Page Object Model. Owns locators and screen-level actions |
| `flows` | Business workflows consumed by tests |
| `tests` | JUnit tests, assertions, tags, and Allure metadata |
| `reporting` | Failure screenshots, page source, logs, and Allure attachments |

## Test Suites

| Suite | Maven profile | Scope |
| --- | --- | --- |
| Smoke | `-Psmoke` | Critical happy paths and filter behavior |
| Feature | `-Pfeature` | Broader functional coverage |

Smoke tests:

- Open Tasks screen.
- Add task with notes.
- Complete task and validate Done/Open filters.

Feature tests:

- Core controls visibility.
- Add button validation.
- Default Medium priority.
- High priority creation.
- Completion and reopen.
- Done/Open/All filter validation.
- Persistence after app restart.
- Single-task delete.
- Clear all tasks.

## Prerequisites

1. macOS with Xcode installed.
2. Real iPhone connected by USB, unlocked, trusted, and with Developer Mode enabled.
3. Java 17 and Maven.
4. Node.js and npm.
5. Appium and the XCUITest driver.

## Tool Installation

Verify Xcode is selected:

```sh
xcode-select -p
sudo xcode-select -s /Applications/Xcode.app/Contents/Developer
```

Install Java 17 and Maven:

```sh
brew install openjdk@17 maven
java -version
mvn -version
```

Install Node.js and npm:

```sh
brew install node
node -v
npm -v
```

Install Appium:

```sh
npm install -g appium
appium driver install xcuitest
appium -v
appium driver list --installed
```

Allure is managed by Maven in this project:

```sh
mvn allure:report
```

Start Appium:

```sh
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer \
appium --address 127.0.0.1 --port 4723
```

Before running tests, install the latest app build on the iPhone from Xcode at least once.

## Run Locally

Run smoke:

```sh
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

Run feature:

```sh
mvn test -Pfeature
```

Compile without running device tests:

```sh
mvn test -DskipTests
```

Run one test class:

```sh
mvn test -Dtest=TaskManagerFeatureTest
```

## Configuration Properties

| Property | Default | Purpose |
| --- | --- | --- |
| `appium.server.url` | `http://127.0.0.1:4723` | Appium server endpoint |
| `ios.device.name` | `iPhone` | Device name |
| `ios.platform.version` | empty | iOS version |
| `ios.udid` | empty | Real-device UDID |
| `ios.bundle.id` | `rubisoft.rabin-task-manager` | App bundle identifier |
| `ios.no.reset` | `true` | Preserve app data between sessions |
| `ios.derived.data.path` | empty | WDA DerivedData path |
| `ios.show.xcode.log` | empty | Show Xcode/WDA logs |
| `ios.xcode.org.id` | empty | Apple Developer Team ID |
| `ios.xcode.signing.id` | empty | Signing identity |
| `ios.xcode.config.file` | empty | WDA signing `.xcconfig` |
| `ios.updated.wda.bundle.id` | empty | Unique WDA bundle id |
| `ios.allow.provisioning.device.registration` | empty | Allow provisioning updates |
| `ios.wda.launch.timeout` | empty | WDA launch timeout in ms |
| `ios.wda.connection.timeout` | empty | WDA connection timeout in ms |
| `ios.wait.seconds` | default from code | Explicit wait timeout |
| `junit.tags` | empty | JUnit tag filter |

## Allure Reports

Raw Allure results:

```text
target/allure-results
```

Generate HTML report:

```sh
mvn allure:report
```

Open report locally:

```sh
mvn allure:serve
```

Generated report:

```text
target/allure-report
```

The report includes nested process-flow steps from:

- `TaskManagerFlow` business methods.
- `TasksScreen` Page Object methods.
- `AppiumActions` low-level actions.
- JUnit assertions via `allure-jupiter-assert`.

The Maven Surefire plugin runs with AspectJ because Allure uses it for `@Step` and attachment instrumentation.

## Logs And Failure Artifacts

Test log:

```text
target/logs/ui-tests.log
```

On failure, Allure attaches:

- Failure message.
- Screenshot.
- Appium page source XML.
- UI test log file.

This makes failures diagnosable from the report without immediately rerunning locally.

## CI/CD

Workflow:

```text
../.github/workflows/ios-ui-tests.yml
```

Jobs:

| Job | Runner | Purpose |
| --- | --- | --- |
| `build-and-compile-tests` | GitHub-hosted `macos-latest` | Build iOS app without signing and compile Java tests |
| `real-device-smoke` | Self-hosted Mac with iPhone | Start Appium, run selected suite, generate Allure, upload artifacts |

The real-device job is manual because physical iPhone execution requires controlled infrastructure.

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

## Troubleshooting

If WebDriverAgent fails with `xcodebuild failed with code 70`, check:

1. iPhone is unlocked.
2. USB cable is connected directly to the Mac.
3. Device is trusted and visible in Xcode `Window > Devices and Simulators`.
4. App was launched once from Xcode on the same device.
5. `DEVELOPER_DIR` points to full Xcode, not Command Line Tools.
6. WDA signing values are valid for the Apple Developer account.

Use a writable DerivedData path:

```sh
-Dios.derived.data.path="/tmp/rabin-task-manager-wda-derived-data"
```

If Appium sees the UDID but Xcode cannot use it as a destination, restart Appium from a normal Terminal session and reconnect/retrust the device.
