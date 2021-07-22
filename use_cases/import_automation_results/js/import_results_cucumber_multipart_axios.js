var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');
var qs=require('qs');

var jira_base_url = "http://192.168.56.102";
var personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY";

//var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);

const report_content = fs.readFileSync("cucumber.json").toString();
console.log(report_content);

var info_json = { 
    "fields": {
        "project": {
            "key": "CALC"
        },
        "summary": "Test Execution for Cucumber Execution",
        "description": "This contains test automation results",
        "fixVersions": [ {"name": "v1.0"}],
        "customfield_11805": ["chrome"],
        "customfield_11807": ["CALC-8895"]
    }
};
var bodyFormData = new FormData();
bodyFormData.append('result', report_content, 'cucumber.json'); 
bodyFormData.append('info', JSON.stringify(info_json), 'info.json'); 
console.log(JSON.stringify(info_json));

var endpoint_url = jira_base_url + "/rest/raven/2.0/import/execution/cucumber/multipart";
axios.post(endpoint_url, bodyFormData, {
    //headers: { 'Authorization': basicAuth, ...bodyFormData.getHeaders() }
    headers: { 'Authorization': "Bearer " + personal_access_token, ...bodyFormData.getHeaders() }
}).then(function(response) {
    console.log('success');
    console.log(response.data.testExecIssue.key);
}).catch(function(error) {
    console.log('Error submiting results: ' + error);
});
