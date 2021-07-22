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
# response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/robot', params=params, files=files, auth=(jira_username, jira_password))

# importing results using Personal Access Tokens 
headers = {'Authorization': 'Bearer ' + personal_access_token}
response = requests.post(f'{jira_base_url}/rest/raven/2.0/import/execution/cucumber/multipart', files=files, headers=headers)

print(response.status_code)
print(response.content)