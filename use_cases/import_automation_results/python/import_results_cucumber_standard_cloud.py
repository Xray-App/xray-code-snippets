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
