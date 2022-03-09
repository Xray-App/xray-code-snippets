var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var jira_base_url = "http://192.168.56.102";
var personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY";

//var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);

const report_content = fs.readFileSync("xray_dc.json").toString();
console.log(report_content);

var endpoint_url = jira_base_url + "/rest/raven/2.0/import/execution";
axios.post(endpoint_url, report_content, {
    //headers: { 'Authorization': basicAuth, "Content-Type": "application/json" }
    headers: { 'Authorization': "Bearer " + personal_access_token, "Content-Type": "application/json" }
}).then(function(response) {
    console.log('success');
    console.log(response.data.testExecIssue.key);
}).catch(function(error) {
    console.log('Error submiting results: ' + error);
});
