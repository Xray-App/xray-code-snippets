import requests
import json
import os

xray_cloud_base_url = "https://xray.cloud.xpand-it.com/api/v2"
client_id = os.getenv('CLIENT_ID', "215FFD69FE4644728C72182E00000000")
client_secret = os.getenv('CLIENT_SECRET',"1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000")

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
