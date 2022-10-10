/*
 Works for Jira DC and for Jira cloud.
  - for Jira DC (datacenter), the password can be either the Personal Access token or the Jira user's password
  - for Jira Cloud, the password is the API Token (https://id.atlassian.com/manage-profile/security/api-tokens)

 Jira's base URL for DC is typically: https://yourlocaljiraserver/rest/api/2
 Jira's base URL for cloud is typically: https://xxxx.atlassian.net/rest/api/3

 endpoint doc, similar for DC and cloud: https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/#api-rest-api-3-issue-issueidorkey-get
*/
var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');

var jira_base_url = "https://xray-demo3.getxray.app/rest/api/2";
// var personal_access_token = process.env["JIRA_PAT"] || "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY";
jira_username = process.env["JIRA_USERNAME"] || "xadmin";
jira_password = process.env["JIRA_PASSWORD"] || "xispe";


/* 
    // Jira cloud..
    jira_base_url = "https://xxxx.atlassian.net/rest/api/3"
    jira_username = "sergio@example.com"
    jira_password = "V4jK1Ic3dNNuRRzkK200"
*/

async function getIssueKeys(issueIds) {
    var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);
    var issueKeys = [];
    for (let i = 0; i < issueIds.length; i++) {
        let issueId=issueIds[i];
        var url = jira_base_url + "/issue/" + issueId;
        try {
            response = await axios.get(url, {
                headers: { 'Authorization': basicAuth }
                // headers: { 'Authorization': "Bearer " + personal_access_token }
            });
            // console.log(response.data.key);
            issueKeys.push(response.data.key)
        } catch (error) {
            console.log('Error fetching info: ' + error);
        }
    };
    return issueKeys;
}

async function getIssueIds(issueKeys) {
    var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);
    var issueIds = [];
    for (let i = 0; i < issueKeys.length; i++) {
        let issueKey = issueKeys[i];
        var url = jira_base_url + "/issue/" + issueKey;
        try {
            response = await axios.get(url, {
                headers: { 'Authorization': basicAuth }
                // headers: { 'Authorization': "Bearer " + personal_access_token }
            });
            // console.log(response.data.id);
            issueIds.push(response.data.id)
        } catch (error) {
            console.log('Error fetching info: ' + error);
        }
    };
    return issueIds;
}

/* main */
(async () => {
    issueIds = ["22153", "22153", "22153"];
    issueKeys = await getIssueKeys(issueIds);
    console.log(issueKeys);

    issueKeys = ["BOOK-490", "BOOK-490", "BOOK-490"];
    issueIds = await getIssueIds(issueKeys);
    console.log(issueIds);
})();