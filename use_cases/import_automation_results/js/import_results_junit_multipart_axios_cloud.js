var btoa = require('btoa');
var axios = require('axios');
var fs = require('fs');
var FormData = require('form-data');

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

var authenticate_url = xray_cloud_base_url + "/authenticate";

axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    console.log('authenticated with success');
    var auth_token = response.data;
    // console.log("AUTH: " + auth_token);

    const report_content = fs.readFileSync("junit.xml").toString();
    //console.log(report_content);
    
    var endpoint_url = xray_cloud_base_url + "/import/execution/junit/multipart";
    
        var info_json = { 
            "fields": {
                "project": {
                    "key": "BOOK"
                },
                "summary": "Test Execution for JUnit tests",
                "description": "This contains test automation results",
                "issuetype": {
                    "name": "Test Execution"
                }
            }
        };
        var bodyFormData = new FormData();
        bodyFormData.append('results', report_content, 'junit.xml'); 
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

