var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var xray_cloud_base_url = "https://xray.cloud.xpand-it.com/api/v2";
var client_id = "215FFD69FE4644728C72182E00000000";
var client_secret = "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

// endpoint doc for authenticating and obtaining token from Xray Cloud: https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2
var authenticate_url = xray_cloud_base_url + "/authenticate";

axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    console.log('authenticated with success');
    var auth_token = response.data;
    // console.log("AUTH: " + auth_token);

    const report_content = fs.readFileSync("cucumber.json").toString();
    // console.log(report_content);
    
    // endpoint doc for importing Cucumber JSON reports using the multipart endpoint: https://docs.getxray.app/display/XRAYCLOUD/Import+Execution+Results+-+REST#ImportExecutionResultsREST-CucumberJSONresultsMultipart
    var endpoint_url = xray_cloud_base_url + "/import/execution/cucumber/multipart";
    
    var info_json = { 
        "fields": {
            "project": {
                "key": "CALC"
            },
            "summary": "Test Execution for Cucumber execution",
            "description": "This contains test automation results",
            "issuetype": {
                "name": "Test Execution"
            },
            "fixVersions": [ {"name": "v1.0"}],
            "xrayFields": {
                "testPlanKey": "CALC-8895",
                "environments": ["chrome"]
            }
        }
    };
    var bodyFormData = new FormData();
    bodyFormData.append('results', report_content, 'cucumber.json'); 
    bodyFormData.append('info', JSON.stringify(info_json), 'info.json'); 
    console.log(JSON.stringify(info_json));

    axios.post(endpoint_url, bodyFormData, {
        headers: { 'Authorization': "Bearer " + auth_token, ...bodyFormData.getHeaders() }
    }).then(function(res) {
        console.log('success');
        console.log(res.data.key);
    }).catch(function(error) {
        console.log('Error submiting results: ' + error);
    });
    

}).catch( (error) => {
    console.log('Error on Authentication: ' + error);
});

