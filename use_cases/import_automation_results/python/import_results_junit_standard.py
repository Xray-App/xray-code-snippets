import requests
from base64 import urlsafe_b64encode as b64e   

jira_base_url = "http://192.168.56.102"
jira_username = "admin"
jira_password = "admin"
personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY"


# endpoint doc for importing JUnit XML reports: https://docs.getxray.app/display/XRAY/Import+Execution+Results+-+REST#ImportExecutionResultsREST-JUnitXMLresults
params = (('projectKey', 'CALC'),('fixVersion','v1.0'))
files = {'file': ('junit.xml', open(r'junit.xml', 'rb')),}

# importing results using HTTP basic authentication
# response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/junit', params=params, files=files, auth=(jira_username, jira_password))

# importing results using Personal Access Tokens 
headers = {'Authorization': 'Bearer ' + personal_access_token}
response = requests.post(f'{jira_base_url}/rest/raven/1.0/import/execution/junit', params=params, files=files, headers=headers)

print(response.status_code)
print(response.content)