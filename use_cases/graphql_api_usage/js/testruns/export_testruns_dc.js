/*
# Obtains the test runs based on a previous saved Jira filter (by its id, not its name)
# 
# Returns:
#   - for each Test Run:
#       - ....
#
# Refs: 
# - https://docs.getxray.app/display/XRAY/Export+Execution+Results+-+REST
*/

var axios = require('axios');
const { create } = require('domain');
const fs = require('fs');

var jira_base_url = "https://xray-demo3.getxray.app";
//var personal_access_token = "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY";
var jira_username = 'xadmin'
var jira_password = 'xispe'


async function createJiraFilter(jql) {
  var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);

  let url = jira_base_url + "/rest/api/2/filter"
  const filter = JSON.stringify(
      {
        "jql": jql,
        "name": "temporary filter",
        "description": "temporary filter to indirectly export test runs from",
        "favourite": false
      }
    );

  return axios.post(url, filter, {
    headers: { 'Authorization': basicAuth, 'Content-Type': 'application/json' }
    // headers: { 'Authorization': "Bearer " + personal_access_token }
  }).then(function(response) {
      console.log('success: created temporary filter ' + response.data["id"] + ' in Jira');
      // console.log(response.data);
      return response.data;
  }).catch(function(error) {
      console.log('Error creating filter: ' + error);
  });
}

async function deleteJiraFilter(filterId) {
  var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);

  let url = jira_base_url + "/rest/api/2/filter/" + filterId
  return axios.delete(url, {
    headers: { 'Authorization': basicAuth }
    // headers: { 'Authorization': "Bearer " + personal_access_token }
  }).then(function(response) {
    console.log('success: created temporary filter ' + filterId + ' in Jira');
      //console.log(response.data);
      return response.data;
  }).catch(function(error) {
      console.log('Error deleting filter: ' + error);
  });
}


async function getTestRuns(filterId, page, limit) {
  var basicAuth = 'Basic ' + btoa(jira_username + ':' + jira_password);

  var endpoint_url = jira_base_url + "/rest/raven/2.0/testruns";
  const params = new URLSearchParams({
    savedFilterId: filterId,
    includeTestFields: "issuelinks",
    page: page,
    limit: limit
  }).toString();
  const url = endpoint_url + "?" + params;

  return axios.get(url, {
      headers: { 'Authorization': basicAuth }
      // headers: { 'Authorization': "Bearer " + personal_access_token }
  }).then(function(response) {
      // console.log('success');
      // console.log(response.data);
      return response.data;
  }).catch(function(error) {
      console.log('Error exporting test runs: ' + error);
  });
}


/**** main *****/


(async () => {


  /*
    We can either use an existing Jira filter, by its id, or create a temporary one so we can define
     the JQL expression in the code. 
    If we choose the latter, i.e., to create a temporary filter, then we should cleanup at the end of the process.
  */

  // Jira JQL expression to indirectly obtain the test runs from
  jql = "project = BOOK and issuetype = 'Test Execution'"

  // create a temporary filter based on a JQL expression
  let filter =  await createJiraFilter(jql);
  let filterId = filter["id"]

  // obtain the Test Runs for the given Jira filter id
  let testruns = []
  let page = 1
  let limit = 100
  let trs = []
  do {
    trs = await getTestRuns(filterId, page, limit)
    page += 1
    testruns.push(...trs)
  } while (trs.length > 0)
  

  // delete temporary filter, if we created it 
  await deleteJiraFilter(filterId);

  console.log(JSON.stringify(testruns, undefined, 2))
  fs.writeFileSync('testruns_dc.json', JSON.stringify(testruns));

})();
