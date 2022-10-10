# Works for Jira DC and for Jira cloud.
#  - for Jira DC (datacenter), the password can be either the Personal Access token or the Jira user's password
#  - for Jira Cloud, the password is the API Token (https://id.atlassian.com/manage-profile/security/api-tokens)
#
# Jira's base URL for DC is typically: https://yourlocaljiraserver/rest/api/2
# Jira's base URL for cloud is typically: https://xxxx.atlassian.net/rest/api/3
#
#  endpoint doc, similar for DC and cloud: https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/#api-rest-api-3-issue-issueidorkey-get

import requests
import json
import os
from requests.auth import HTTPBasicAuth

jira_base_url = "https://sergiofreire.atlassian.net/rest/api/3"
jira_username = client_id = os.getenv('JIRA_USERNAME', "xxxx")
jira_password = os.getenv('JIRA_PASSWORD',"xxx") 

def getIssueKeys(issueIds):
    issueKeys = []
    for issueId in issueIds:
        response = requests.get(f'{jira_base_url}/issue/{issueId}', params={}, auth=HTTPBasicAuth(jira_username, jira_password))
        res = response.json()
        issueKeys.append(res["key"])
    return issueKeys

def getIssueIds(issueKeys):
    issueIds = []
    for issueKey in issueKeys:
        response = requests.get(f'{jira_base_url}/issue/{issueKey}', params={}, auth=HTTPBasicAuth(jira_username, jira_password))
        res = response.json()
        issueIds.append(res["id"])
    return issueIds

issueIds = ["12933", "12933"]
print(getIssueKeys(issueIds))

issueKeys = ['CALC-2663', 'CALC-2663']
print(getIssueIds(issueKeys))