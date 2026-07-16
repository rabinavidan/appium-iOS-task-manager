# Rabin Task Manager - Project Context

## Current App

- Project: `rabin-task-manager`
- Platform: iOS / SwiftUI
- Main files:
  - `rabin-task-manager/ContentView.swift`
  - `rabin-task-manager/rabin_task_managerApp.swift`
- The app is a local task manager.
- Current features:
  - Add task title
  - Add notes
  - Set priority: Low, Medium, High
  - Set due date
  - Mark task complete/incomplete
  - Filter tasks by All, Open, Done
  - Delete tasks with list delete gesture
  - Clear all tasks with a toolbar action and confirmation
  - Persist tasks locally with `@AppStorage`

## Installation History

- The app was built and installed on a real iPhone.
- Developer Mode was initially hard to find on the iPhone.
- The practical flow was:
  - Connect iPhone to Mac
  - Run from Xcode
  - Enable Developer Mode if prompted
  - Trust the developer profile under iPhone settings if needed
- The app eventually opened successfully and worked correctly on the iPhone.

## Build Validation

- A generic iOS device build succeeded with:

```sh
xcodebuild -scheme rabin-task-manager -configuration Debug -destination 'generic/platform=iOS' -derivedDataPath /private/tmp/rabin-task-manager-derived-data CODE_SIGNING_ALLOWED=NO build
```

- The SwiftUI preview block was removed because the sandbox could not execute the preview macro plugin during command-line build validation.

## Testing Direction

- The requested testing direction is Java UI automation that tests the app like a real user on an iPhone.
- Chosen stack:
  - Appium
  - Appium XCUITest driver
  - Java
  - JUnit 5
  - Maven

## Accessibility Work

`ContentView.swift` now includes accessibility identifiers for UI automation:

- `tasks-screen`
- `task-input-panel`
- `task-title-field`
- `task-notes-field`
- `priority-picker`
- `due-date-picker`
- `add-task-button`
- `task-filter-picker`
- `filter-all`
- `filter-open`
- `filter-done`
- `task-list`
- `empty-state`
- `open-task-count`
- Dynamic task row identifiers:
  - `task-row-{uuid}`
  - `complete-task-{uuid}`
  - `task-title-{uuid}`
  - `task-notes-{uuid}`
  - `task-priority-{uuid}`
  - `task-due-date-{uuid}`

Completion buttons also expose labels with the task title, for example:

- `Mark Buy milk complete`
- `Mark Buy milk incomplete`

## Java UI Test Plan

Initial tests:

1. App launch
   - Open the app through Appium.
   - Verify the `Tasks` screen appears.

2. Add task
   - Enter a unique task name.
   - Enter notes.
   - Tap Add Task.
   - Verify the task appears in the list.

3. Complete task and filter
   - Create a unique task.
   - Mark it complete.
   - Switch to Done.
   - Verify the task appears.
   - Switch to Open.
   - Verify the task does not appear.

Next tests to add:

- Priority selection
- Due date selection
- Delete task by swipe
- Persistence after app relaunch
- Multiple task ordering

## How To Run Java UI Tests

See `java-ui-tests/README.md`.

## Latest Appium Run

- Appium was started locally with `npx`.
- Server URL: `http://127.0.0.1:4723`
- Appium version: `3.5.2`
- XCUITest driver version: `11.17.7`
- Server status returned ready.
- First Maven test run reached Appium but failed to create a session because no real-device UDID was provided.
- Error summary: Appium tried to resolve an iPhone Simulator SDK and failed with `SDK "iphonesimulator" cannot be located`.
- A later run used `F17F90CG0D98`, but Appium returned `Unknown device or simulator UDID`.
- The user then provided the correct real-device UDID:
  - `00008101-000209223A21001E`
- Running without `DEVELOPER_DIR` failed because the system was using CommandLineTools instead of Xcode:
  - `/Library/Developer/CommandLineTools is not a valid Xcode path`
- Appium was restarted with:

```sh
DEVELOPER_DIR=/Applications/Xcode.app/Contents/Developer APPIUM_HOME=/private/tmp/rabin-task-manager-appium-home npm_config_cache=/private/tmp/rabin-task-manager-npm-cache npx --yes appium --address 127.0.0.1 --port 4723
```

