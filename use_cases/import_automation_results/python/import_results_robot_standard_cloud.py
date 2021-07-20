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
