# Importing test automation results


## Background

Importing results is accomplished by using the REST APIs.
There are some subtle differences between [Xray server/DC REST API](https://docs.getxray.app/display/XRAY/REST+API) and [Xray Cloud REST API](https://docs.getxray.app/display/XRAYCLOUD/REST+API), including authentication mechanisms and also on the request iself.

First, Xray supports importing test automation results in different formats, including JUnit XML, TestNG XML, Robot Framework XML, Cucumber JSON, Behave JSON, etc.
Xray also has a specific proprietary format named "Xray JSON" that can be used to import results
The information that can be processed from these report formats differs a bit due to their nature.

For all these formats, there are specific endpoints that can be used to submit the test automation results.

**There are in fact two endpoints per each format**:
- a "standard" one, that is simpler to use and provides the ability to specify common parameters to better identify what we aim to report against (e.g. project, version, Test Plan, Test Environment). Usually, these endpoint URLs end with ".../import/execution/<format>", with the exception of Xray JSON format that ends up with ".../import/execution";
- a "multipart" one, which is a bit more low-level; its purpose is to allow full customization of the fields of the Test Execution that will be created (and eventually also on the Test issues). Usually, these endpoint URLs end with ".../import/execution/<format>/multipart", with the exception of Xray JSON format that ends up with ".../import/execution/multipart";

To keep it simple, you can use the so called "standard" endpoints, as their syntax is simpler and it fits most scenarios. 

## Code snippets

### Java

### JavaScript

In JavaScript, there are a bunch of HTTP client libraries (e.g. axios, request, superagent) that can be used to build our code.
The following examples make use of [axios](https://www.npmjs.com/package/axios).
  
You can have a look at some JavaScript files with these examples [in the repo](use_cases/import_automation_results/js). You need to run `npm install` before running them, to install the dependencies. Don't forget to customize the variables directly in the source-code, to specify the Xray/Jira credentials among other.


#### Importing results from Robot Framework to a given Jira project, identified by its key, for a specific version/release

This example shows how to either use HTTP basic authentication or Personal Access tokens.
It uses the "standard" RF endpoint provided by Xray.

##### Xray server/DC
  
```javascript
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var jira_base_url = "http://192.168.56.102";
var personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY";


  //var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);

  const report_content = fs.readFileSync("output.xml").toString();
  console.log(report_content);

  var bodyFormData = new FormData();
  bodyFormData.append('file', report_content, 'output.xml'); 

  var endpoint_url = jira_base_url + "/rest/raven/2.0/import/execution/robot";
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
        console.log('Error on Authentication: ' + error);
    });
```

##### Xray Cloud

```javascript
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var xray_cloud_base_url = "https://xray.cloud.xpand-it.com/api/v2";
var client_id = "215FFD69FE4644728C72182E00000000";
var client_secret = "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

var authenticate_url = xray_cloud_base_url + "/authenticate";

    axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
        console.log('success');
        var auth_token = response.data;

        console.log("AUTH: " + auth_token);

        const report_content = fs.readFileSync("output.xml").toString();
        console.log(report_content);
      
        var endpoint_url = xray_cloud_base_url + "/import/execution/robot";
          const params = new URLSearchParams({
              projectKey: "BOOK"
          }).toString();
          const url = endpoint_url + "?" + params;
      
      
          axios.post(url, report_content, {
              headers: { 'Authorization': "Bearer " + auth_token, "Content-Type": "application/xml" }
          }).then(function(res) {
              console.log('success');
              console.log(res.data.key);
          }).catch(function(error) {
              console.log('Error on Authentication: ' + error);
          });
      

    }).catch( (error) => {
        console.log('Error on Authentication: ' + error);
    });
```


### Python

In Python, [requests](https://pypi.org/project/requests/) is one of the well-known HTTP libraries that we can use to build our client code.


#### Importing results from Robot Framework to a given Jira project, identified by its key, for a specific version/release

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


# endpoint doc for importing Robot Framework XML reports: https://docs.getxray.app/display/XRAY/Import+Execution+Results+-+REST#ImportExecutionResultsREST-RobotFrameworkXMLresults
params = (('projectKey', 'CALC'),('fixVersion','v1.0'))
files = {'file': ('output.xml', open(r'output.xml', 'rb')),}

# importing results using HTTP basic authentication
# response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/robot', params=params, files=files, auth=(jira_username, jira_password))

# importing results using Personal Access Tokens 
headers = {'Authorization': 'Bearer ' + personal_access_token}
response = requests.post(f'{jira_base_url}/rest/raven/1.0/import/execution/robot', params=params, files=files, headers=headers)

print(response.status_code)
print(response.content)
```

##### Xray Cloud

```python
import requests
import json

xray_cloud_base_url = "https://xray.cloud.xpand-it.com/api/v2"
client_id = "215FFD69FE4644728C72182E00000000"
client_secret = "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000"

# endpoint doc for authenticating and obtaining token from Xray Cloud: https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2
headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
auth_data = { "client_id": client_id, "client_secret": client_secret }
response = requests.post(f'{xray_cloud_base_url}/authenticate', data=json.dumps(auth_data), headers=headers)
auth_token = response.json()
print(auth_token)

# endpoint doc for importing Robot Framework XML reports: https://docs.getxray.app/display/XRAYCLOUD/Import+Execution+Results+-+REST+v2#ImportExecutionResultsRESTv2-RobotFrameworkXMLresults
params = (('projectKey', 'BOOK'),('fixVersion','1.0'))
report_content = open(r'output.xml', 'rb')
headers = {'Authorization': 'Bearer ' + auth_token, 'Content-Type': 'application/xml'}
response = requests.post(f'{xray_cloud_base_url}/import/execution/robot', params=params, data=report_content, headers=headers)

print(response.content)
```
