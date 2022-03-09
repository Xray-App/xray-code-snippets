# Importing test automation results

## Background

Importing results is accomplished by using the REST APIs.
There are some subtle differences between [Xray server/DC REST API](https://docs.getxray.app/display/XRAY/REST+API) and [Xray Cloud REST API](https://docs.getxray.app/display/XRAYCLOUD/REST+API), including authentication mechanisms and also on the request iself.

First, Xray supports importing test automation results in different formats, including JUnit XML, TestNG XML, Robot Framework XML, Cucumber JSON, Behave JSON, etc.
Xray also has a specific proprietary format named "Xray JSON" that can be used to import results; this format has some subtle differences between Xray server/DC and Xray cloud, so please check the documentation.
The information that can be processed from these report formats differs a bit due to their nature.

For all these formats, there are specific endpoints that can be used to submit the test automation results.

**There are in fact two endpoints per each format**:
- a "standard" one, that is simpler to use and provides the ability to specify common parameters to better identify what we aim to report against (e.g. project, version, Test Plan, Test Environment). Usually, these endpoint URLs end with ".../import/execution/<format>", with the exception of Xray JSON format that ends up with ".../import/execution";
- a "multipart" one, which is a bit more low-level; its purpose is to allow full customization of the fields of the Test Execution that will be created (and eventually also on the Test issues). Usually, these endpoint URLs end with ".../import/execution/<format>/multipart", with the exception of Xray JSON format that ends up with ".../import/execution/multipart";

To keep it simple, you can use the so called "standard" endpoints, as their syntax is simpler and it fits most scenarios.

To be able to import Cucumber JSON reports (or Behave JSON reports), we need to make sure the Scenarios are properly tagged with the correspoding Test issue keys in Jira. That can be done automatic if we generate the .feature file(s) by exporting them out of Jira before running the tests, accordingly with the [supported flows](https://docs.getxray.app/pages/viewpage.action?pageId=62267221#TestinginBDDwithGherkinbasedframeworks(e.g.Cucumber)-Workflows) detailed in Xray documentation. If Scenario/Scenario Outline are not tagged, then results cannot be imported as they need to be mapped to existing Test issues in Xray.

Please check this repository, as it not onlu provides the code samples shown in this page but even more.
See examples for:

- [Java](java)
- [JavaScript](js)
- [Python](python)

The Maven-based Java project code available in the repo contains a [possible implementation](java/xray-code-snippets/src/main/java/com/xblend/xray/XrayResultsImporter.java) of a client library to import results, supporting multiple formats and the available endpoints, and also both Xray server/DC and Cloud.

## Code snippets

### Importing results using the Xray JSON format

Xray has a specific proprietary format named "Xray JSON" that can be used to import results; this format has some subtle differences between [Xray server/DC](https://docs.getxray.app/display/XRAY/Import+Execution+Results#ImportExecutionResults-XrayJSONformat) and [Xray cloud](https://docs.getxray.app/display/XRAYCLOUD/Using+Xray+JSON+format+to+import+execution+results#UsingXrayJSONformattoimportexecutionresults-XrayJSONformat), so please check the proper technical documentation. Besides, some features/fields may only be available in a specific version of the endpoint (e.g., Test Run custom fields are only available on v2 of the REST API endpoint).

In this repo, you can see one [example of contents for the Xray JSON in Xray server/DC](java/xray-code-snippets/src/main/resources/xray_dc.json) and another [example of contents for the Xray JSON as supported by Xray cloud](java/xray-code-snippets/src/main/resources/xray_cloud.json).

The following code snippets make use of a prebuilt Xray JSON content; however, you could also build out this JSON programmaticaly.

#### Java

Please check the [possible implementation](java/xray-code-snippets/src/main/java/com/xblend/xray/XrayResultsImporter.java) of a Java client library.

You can find these, or similar, [examples in a sample ImportResultsExamples class](java/xray-code-snippets/src/main/java/com/xblend/xray/ImportResultsExamples.java).

##### Xray server/DC

```java
final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

System.out.println("Importing a Xray JSON report to a Xray Server/Data Center instance...");

OkHttpClient client = new OkHttpClient();
String credentials;
if (jiraPersonalAccessToken!= null) {
    credentials = "Bearer " + jiraPersonalAccessToken;
} else {
    credentials = Credentials.basic(jiraUsername, jiraPassword);
} 

String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution";
RequestBody requestBody = null;
try {
    String reportContent = new String ( Files.readAllBytes( Paths.get(reportFile) ) );
    requestBody = RequestBody.create(reportContent, MEDIA_TYPE_JSON);
} catch (Exception e1) {
    e1.printStackTrace();
    throw e1;
}
Request request = new Request.Builder().url(endpointUrl).post(requestBody).addHeader("Authorization", credentials).build();
Response response = null;
try {
    response = client.newCall(request).execute();
    String responseBody = response.body().string();
    if (response.isSuccessful()){
        JSONObject responseObj = new JSONObject(responseBody);
        System.out.println("Test Execution: "+((JSONObject)(responseObj.get("testExecIssue"))).get("key"));
        return(responseBody);
    } else {
        throw new IOException("Unexpected HTTP code " + response);
    }
} catch (IOException e) {
    e.printStackTrace();
    throw(e);
}
```

If you use the sample client library code, it could become as simple as:

```java
XrayResultsImporter xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).build();
// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).build();
String response = xrayImporter.submit(XrayResultsImporter.XRAY_FORMAT, xrayJsonDCReport);
```

##### Xray Cloud

```java
final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

String clientId = System.getenv().getOrDefault("CLIENT_ID", "215FFD69FE4644728C72182E00000000");
String clientSecret = System.getenv().getOrDefault("CLIENT_SECRET", "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000");
String xrayCloudApiBaseUrl = "https://xray.cloud.getxray.app/api/v2";
String authenticateUrl = xrayCloudApiBaseUrl + "/authenticate";

System.out.println("Importing a Xray JSON report to a Xray Cloud instance...");

OkHttpClient client = new OkHttpClient();
String authenticationPayload = "{ \"client_id\": \"" + clientId +"\", \"client_secret\": \"" + clientSecret +"\" }";
RequestBody body = RequestBody.create(authenticationPayload, MEDIA_TYPE_JSON);
Request request = new Request.Builder().url(authenticateUrl).post(body).build();
Response response = null;
String authToken = null;
try {
    response = client.newCall(request).execute();
    String responseBody = response.body().string();
    if (response.isSuccessful()){
        authToken = responseBody.replace("\"", "");	
    } else {
        throw new IOException("failed to authenticate " + response);
    }
} catch (IOException e) {
    e.printStackTrace();
    throw e;
}
String credentials = "Bearer " + authToken;

String endpointUrl =  xrayCloudApiBaseUrl + "/import/execution"; 
RequestBody requestBody = null;
try {
    String reportContent = new String ( Files.readAllBytes( Paths.get(reportFile) ) );
    requestBody = RequestBody.create(reportContent, MEDIA_TYPE_JSON);
} catch (Exception e1) {
    e1.printStackTrace();
    throw e1;
}

request = new Request.Builder().url(endpointUrl).post(requestBody).addHeader("Authorization", credentials).build();
response = null;
try {
    response = client.newCall(request).execute();
    String responseBody = response.body().string();
    if (response.isSuccessful()){
        JSONObject responseObj = new JSONObject(responseBody);
        System.out.println("Test Execution: " + responseObj.get("key"));
        return(responseBody);
    } else {
        throw new IOException("Unexpected HTTP code " + response);
    }
} catch (IOException e) {
    e.printStackTrace();
    throw e;
}
```

If you use the sample client library code, it could become as simple as:

```java
XrayResultsImporter xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).build();
String response = xrayImporter.submit(XrayResultsImporter.XRAY_FORMAT, xrayJsonCloudReport);
```

#### JavaScript

##### Xray server/DC

```javascript
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var jira_base_url = "http://192.168.56.102";
var personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY";

//var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);

const report_content = fs.readFileSync("xray_dc.json").toString();
console.log(report_content);

var endpoint_url = jira_base_url + "/rest/raven/2.0/import/execution";
axios.post(endpoint_url, report_content, {
    //headers: { 'Authorization': basicAuth, "Content-Type": "application/json" }
    headers: { 'Authorization': "Bearer " + personal_access_token, "Content-Type": "application/json" }
}).then(function(response) {
    console.log('success');
    console.log(response.data.testExecIssue.key);
}).catch(function(error) {
    console.log('Error submiting results: ' + error);
});
```

##### Xray Cloud

```javascript
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

var authenticate_url = xray_cloud_base_url + "/authenticate";

axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    console.log('success');
    var auth_token = response.data;
    console.log("AUTH: " + auth_token);

    const report_content = fs.readFileSync("xray_cloud.json").toString();
    console.log(report_content);

    var endpoint_url = xray_cloud_base_url + "/import/execution";
    axios.post(endpoint_url, report_content, {
        headers: { 'Authorization': "Bearer " + auth_token, "Content-Type": "application/json" }
    }).then(function(res) {
        console.log('success');
        console.log(res.data.key);
    }).catch(function(error) {
        console.log('Error submiting results: ' + error);
    });
}).catch( (error) => {
    console.log('Error on Authentication: ' + error);
});
```

#### Python

##### Xray server/DC

```python
import requests
from base64 import urlsafe_b64encode as b64e

jira_base_url = "http://192.168.56.102"
jira_username = "admin"
jira_password = "admin"
personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY"


# endpoint doc for importing Xray JSON reports: https://docs.getxray.app/display/XRAY/Import+Execution+Results+-+REST#ImportExecutionResultsREST-XrayJSONresults
report_content = open(r'xray_dc.json', 'rb')

# importing results using HTTP basic authentication
# response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution', params=params, data=report_content, auth=(jira_username, jira_password), headers=headers)

# importing results using Personal Access Tokens
headers = {'Authorization': 'Bearer ' + personal_access_token, 'Content-Type': 'application/json'}
response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution', data=report_content, headers=headers)

print(response.status_code)
print(response.content)
```

##### Xray Cloud

```python
import requests
import json
import os

xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2"
client_id = os.getenv('CLIENT_ID', "215FFD69FE4644728C72182E00000000")
client_secret = os.getenv('CLIENT_SECRET',"1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000")

# endpoint doc for authenticating and obtaining token from Xray Cloud: https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2
headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
auth_data = { "client_id": client_id, "client_secret": client_secret }
response = requests.post(f'{xray_cloud_base_url}/authenticate', data=json.dumps(auth_data), headers=headers)
auth_token = response.json()
# print(auth_token)

# endpoint doc for importing Xray JSON reports: https://docs.getxray.app/display/XRAYCLOUD/Import+Execution+Results+-+REST#ImportExecutionResultsREST-XrayJSONresults
report_content = open(r'xray_cloud.json', 'rb')
headers = {'Authorization': 'Bearer ' + auth_token, 'Content-Type': 'application/json'}
response = requests.post(f'{xray_cloud_base_url}/import/execution', data=report_content, headers=headers)

print(response.status_code)
print(response.content)
```

### Importing results from JUnit to a given Jira project, identified by its key, for a specific version/release

This example shows how to either use HTTP basic authentication or Personal Access tokens.
It uses the "standard" JUnit endpoint provided by Xray for this purpose.
If using TestNG, Robot Framework, NUnit, or xUnit reports, the request would be similar.

#### Java

Please check the [possible implementation](java/xray-code-snippets/src/main/java/com/xblend/xray/XrayResultsImporter.java) of a Java client library.

You can find these, or similar, [examples in a sample ImportResultsExamples class](java/xray-code-snippets/src/main/java/com/xblend/xray/ImportResultsExamples.java).

##### Xray server/DC

```java
final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");

String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

String projectKey = "CALC";
String fixVersion = "v1.0";
String revision = null;
String testPlanKey = "CALC-8895";
String testEnvironment = "chrome";

System.out.println("Importing a JUnit XML report to a Xray Server/Data Center instance...");

OkHttpClient client = new OkHttpClient();
String credentials;
if (jiraPersonalAccessToken!= null) {
    credentials = "Bearer " + jiraPersonalAccessToken;
} else {
    credentials = Credentials.basic(jiraUsername, jiraPassword);
} 

String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution/junit"; 
MultipartBody requestBody = null;
try {
    requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", reportFile, RequestBody.create(new File(reportFile), MEDIA_TYPE_XML))
            .build();
} catch (Exception e1) {
    e1.printStackTrace();
    throw e1;
}

HttpUrl url = HttpUrl.get(endpointUrl);
HttpUrl.Builder builder = url.newBuilder();
if (projectKey != null) {
    builder.addQueryParameter("projectKey", projectKey);
}
if (fixVersion != null) {
    builder.addQueryParameter("fixVersion", fixVersion);
}
if (revision != null) {
    builder.addQueryParameter("revision", revision);
}
if (testPlanKey != null) {
    builder.addQueryParameter("testPlanKey", testPlanKey);
}
if (testEnvironment != null) {
    builder.addQueryParameter("testEnvironment", testEnvironment);
}        

Request request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
Response response = null;
try {
    response = client.newCall(request).execute();
    String responseBody = response.body().string();
    if (response.isSuccessful()){
        JSONObject responseObj = new JSONObject(responseBody);
        System.out.println("Test Execution: "+((JSONObject)(responseObj.get("testExecIssue"))).get("key"));
        return(responseBody);
    } else {
        throw new IOException("Unexpected HTTP code " + response);
    }
} catch (IOException e) {
    e.printStackTrace();
    throw(e);
}
```

##### Xray Cloud

```java
final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");

String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

String projectKey = "CALC";
String fixVersion = "v1.0";
String revision = null;
String testPlanKey = "CALC-8895";
String testEnvironment = "chrome";

System.out.println("Importing a JUnit XML report to a Xray Server/Data Center instance...");

OkHttpClient client = new OkHttpClient();
String credentials;
if (jiraPersonalAccessToken!= null) {
    credentials = "Bearer " + jiraPersonalAccessToken;
} else {
    credentials = Credentials.basic(jiraUsername, jiraPassword);
} 

String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution/junit"; 
MultipartBody requestBody = null;
try {
    requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", reportFile, RequestBody.create(MEDIA_TYPE_XML, new File(reportFile)))
            .build();
} catch (Exception e1) {
    e1.printStackTrace();
    throw e1;
}

HttpUrl url = HttpUrl.get(endpointUrl);
HttpUrl.Builder builder = url.newBuilder();
if (projectKey != null) {
    builder.addQueryParameter("projectKey", projectKey);
}
if (fixVersion != null) {
    builder.addQueryParameter("fixVersion", fixVersion);
}
if (revision != null) {
    builder.addQueryParameter("revision", revision);
}
if (testPlanKey != null) {
    builder.addQueryParameter("testPlanKey", testPlanKey);
}
if (testEnvironment != null) {
    builder.addQueryParameter("testEnvironment", testEnvironment);
}

Request request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
Response response = null;
try {
    response = client.newCall(request).execute();
    String responseBody = response.body().string();
    if (response.isSuccessful()){
        JSONObject responseObj = new JSONObject(responseBody);
        System.out.println("Test Execution: "+((JSONObject)(responseObj.get("testExecIssue"))).get("key"));
        return(responseBody);
    } else {
        throw new IOException("Unexpected HTTP code " + response);
    }
} catch (IOException e) {
    e.printStackTrace();
    throw(e);
}
```

If you use the sample client library code, it could become as simple as:

```java
XrayResultsImporter xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).withProjectKey("CALC").withVersion("1.0").withTestPlanKey("CALC-8895")..withTestEnvironment("chrome").build();
// XrayResultsImporter xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).withProjectKey("CALC").withVersion("1.0").withTestPlanKey("CALC-8895").withTestEnvironment("chrome").build();
String response = xrayImporter.submit(XrayResultsImporter.JUNIT_FORMAT, junitReport);
````

##### Xray Cloud

```java
final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");

String clientId = System.getenv().getOrDefault("CLIENT_ID", "215FFD69FE4644728C72182E00000000");
String clientSecret = System.getenv().getOrDefault("CLIENT_SECRET", "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000");
String xrayCloudApiBaseUrl = "https://xray.cloud.getxray.app/api/v2";
String authenticateUrl = xrayCloudApiBaseUrl + "/authenticate";

String projectKey = "CALC";
String fixVersion = "v1.0";
String revision = null;
String testPlanKey = "CALC-8895";
String testEnvironment = "chrome";

System.out.println("Importing a JUnit XML report to a Xray Cloud instance...");

OkHttpClient client = new OkHttpClient();
String authenticationPayload = "{ \"client_id\": \"" + clientId +"\", \"client_secret\": \"" + clientSecret +"\" }";
RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, authenticationPayload);
Request request = new Request.Builder().url(authenticateUrl).post(body).build();
Response response = null;
String authToken = null;
try {
    response = client.newCall(request).execute();
    String responseBody = response.body().string();
    if (response.isSuccessful()){
        authToken = responseBody.replace("\"", "");
    } else {
        throw new IOException("failed to authenticate " + response);
    }
} catch (IOException e) {
    e.printStackTrace();
    throw e;
}
String credentials = "Bearer " + authToken;

String endpointUrl =  xrayCloudApiBaseUrl + "/import/execution/junit"; 
RequestBody requestBody = null;
try {
    String reportContent = new String ( Files.readAllBytes( Paths.get(reportFile) ) );
    requestBody = RequestBody.create(MEDIA_TYPE_XML, reportContent);
} catch (Exception e1) {
    e1.printStackTrace();
    throw e1;
}

HttpUrl url = HttpUrl.get(endpointUrl);
HttpUrl.Builder builder = url.newBuilder();
if (projectKey != null) {
    builder.addQueryParameter("projectKey", projectKey);
}
if (fixVersion != null) {
    builder.addQueryParameter("fixVersion", fixVersion);
}
if (revision != null) {
    builder.addQueryParameter("revision", revision);
}
if (testPlanKey != null) {
    builder.addQueryParameter("testPlanKey", testPlanKey);
}
if (testEnvironment != null) {
    builder.addQueryParameter("testEnvironment", testEnvironment);
} 

request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
response = null;
try {
    response = client.newCall(request).execute();
    String responseBody = response.body().string();
    if (response.isSuccessful()){
        JSONObject responseObj = new JSONObject(responseBody);
        System.out.println("Test Execution: " + responseObj.get("key"));
        return(responseBody);
    } else {
        throw new IOException("Unexpected HTTP code " + response);
    }
} catch (IOException e) {
    e.printStackTrace();
    throw e;
}
```

If you use the sample client library code, it could become as simple as:

```java
XrayResultsImporter xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).withProjectKey("CALC").withVersion("1.0").withTestPlanKey("CALC-8895").withTestEnvironment("chrome").build();
String response = xrayImporter.submit(XrayResultsImporter.JUNIT_FORMAT, junitReport);
````

#### JavaScript

In JavaScript, there are a bunch of HTTP client libraries (e.g. axios, request, superagent) that can be used to build our code.
The following examples make use of [axios](https://www.npmjs.com/package/axios).
  
You can have a look at some JavaScript files with these examples [in the repo](use_cases/import_automation_results/js). You need to run `npm install` before running them, to install the dependencies. Don''t forget to customize the variables directly in the source-code, to specify the Xray/Jira credentials among other.

##### Xray server/DC

```javascript
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var jira_base_url = "http://192.168.56.102";
var personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY";

//var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);

const report_content = fs.readFileSync("junit.xml").toString();
console.log(report_content);

var bodyFormData = new FormData();
bodyFormData.append('file', report_content, 'junit.xml'); 

var endpoint_url = jira_base_url + "/rest/raven/2.0/import/execution/junit";
const params = new URLSearchParams({
    projectKey: "CALC"
}).toString();
const url = endpoint_url + "?" + params;


axios.post(url, bodyFormData, {
    //headers: { 'Authorization': basicAuth, ...bodyFormData.getHeaders() }
    headers: { 'Authorization': "Bearer " + personal_access_token, ...bodyFormData.getHeaders() }
}).then(function(response) {
    console.log('success');
    console.log(response.data.testExecIssue.key);
}).catch(function(error) {
    console.log('Error submiting results: ' + error);
});
```

##### Xray Cloud

```javascript
import requests
import json

xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2"
client_id = "215FFD69FE4644728C72182E00000000"
client_secret = "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000"

# endpoint doc for authenticating and obtaining token from Xray Cloud: https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2
headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
auth_data = { "client_id": client_id, "client_secret": client_secret }
response = requests.post(f'{xray_cloud_base_url}/authenticate', data=json.dumps(auth_data), headers=headers)
auth_token = response.json()
print(auth_token)

# endpoint doc for importing JUnit XML reports: https://docs.getxray.app/display/XRAYCLOUD/Import+Execution+Results+-+REST#ImportExecutionResultsREST-JUnitXMLresults
params = (('projectKey', 'BOOK'),('fixVersion','1.0'))
report_content = open(r'junit.xml', 'rb')
headers = {'Authorization': 'Bearer ' + auth_token, 'Content-Type': 'application/xml'}
response = requests.post(f'{xray_cloud_base_url}/import/execution/junit', params=params, data=report_content, headers=headers)

print(response.content)
```

#### Python

In Python, [requests](https://pypi.org/project/requests/) is one of the well-known HTTP libraries that we can use to build our client code.

You can have a look at some Python files with these examples [in the repo](use_cases/import_automation_results/python). You need to run `pip install -r requirements.txt` before running them, to install the dependencies. Don't forget to customize the variables directly in the source-code, to specify the Xray/Jira credentials among other.
  
This example shows how to either use HTTP basic authentication or Personal Access tokens.
It uses the "standard" RF endpoint provided by Xray.

##### Xray server/DC
  
```python
import requests
from base64 import urlsafe_b64encode as b64e

jira_base_url = "http://192.168.56.102"
jira_username = "admin"
jira_password = "admin"
personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY"


# endpoint doc for importing JUnit XML reports: https://docs.getxray.app/display/XRAY/Import+Execution+Results+-+REST#ImportExecutionResultsREST-JUnitXMLresults
params = (('projectKey', 'CALC'),('fixVersion','v1.0'))
files = {'file': ('output.xml', open(r'junit.xml', 'rb')),}

# importing results using HTTP basic authentication
# response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/junit', params=params, files=files, auth=(jira_username, jira_password))

# importing results using Personal Access Tokens 
headers = {'Authorization': 'Bearer ' + personal_access_token}
response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/junit', params=params, files=files, headers=headers)

print(response.status_code)
print(response.content)
```

##### Xray Cloud

```python
import requests
import json

xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2"
client_id = "215FFD69FE4644728C72182E00000000"
client_secret = "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000"

# endpoint doc for authenticating and obtaining token from Xray Cloud: https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2
headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
auth_data = { "client_id": client_id, "client_secret": client_secret }
response = requests.post(f'{xray_cloud_base_url}/authenticate', data=json.dumps(auth_data), headers=headers)
auth_token = response.json()
print(auth_token)

# endpoint doc for importing JUnit XML reports: https://docs.getxray.app/display/XRAYCLOUD/Import+Execution+Results+-+REST#ImportExecutionResultsREST-JUnitXMLresults
params = (('projectKey', 'BOOK'),('fixVersion','1.0'))
report_content = open(r'junit.xml', 'rb')
headers = {'Authorization': 'Bearer ' + auth_token, 'Content-Type': 'application/xml'}
response = requests.post(f'{xray_cloud_base_url}/import/execution/junit', params=params, data=report_content, headers=headers)

print(response.content)
```

### Importing results from Cucumber
  
This example shows how to either use HTTP basic authentication or Personal Access tokens.
It uses the "standard" Cucumber endpoint provided by Xray. This endpoint doesn't provide the ability to define additional parameters, such as project, version, Test Plan, Test Environment. To accomplish that, we need to use the "multipart" endpoint instead.
Whenever importing results using the Cucumber "standard" endpoint, a Test Execution will be created in the same project where Test issues are.

#### Java

Please check the [possible implementation](java/xray-code-snippets/src/main/java/com/xblend/xray/XrayResultsImporter.java) of a Java client library.

You can find these, or similar, [examples in a sample ImportResultsExamples class](java/xray-code-snippets/src/main/java/com/xblend/xray/ImportResultsExamples.java).

##### Xray server/DC

If you use the sample client library code, it could become as simple as:

```java
XrayResultsImporter new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).build();
// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).build();
String response = xrayImporter.submit(XrayResultsImporter.CUCUMBER_FORMAT, cucumberReport);
````

##### Xray Cloud

If you use the sample client library code, it could become as simple as:

```java
XrayResultsImporter xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).build();
String response = xrayImporter.submit(XrayResultsImporter.CUCUMBER_FORMAT, cucumberReport);
````

#### JavaScript

##### Xray server/DC
  
```javascript
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');
var qs=require('qs');

var jira_base_url = "http://192.168.56.102";
var personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY";

//var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);

const report_content = fs.readFileSync("cucumber.json").toString();
console.log(report_content);

var info_json = { 
    "fields": {
        "project": {
            "key": "CALC"
        },
        "summary": "Test Execution for Cucumber Execution",
        "description": "This contains test automation results",
        "fixVersions": [ {"name": "v1.0"}],
        "customfield_11805": ["chrome"],
        "customfield_11807": ["CALC-8895"]
    }
};
var bodyFormData = new FormData();
bodyFormData.append('result', report_content, 'cucumber.json'); 
bodyFormData.append('info', JSON.stringify(info_json), 'info.json'); 
console.log(JSON.stringify(info_json));

var endpoint_url = jira_base_url + "/rest/raven/2.0/import/execution/cucumber/multipart";
axios.post(endpoint_url, bodyFormData, {
    //headers: { 'Authorization': basicAuth, ...bodyFormData.getHeaders() }
    headers: { 'Authorization': "Bearer " + personal_access_token, ...bodyFormData.getHeaders() }
}).then(function(response) {
    console.log('success');
    console.log(response.data.testExecIssue.key);
}).catch(function(error) {
    console.log('Error on Authentication: ' + error);
});
```

##### Xray Cloud

```javascript
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var client_id = "215FFD69FE4644728C72182E00000000";
var client_secret = "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

// endpoint doc for authenticating and obtaining token from Xray Cloud: https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2
var authenticate_url = xray_cloud_base_url + "/authenticate";

axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    console.log('authenticated with success');
    var auth_token = response.data;
    // console.log("AUTH: " + auth_token);

    const report_content = fs.readFileSync("cucumber.json").toString();
    // console.log(report_content);
    
    // endpoint doc for importing Cucumber JSON reports using the multipart endpoint: https://docs.getxray.app/display/XRAYCLOUD/Import+Execution+Results+-+REST#ImportExecutionResultsREST-CucumberJSONresultsMultipart
    var endpoint_url = xray_cloud_base_url + "/import/execution/cucumber/multipart";
    
    var info_json = { 
        "fields": {
            "project": {
                "key": "CALC"
            },
            "summary": "Test Execution for Cucumber execution",
            "description": "This contains test automation results",
            "issuetype": {
                "name": "Test Execution"
            },
            "fixVersions": [ {"name": "v1.0"}],
            "xrayFields": {
                "testPlanKey": "CALC-8895",
                "environments": ["chrome"]
            }
        }
    };
    var bodyFormData = new FormData();
    bodyFormData.append('results', report_content, 'cucumber.json'); 
    bodyFormData.append('info', JSON.stringify(info_json), 'info.json'); 
    console.log(JSON.stringify(info_json));

    axios.post(endpoint_url, bodyFormData, {
        headers: { 'Authorization': "Bearer " + auth_token, ...bodyFormData.getHeaders() }
    }).then(function(res) {
        console.log('success');
        console.log(res.data.key);
    }).catch(function(error) {
        console.log('Error submiting results: ' + error);
    });
    

}).catch( (error) => {
    console.log('Error on Authentication: ' + error);
});


```

#### Python

##### Xray server/DC
  
```python
import requests

jira_base_url = "http://192.168.56.102"
jira_username = "admin"
jira_password = "admin"
personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY"


# endpoint doc for importing Cucumber JSON reports: https://docs.getxray.app/display/XRAY/Import+Execution+Results+-+REST#ImportExecutionResultsREST-CucumberJSONresults
# unlike other endpoints, params are NOT YET supported for the Cucumber standard endpoint
# params = (('projectKey', 'CALC'),('fixVersion','v1.0'))
report_content = open(r'cucumber.json', 'rb').read()
# print (report_content)

headers = {'Authorization': 'Bearer ' + personal_access_token, "Content-Type": 'application/json'}

# importing results using HTTP basic authentication
response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/cucumber', data=report_content, auth=(jira_username, jira_password), headers=headers)

# importing results using Personal Access Tokens 
# response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/cucumber', data=report_content, headers=headers)

print(response.status_code)
print(response.content)  

```

##### Xray Cloud

```python
import requests
import json

xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2"
client_id = "215FFD69FE4644728C72182E00000000"
client_secret = "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000"

# endpoint doc for authenticating and obtaining token from Xray Cloud: https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2
headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
auth_data = { "client_id": client_id, "client_secret": client_secret }
response = requests.post(f'{xray_cloud_base_url}/authenticate', data=json.dumps(auth_data), headers=headers)
auth_token = response.json()
print(auth_token)


# endpoint doc for importing Cucumber JSON reports: https://docs.getxray.app/display/XRAYCLOUD/Import+Execution+Results+-+REST#ImportExecutionResultsREST-CucumberJSONresults
# unlike other endpoints, params are NOT YET supported for the Cucumber standard endpoint
# params = (('projectKey', 'CALC'),('fixVersion','v1.0'))
report_content = open(r'cucumber.json', 'rb').read()
# print (report_content)

headers = {'Authorization': 'Bearer ' + auth_token, 'Content-Type': 'application/json'}
response = requests.post(f'{xray_cloud_base_url}/import/execution/cucumber', data=report_content, headers=headers)

print(response.status_code)
print(response.content)
```

### Importing results from Cucumber, to a specific project, version, Test Plan, and Test Environment
  
This example shows how to either use HTTP basic authentication or Personal Access tokens.
It uses the "multipart" Cucumber endpoint provided by Xray so we can specify the project, version, Test Plan, and Test Environment the results are related to.
  
#### Java

Please check the [possible implementation](java/xray-code-snippets/src/main/java/com/xblend/xray/XrayResultsImporter.java) of a Java client library.

You can find these, or similar, [examples in a sample ImportResultsExamples class](java/xray-code-snippets/src/main/java/com/xblend/xray/ImportResultsExamples.java).

##### Xray server/DC

If you use the sample client library code, it could become as simple as:

```java
JSONObject testInfo = new JSONObject();
JSONObject testExecInfo = new JSONObject()
.put("fields", new JSONObject()
    .put("fields", new JSONObject()
        .put("summary", "Test execution for automated tests")
        .put("project", new JSONObject().put("key", "CALC"))
        .put("issuetype", new JSONObject().put("name", "Test Execution"))
        .put("fixVersions", new JSONObject[] { new JSONObject().put("name", "v1.0") })
        .put("customfield_11807", new String[] {"CALC-8895"} ) // in this Jira instance, customfield_11805 corresponds to the Test Plan custom field; please check yours
        .put("customfield_11805", new String[] {"chrome"} )  // in this Jira instance, customfield_11805 corresponds to the Test Environments custom field; please check yours
    );
XrayResultsImporter xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).build();
// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).build();
String response = xrayImporter.submitMultipartServerDC(XrayResultsImporter.CUCUMBER_FORMAT, cucumberReport, testExecInfo, testInfo);
````

##### Xray Cloud

If you use the sample client library code, it could become as simple as:

```java
JSONObject testInfo = new JSONObject();
JSONObject testExecInfo = new JSONObject()
    .put("fields", new JSONObject()
        .put("summary", "Test execution for automated tests")
        .put("project", new JSONObject().put("key", "CALC"))
        .put("issuetype", new JSONObject().put("name", "Test Execution"))
        .put("fixVersions", new JSONObject[] { new JSONObject().put("name", "v1.0") })
        )
    .put("xrayFields", new JSONObject()
        .put("testPlanKey", "CALC-1224")
        .put("environments", new String[] { "Chrome" })
        );
XrayResultsImporter xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).build();
String response = xxrayImporter.submitMultipartCloud(XrayResultsImporter.CUCUMBER_FORMAT, cucumberReport, testExecInfo, testInfo);
````

#### JavaScript

##### Xray server/DC

```javascript
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');
var qs=require('qs');

var jira_base_url = "http://192.168.56.102";
var personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY";

//var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);

const report_content = fs.readFileSync("cucumber.json").toString();
console.log(report_content);

var info_json = { 
    "fields": {
        "project": {
            "key": "CALC"
        },
        "summary": "Test Execution for Cucumber Execution",
        "description": "This contains test automation results",
        "fixVersions": [ {"name": "v1.0"}],
        "customfield_11805": ["chrome"],
        "customfield_11807": ["CALC-8895"]
    }
};
var bodyFormData = new FormData();
bodyFormData.append('result', report_content, 'cucumber.json'); 
bodyFormData.append('info', JSON.stringify(info_json), 'info.json'); 
console.log(JSON.stringify(info_json));

var endpoint_url = jira_base_url + "/rest/raven/2.0/import/execution/cucumber/multipart";
axios.post(endpoint_url, bodyFormData, {
    //headers: { 'Authorization': basicAuth, ...bodyFormData.getHeaders() }
    headers: { 'Authorization': "Bearer " + personal_access_token, ...bodyFormData.getHeaders() }
}).then(function(response) {
    console.log('success');
    console.log(response.data.testExecIssue.key);
}).catch(function(error) {
    console.log('Error submiting results: ' + error);
});
```

##### Xray Cloud

```javascript
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

// endpoint doc for authenticating and obtaining token from Xray Cloud: https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2
var authenticate_url = xray_cloud_base_url + "/authenticate";

axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    console.log('authenticated with success');
    var auth_token = response.data;

    const report_content = fs.readFileSync("cucumber.json").toString();
    
    // endpoint doc for importing Cucumber JSON reports using the multipart endpoint: https://docs.getxray.app/display/XRAYCLOUD/Import+Execution+Results+-+REST#ImportExecutionResultsREST-CucumberJSONresultsMultipart
    var endpoint_url = xray_cloud_base_url + "/import/execution/cucumber/multipart";
    
    var info_json = { 
        "fields": {
            "project": {
                "key": "CALC"
            },
            "summary": "Test Execution for Cucumber execution",
            "description": "This contains test automation results",
            "issuetype": {
                "name": "Test Execution"
            },
            "fixVersions": [ {"name": "v1.0"}],
            "xrayFields": {
                "testPlanKey": "CALC-8895",
                "environments": ["chrome"]
            }
        }
    };
    var bodyFormData = new FormData();
    bodyFormData.append('results', report_content, 'cucumber.json'); 
    bodyFormData.append('info', JSON.stringify(info_json), 'info.json'); 
    console.log(JSON.stringify(info_json));

    axios.post(endpoint_url, bodyFormData, {
        headers: { 'Authorization': "Bearer " + auth_token, ...bodyFormData.getHeaders() }
    }).then(function(res) {
        console.log('success');
        console.log(res.data.key);
    }).catch(function(error) {
        console.log('Error submiting results: ' + error);
    });
    

}).catch( (error) => {
    console.log('Error on Authentication: ' + error);
});

```

#### Python

##### Xray server/DC
  
```python
import requests
import json

jira_base_url = "http://192.168.56.102"
jira_username = "admin"
jira_password = "admin"
personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY"


# endpoint doc for importing Cucumber JSON reports, allowing customization of Test Execution fields: 
# customfield_11805 corresponds to the "Test Environments" custom field; pls obtain this id on your Jira instance from Jira administration
# customfield_11807 corresponds to the "Test Plan" custom field; pls obtain this id on your Jira instance from Jira administration
info_json = { 
    "fields": {
        "project": {
            "key": "CALC"
        },
        "summary": "Test Execution for Cucumber execution",
        "description": "This contains test automation results",
        "fixVersions": [ {"name": "v1.0"}],
        "customfield_11805": ["chrome"],
        "customfield_11807": ["CALC-8895"]
    }
}

files = {
        'result': ('cucumber.json', open(r'cucumber.json', 'rb')),
        'info': ('info.json', json.dumps(info_json) )
        }

# importing results using HTTP basic authentication
# response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/junit', params=params, files=files, auth=(jira_username, jira_password))

# importing results using Personal Access Tokens 
headers = {'Authorization': 'Bearer ' + personal_access_token}
response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/cucumber/multipart', files=files, headers=headers)

print(response.status_code)
print(response.content)
```

##### Xray Cloud

```python
import requests
import json

xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2"
client_id = "215FFD69FE4644728C72182E00000000"
client_secret = "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000"

# endpoint doc for authenticating and obtaining token from Xray Cloud: https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2
headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
auth_data = { "client_id": client_id, "client_secret": client_secret }
response = requests.post(f'{xray_cloud_base_url}/authenticate', data=json.dumps(auth_data), headers=headers)
auth_token = response.json()
print(auth_token)

info_json = { 
    "fields": {
        "project": {
            "key": "CALC"
        },
        "summary": "Test Execution for RF execution",
        "description": "This contains test automation results",
        "fixVersions": [ {"name": "v1.0"}],
        "xrayFields": {
            "testPlanKey": "CALC-8895",
            "environments": ["chrome"]
        }
    }
}

files = {
        'results': ('cucumber.json', open(r'cucumber.json', 'rb')),
        'info': ('info.json', json.dumps(info_json) )
        }

# endpoint doc for importing Cucumber JSON reports using the multipart endpoint: https://docs.getxray.app/display/XRAYCLOUD/Import+Execution+Results+-+REST#ImportExecutionResultsREST-CucumberJSONresultsMultipart
headers = {'Authorization': 'Bearer ' + auth_token}
response = requests.post(f'{xray_cloud_base_url}/import/execution/cucumber/multipart', files=files, headers=headers)

print(response.status_code)
print(response.content)
```
