# Importing test automation results


## Background

Importing results is accomplished by using the REST APIs.
There are some subtle differences between Xray server/DC REST API and Xray Cloud REST API, including authentication mechanisms and also on the request iself.


## Code snippets

### Java

### JavaScript

### Python

In Python, [requests](https://pypi.org/project/requests/) is one of the well-known HTTP libraries that we can use to build our client code.


#### Xray server/DC

##### Importing results from Robot Framework to the project identified by key CALC, related to version/release "v1.0"

This example shows how to either use HTTP basic authentication or Personal Access tokens.

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

#### Xray Cloud

##### Importing results from Robot Framework to the project identified by key BOOK, related to version/release "1.0"


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
