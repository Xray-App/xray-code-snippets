---
name: java-test-runner
description: Runs Java tests, mapped to Tests in Xray, using Maven Surefire and Failsafe
allowed-tools: Bash(mvn:*, mvnw:*, find:*, grep:*, cat:*)
---

# Running Java Tests by Classname and Method Name based on Test Automation IDs stored in Xray

This skill enables discovery of automated Tests based on a user specified Test Plan issue key or Test Execution issue key, and then running those tests by classname and method name using Maven Surefire and Failsafe plugins.


## Flow

When a user asks to run Java tests, follow these steps:

1. **Obtain the test automation ids**: Obtain the classnames and method names of the tests to be executed, based on the test automation id of the related tests in Xray. Use the `obtaining-tests` skill to obtain the list of tests with their automation ids, and then filter the tests based on the test automation ids specified by the user, to get the classnames and method names of the tests to be executed.
3. **Identify test type**: Identify the type of tests to be executed (unit tests or integration tests) based on the classname pattern (e.g., `*Test` for unit tests and `*IT` for integration tests), to determine whether to use Maven Surefire or Failsafe plugin for execution.
4. **Prepare automation ids**: Prepare the test automation ids to be used in the Maven command, by converting the classnames and method names into the appropriate format for Maven Surefire and Failsafe plugins.
5. **Run tests**: Run the specific test(s) using the appropriate Maven command
6. **Report results**: Show the test execution output and indicate success/failure


### Prepare the test automation ids for Maven Surefire and Failsafe

To prepare the test automation ids for Maven Surefire and Failsafe, follow these steps:
1. For each test automation id, extract the classname and method name. In Xray, test automation ids are typically in the format of `com.example.tests.UserServiceUnitTest.existsReturnsTrueIfExisting`, where the package is `com.example.tests`, the classname is `UserServiceUnitTest`, and the method name is `existsReturnsTrueIfExisting`. So, for each test automation id, split it by the last dot (`.`) to separate the classname and method name. In this example, the classname would be `UserServiceUnitTest` and the method name would be `existsReturnsTrueIfExisting`.
2. Convert the classname and method name into the appropriate format for Maven Surefire and Failsafe plugins, which is `package.classname#methodname` for running a specific test method, and `classname` for running all tests in a class.

### Running Tests

Prefer to use `mvnw` (Maven Wrapper) if available in the project (**always** check before running if `mvnw` is available), to ensure consistent Maven version across different environments. If `mvnw` is not available, fallback to using `mvn` (it has the same syntax).

**Run a specific unit/surefire test:**
```bash
./mvnw test -Dtest=UserServiceUnitTest#existsReturnsTrueIfExisting
```

**Run a specific integration (failsafe) test:**
```bash
./mvnw integration-test -Dit.test=UserRestControllerIT#createUserWithSuccess
```

**Run several tests at once (example for surefire and failsafe) - preferred approach:**
```bash
./mvnw test -Dtest=UserManagerUnitTest#existsReturnsTrueIfExisting,UserManagerUnitTest#existsReturnsFalseIfNotExisting
./mvnw integration-test -Dit.test=UserRestControllerIT#createUserWithSuccess,UserRestControllerIT#removeUserWithSuccess
```

**Run with less verbose output:**
```bash
./mvnw test -Dtest=TestClassName --quiet
```

## Test Types

This project uses two types of tests:

- **Unit Tests**: Files ending with `*Test.java` (e.g., `UserServiceUnitTest.java`)
  - Run via Maven Surefire plugin: `./mvnw test -Dtest=...`
  - Reports in `target/surefire-reports/`

- **Integration Tests**: Files ending with `*IT.java` (e.g., `UserRestControllerIT.java`)
  - Run via Maven Failsafe plugin: `./mvnw integration-test -Dit.test=...`
  - Reports in `target/failsafe-reports/`

## Important Notes

- Always run commands from the **project root** directory (where `pom.xml` is located)
- Use Maven wrapper `./mvnw` if available, fallback to `mvn` if needed
- When running integration tests with Failsafe, **never** run the `verify` phase, as it will run both unit and integration tests; instead, run only the `integration-test` phase to run only the integration tests
- Exit code 0 indicates success, non-zero indicates test failure

## Dos

- Whenever checking the test results, make sure you check the test reports in `target/surefire-reports/` for unit tests and `target/failsafe-reports/` for integration tests, to get detailed information about the test execution, including which tests passed or failed, and any error messages or stack traces for failed tests. Make sure you're not looking at previous test execution results, but the most recent ones, to ensure you're checking the correct test results.
- If a test doesn't have a test automation id in Xray, it should not be executed, even if it matches the classname pattern for unit or integration tests. Only run tests that have a corresponding test automation id in Xray, as specified by the user.

## Don'ts

- Never use other build tools (Gradle, Ant) - this skill is Maven-specific
- Never run tests that were not part of the obtained list of tests based on the test automation ids from Xray; run *only* the tests based on the corresponding test automation ids obtained from Xray, as specified by the user
```
## Output format

Example of output format when the user asks for all tests of a given test plan or test execution:

```
✅ Test results
All 5 tests from ST-297 PASSED successfully:

✅ ST-6 - UserRestControllerIT.createUserWithSuccess (0.026s)
✅ ST-7 - UserRestControllerIT.deleteUserUnsuccess (0.031s)
✅ ST-9 - IndexControllerIT.getWelcomeMessage (1.926s)
✅ ST-10 - UserRestControllerIT.listAllUsersWithSuccess (0.033s)
✅ ST-11 - UserRestControllerIT.getUserUnsuccess (0.045s)
```