- With the correct UDID and Xcode path, Appium reached the real iPhone and started preparing WebDriverAgent.
- WebDriverAgent then failed with `xcodebuild failed with code 70`.
- The visible cause was permission errors writing WDA DerivedData under:
  - `/Users/root26/Library/Developer/Xcode/DerivedData/WebDriverAgent-.../Logs/...`
- The Java test base now supports optional Appium capabilities for:
  - `ios.derived.data.path`
  - `ios.show.xcode.log`
  - `ios.wda.startup.retries`
  - `ios.wda.startup.retry.interval`
  - `ios.xcode.org.id`
  - `ios.xcode.signing.id`
  - `ios.updated.wda.bundle.id`
- Next run should pass:

```sh
-Dios.derived.data.path="/private/tmp/rabin-task-manager-wda-derived-data" -Dios.show.xcode.log="true"
```

- If WebDriverAgent still fails after using the writable DerivedData path, the next likely issue is real-device signing for WebDriverAgent. In that case, provide an Apple Developer Team ID and use `ios.xcode.org.id`, `ios.xcode.signing.id`, and `ios.updated.wda.bundle.id`.

## Latest WebDriverAgent Diagnosis

- Java test compilation succeeds:

```sh
mvn -Dmaven.repo.local=/private/tmp/rabin-task-manager-m2 test -DskipTests
```

- A focused real-device run was attempted with:
  - `ios.udid=00008101-000209223A21001E`
  - `ios.platform.version=26.5`
  - `ios.derived.data.path=/private/tmp/rabin-task-manager-wda-derived-data`
  - `ios.show.xcode.log=true`
- The writable DerivedData path is working.
- Appium can see the iPhone UDID through its device layer:
  - `Available real devices: 00008101-000209223A21001E`
- The blocking failure is now from `xcodebuild`:

```text
xcodebuild: error: Unable to find a device matching the provided destination specifier:
    { id:00008101-000209223A21001E }
The requested device could not be found because no available devices matched the request.
```

- This means Appium sees the physical device, but Xcode/CoreDevice cannot use it as a test destination for WebDriverAgent from the current command environment.
- Attempts to inspect devices with `xcrun devicectl list devices` also failed with CoreDevice initialization timeout in this environment.
- Starting Appium with a temporary `HOME` did not fix the CoreDevice/CoreSimulator failure.
- Next practical step: start Appium from a normal macOS Terminal outside the restricted Codex/Xcode shell, then rerun Maven from the test folder. If the same error happens outside Codex, unlock/retrust the iPhone in Xcode `Window > Devices and Simulators`, run the app once from Xcode, and retry.

## Latest Test Stabilization

- WebDriverAgent signing progressed after passing:
  - `ios.xcode.org.id=5J4252D7AH`
  - `ios.xcode.signing.id=Apple Development`
  - `ios.xcode.config.file=java-ui-tests/wda-signing.xcconfig`
  - `ios.updated.wda.bundle.id=com.rabintaskmanager.WebDriverAgentRunner`
  - `ios.allow.provisioning.device.registration=true`
- A later run reached app interaction, but still had:
  - WebDriverAgent `/status` timeout after 60000 ms
  - one UI lookup timeout for `task-title-field`
- Java test infrastructure was updated to:
  - reuse one Appium session per test class instead of starting WDA for every test method
  - support `ios.wda.launch.timeout`
  - support `ios.wda.connection.timeout`
  - support `ios.wait.seconds`
  - use fallback lookup for text fields by placeholder text if accessibility id is not found
- Recommended next command includes:
  - `-Dios.wda.launch.timeout="180000"`
  - `-Dios.wda.connection.timeout="180000"`
  - `-Dios.wait.seconds="30"`

## Expanded Java UI Coverage

- Added `TaskManagerFeatureTest.java` for broader functional coverage.
- Current Java/Appium coverage now includes:
  - screen launch
  - core controls visible
  - Add Task disabled until a title exists
  - add task with notes
  - default Medium priority
  - High priority task creation
  - complete task
  - reopen task
  - Done/Open/All filters
  - persistence after app restart
  - delete task from list
  - clear all tasks from list
