# Works for Jira DC and for Jira cloud.
#  - for Jira DC (datacenter), the password can be either the Personal Access token or the Jira user's password
#  - for Jira Cloud, the password is the API Token (https://id.atlassian.com/manage-profile/security/api-tokens)
#
# Jira's base URL for DC is typically: https://yourlocaljiraserver/rest/api/2
# Jira's base URL for cloud is typically: https://xxxx.atlassian.net/rest/api/3
#
#  endpoint doc, similar for DC and cloud: https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issue-attachments/#api-rest-api-3-issue-issueidorkey-attachments-post
#  other info: https://community.atlassian.com/t5/Jira-questions/Import-multiple-files-into-a-Jira-Cloud-issue-via-Rest-API/qaq-p/1996165

import requests
from requests.auth import HTTPBasicAuth
import os
import json

jira_base_url = "https://xraytutorials.atlassian.net/rest/api/2"
jira_username = client_id = os.getenv('JIRA_USERNAME', "xxx")
jira_password = os.getenv('JIRA_PASSWORD',"xxx")
issueIdOrKey = "CALC-42"


# List of files to attach
file_paths = ['./use_cases/jira_rest_api_usage/python/dummyfile1.txt', './use_cases/jira_rest_api_usage/python/dummyfile2.txt']

# Prepare the attachments data
payload = []
for file_path in file_paths:
    file_name = file_path.split('/')[-1]
    payload.append( ('file', (file_name, open(file_path, 'rb') , 'application/octet-stream') ) )

url = f'{jira_base_url}/issue/{issueIdOrKey}/attachments'
auth = HTTPBasicAuth(jira_username, jira_password)
headers = {
    "Accept": "application/json",
    "X-Atlassian-Token": "no-check"
 }

response = requests.request(
    "POST",
    url,
    headers = headers,
    auth = auth,
    files = payload  
 )

if response.status_code == 200:
    print("Files attached successfully!")
    print(json.dumps(json.loads(response.text), sort_keys=True, indent=4, separators=(",", ": ")))
else:
    print(f"Failed to attach files: {response.text}")







