/*
# Works for Jira DC and for Jira cloud.
#  - for Jira DC (datacenter), the password can be either the Personal Access token or the Jira user's password
#  - for Jira Cloud, the password is the API Token (https://id.atlassian.com/manage-profile/security/api-tokens)
#
# Jira's base URL for DC is typically: https://yourlocaljiraserver/rest/api/2
# Jira's base URL for cloud is typically: https://xxxx.atlassian.net/rest/api/3
#
#  endpoint doc, similar for DC and cloud: https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issue-attachments/#api-rest-api-3-issue-issueidorkey-attachments-post
#  other info: https://community.atlassian.com/t5/Jira-questions/Import-multiple-files-into-a-Jira-Cloud-issue-via-Rest-API/qaq-p/1996165
*/
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var jira_base_url = "https://xray-demo3.getxray.app/rest/api/2";
// var personal_access_token = process.env["JIRA_PAT"] || "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY";
jira_username = process.env["JIRA_USERNAME"] || "xxx";
jira_password = process.env["JIRA_PASSWORD"] || "xxx";

var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);


/* 
    // Jira cloud..
    jira_base_url = "https://xxxx.atlassian.net/rest/api/3"
    jira_username = "sergio@example.com"
    jira_password = "V4jK1Ic3dNNuRRzkK200"
*/

async function attachFilesToIssue(issueKey, filePaths) {    
    var bodyFormData = new FormData();
    for (const filePath of filePaths) {
        const fileStream = fs.createReadStream(filePath);
        bodyFormData.append('file', fileStream);
    }

    var endpoint_url = jira_base_url + `/issue/${issueKey}/attachments`;
    console.log(endpoint_url);
    return await axios.post(endpoint_url, bodyFormData, {
        headers: { 'Authorization': basicAuth, 'X-Atlassian-Token': 'no-check', ...bodyFormData.getHeaders() }
        // headers: { 'Authorization': "Bearer " + personal_access_token, ...bodyFormData.getHeaders() }
    }).then(function(response) {
        // console.log('success');
        return response;
    }).catch(function(error) {
        console.log('Error adding attachments: ' + error);
    });
}


/* main */
(async () => {
    // List of files to attach
    filePaths = ['./use_cases/jira_rest_api_usage/js/dummyfile1.txt', './use_cases/jira_rest_api_usage/js/dummyfile2.txt']
    issueKey = 'COM-38';
    
    response = await attachFilesToIssue('COM-38', filePaths);
    console.log(response);
})();