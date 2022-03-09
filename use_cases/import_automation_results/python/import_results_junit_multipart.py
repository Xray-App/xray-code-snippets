import requests
import json

jira_base_url = "http://192.168.56.102"
jira_username = "admin"
jira_password = "admin"
personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY"

# endpoint doc for importing JUnit XML reports: https://docs.getxray.app/display/XRAY/Import+Execution+Results+-+REST#ImportExecutionResultsREST-JUnitXMLresultsMultipart
# customfield_11805 corresponds to the "Test Environments" custom field; pls obtain this id on your Jira instance from Jira administration
# customfield_11807 corresponds to the "Test Plan" custom field; pls obtain this id on your Jira instance from Jira administration
info_json = { 
    "fields": {
        "project": {
            "key": "CALC"
        },
        "summary": "Test Execution for JUnit tests",
        "description": "This contains test automation results",
        "fixVersions": [ {"name": "v1.0"}],
        "customfield_11805": ["chrome"],
        "customfield_11807": ["CALC-8895"]
    }
}

files = {
        'file': ('junit.xml', open(r'junit.xml', 'rb')),
        'info': ('info.json', json.dumps(info_json) )
        }

# importing results using HTTP basic authentication
# response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/junit', params=params, files=files, auth=(jira_username, jira_password))

# importing results using Personal Access Tokens 
headers = {'Authorization': 'Bearer ' + personal_access_token}
response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/junit/multipart', files=files, headers=headers)

print(response.status_code)
print(response.content)