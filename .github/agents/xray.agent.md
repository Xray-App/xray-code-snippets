---
description: 'Analyze test results and make a summary before submitting to Xray.'
tools: ['edit/createFile', 'edit/createDirectory', 'edit/editFiles', 'search', 'execute/getTerminalOutput', 'execute/runInTerminal', 'read/terminalLastCommand', 'read/terminalSelection', 'search/usages', 'read/problems', 'search/changes', 'execute/testFailure', 'web/fetch', 'execute/runTests']
---
The agent provides two main capabilities:
- analyze test results from JUnit XML files and produce a summary highlighting failed tests and probable root causes.
- upload the test execution results to Xray using Xray's REST API.

0. Running tests
To run tests, use the appropriate command for your project. For Maven projects, run:

```bash
mvn test verify
```
For Gradle projects, run:

```bash
./gradlew test
```

For other build tools, use the corresponding command to execute tests and generate JUnit XML reports.

1. Analysis of test results

Analyze the test results based on the JUnit XML file or files. DON'T run the tests unless explicitly requested and necessary.

First check in the pom.xml file if `xray-maven-plugin` is configured to generate JUnit reports based on the <reportFile> element; if so, use that path to locate the JUnit XML files. If not, use the default paths for Maven (`target/surefire-reports` and `target/failsafe-reports`) or Gradle (`build/test-results/test`).
stored in target/surefire-reports and on target/failsafe-reports, and produce a summary highlighting the failed tests and the probable root-cause for them. Analyse the current code and the previous commits to highlight the change that may have introduced the failure.

Generate a temporary JSON file named `testexecinfo.json`, overwriting it if necessary, following the structure below, and fill it with the analysis results on the "description" field. Use <project-key> as the project key placeholder; if the user hasn't mentioned it, use "ST". The description must be in Text Formatting Notation format, the one supported by Jira cloud; code blocks should be using `{code}` blocks where appropriate, while other blocks of text (non-coding related) should use the `{noformat}` for unformatted text blocks of text! Note that Jira doesn't support the typical markdown format, so you should use the format here described: https://jira.atlassian.com/secure/WikiRendererHelpAction.jspa?section=all ! It shall include sections for "Issue Summary", "Root Cause Analysis", "Resolution Steps", and "Verification Steps".



```json
{
    "fields": {
        "project": {
            "key": "<project-key>"
        },
        "summary": "Test automation results",
        "description": "<!--- Put here the analysis report -->",
        "issuetype": {
            "name": "Test Execution"
        }
    }
}
```

2. Uploading test results to Xray

This section describes how to upload the test execution results to Xray; it requires the `testexecinfo.json` file created in the previous step and requires having the JUnit XML test results available.

To identify which JUnit XML files should be uploaded,  First check in the pom.xml file if `xray-maven-plugin` is configured to generate JUnit reports based on the <reportFile> 
element; if so, use that path to locate the JUnit XML files. If not, use the default paths for Maven (`target/surefire-reports` and `target/failsafe-reports`) or Gradle (`build/test-results/test`).


To upload the test executions to Xray, check if you're using a maven project. If so, check if you have the `xray-maven-plugin` on the pom.xml and if it is then run the following command to upload the test results to Xray.

Identify first the appropriate values for `<client-id>` and `<client-secret>` for your Xray instance.
Then run the following command:

```bash
mvn xray:import-results -Dxray.clientId=<client-id> -Dxray.clientSecret=<client-secret> -Dxray.testExecInfoJson=testexecinfo.json -Dxray.reportFormat=junit -Dxray.resultFiles=<path-to-junit-xml-files>
```

Replace `<client-id>` and `<client-secret>` with the appropriate values for your Xray instance.
If the pom.xml doesn't contain the configuration for the client id and client secret, try to use environment variables to obtain the client id and secret if possible, based on CLIENT_ID and CLIENT_SECRET environment variables. 
Run the command to be executed, without any additional explanations or text.


If not a maven project, use curl to upload the test results to Xray. First, obtain an authentication token by running the following command:

```bash
curl -H "Content-Type: application/json" -X POST -d '{"client_id": "<client-id>", "client_secret": "<client secret>"}' "https://xray.cloud.getxray.app/api/v2/authenticate"
```
Replace `<client-id>` and `<client-secret>` with the appropriate values for your Xray instance.

Then, use the obtained token to upload the test results with the following command:

```bash
curl -H "Content-Type: multipart/form-data" -H "Authorization: Bearer  <auth-token>" -F 
```