- Shared Appium helper coverage now includes:
  - one Appium session per test class
  - robust lookup by accessibility id, text, placeholder, or element type
  - useful page-source excerpts in timeout failures
  - WDA signing and launch timeout capabilities
  - app restart helper
  - list deletion by mobile swipe
- Latest fix after broader run:
  - new tasks were created but not visible because the list already had many tasks and new items could be off-screen
  - helpers now scroll to target text before asserting visibility or deleting
  - keyboard is hidden before tapping Add Task
  - priority control lookup now uses visible value `Medium` instead of label `Priority`
- Follow-up fix:
  - Appium `mobile: scroll` with predicate did not move the SwiftUI list reliably
  - `scrollToText` now performs repeated physical `mobile: swipe` gestures upward and checks for the target text after each swipe

## Java Automation Architecture

- The Java/Appium test project now follows a layered automation model:
  - `config` - reads runtime configuration from Maven/system properties
  - `driver` - creates the Appium iOS driver and XCUITest capabilities
  - `core` - low-level Appium actions, waits, swipes, scrolling, and timeout diagnostics
  - `screens` - Page Object Model layer, currently `TasksScreen`
  - `flows` - business flows for task creation, completion, reopening, restart, and delete
  - `tests` - JUnit test classes only, separated from POM/actions logic
  - `reporting` - Allure attachments and log capture
- Test packages were moved under:
  - `java-ui-tests/src/test/java/com/rabin/taskmanager/tests`
- Existing tests now use:
  - `TasksScreen`
  - `TaskManagerFlow`
  - `BaseIosTest`
- `BaseIosTest` now tries to clear remaining task data in `@AfterAll` before quitting the Appium driver.

## Allure Reports And Logs

- Maven dependencies now include:
  - `io.qameta.allure:allure-jupiter`
  - `ch.qos.logback:logback-classic`
- Allure raw results:
  - `java-ui-tests/target/allure-results`
- Generated Allure report:
  - `java-ui-tests/target/allure-report`
- Test logs:
  - `java-ui-tests/target/logs/ui-tests.log`
- On test failure, Allure attaches:
  - failure message
  - screenshot
  - page source XML
  - UI test log file
- Allure process-flow steps are now generated from:
  - `@Step` annotations in `TaskManagerFlow`
  - `@Step` annotations in `TasksScreen`
  - `@Step` annotations in `AppiumActions`
  - `allure-jupiter-assert` for assertion steps
- Maven Surefire uses AspectJ javaagent because Allure requires AspectJ for `@Step` and `@Attachment` annotation support.
- Maven profiles:
  - `-Psmoke` runs tests tagged `smoke`
  - `-Pfeature` runs tests tagged `feature`
- CI/CD:
  - `.github/workflows/ios-ui-tests.yml`
  - `build-and-compile-tests` runs on GitHub-hosted `macos-latest` and validates the Xcode build plus Java test compilation.
  - `real-device-smoke` runs manually on a self-hosted Mac with an unlocked iPhone and uploads Allure artifacts.
- Report commands:

```sh
mvn allure:report
mvn allure:serve
```

- Java test compilation was validated successfully with:

```sh
mvn -Dmaven.repo.local=/private/tmp/rabin-task-manager-m2 test -DskipTests
```

## Latest JUnit Discovery Fix

- A smoke run failed before Appium interaction with:

```text
TestEngine with ID 'junit-jupiter' failed to discover tests
OutputDirectoryProvider not available
```

- Root cause:
  - `junit-jupiter` used JUnit Platform `1.12.2`
  - Allure transitively pulled `junit-platform-launcher` `1.10.3`
  - JUnit engine and launcher were not aligned
- Fix:
  - added direct dependency on `org.junit.platform:junit-platform-launcher:1.12.2`
- Verification:
  - JUnit discovery now reaches `Running com.rabin.taskmanager.tests.TaskManagerSmokeTest`
  - A forced bad Appium URL fails later with `Connection refused`, confirming discovery is fixed
