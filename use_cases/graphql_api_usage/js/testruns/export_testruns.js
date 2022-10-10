/*
# Obtains the test runs for a given list of Test Executions (or Tests)
# 
# Returns:
#   - for each Test Run:
#       - global status, evidence, comment, defects
#       - related Test issue key, summary
#       - related Test Execution issue key, summary
#       - list of Preconditions
#          - key, summary, definition
#       - for "manual", structured tests:
#         - test steps; related custom fields, attachments, comment, actual result, status
#       - for gherkin (e.g. "cucumber") tests:
#         - scenario type, scenario, examples
#       - for unstructured (e.g. "generic") tests:
#         - definition
#       - Test Run custom fields
#
# Refs: 
# - https://xray.cloud.getxray.app/doc/graphql/gettestruns.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/gettests.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/testrunresults.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/testrun.doc.html
*/

var axios = require('axios');
const { GraphQLClient, gql } = require('graphql-request')
const fs = require('fs');

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var xray_cloud_graphql_url = xray_cloud_base_url + "/graphql";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

var authenticate_url = xray_cloud_base_url + "/authenticate";

// Jira JQL query to define the list of Test Execution issues to export test runs from
jql = "project=CALC and issuetype = 'Test Execution'"

// Revision custom field
revision_cf = "customfield_10028"

async function getTestIds (jql, start, limit) {
  return axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {

    var auth_token = response.data;
    const graphQLClient = new GraphQLClient(xray_cloud_graphql_url, {
        headers: {
          authorization: `Bearer ${auth_token}`,
        },
      })

      const test_ids_query = gql` 
      query
      {
          getTests(jql: "${jql}", limit: ${limit}, start: ${start}) {
            results{
              issueId
            }
          }
      }
`

    return graphQLClient.request(test_ids_query).then(function(data) {
      test_ids = data['getTests']['results'].map(function(t){
        return t['issueId'];
      });

      // console.log(test_ids);
      return test_ids;
    }).catch(function(error) {
      console.log('Error performing query to obtain Test ids: ' + error);
    });
  }).catch( (error) => {
      console.log('Error on Authentication: ' + error);
  });
}

async function getTestExecutionIds (jql, start, limit) {
  return axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    
    var auth_token = response.data;
    const graphQLClient = new GraphQLClient(xray_cloud_graphql_url, {
        headers: {
          authorization: `Bearer ${auth_token}`,
        },
      })

       console.log(auth_token);

      const testexec_ids_query = gql` 
      query
      {
          getTestExecutions(jql: "${jql}", limit: ${limit}, start: ${start}) {
            results{
              issueId
            }
          }
      }
`

    return graphQLClient.request(testexec_ids_query).then(function(data) {
      testexec_ids = data['getTestExecutions']['results'].map(function(t){
        return t['issueId'];
      });

      // console.log(testexec_ids);
      return testexec_ids;
    }).catch(function(error) {
      console.log('Error performing query to obtain Test Execution ids: ' + error);
    });
  }).catch( (error) => {
      console.log('Error on Authentication: ' + error);
  });
}

/*
async function getTestRunsByTestsJQL (jql, start, limit) {
  return axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
      //console.log('success');
      var auth_token = response.data;

      //console.log("AUTH: " + auth_token);

      const graphQLClient = new GraphQLClient(xray_cloud_graphql_url, {
          headers: {
            authorization: `Bearer ${auth_token}`,
          },
        })

        const test_ids_query = gql` 
        query
        {
            getTests(jql: "${jql}", limit: 100, start: 100) {
              results{
                issueId
              }
            }
        }
  `
  
      return graphQLClient.request(test_ids_query).then(function(data) {
        // test_ids = map(lambda t: t['issueId'], data['getTests']['results']))
        test_ids = data['getTests']['results'].map(function(t){
          return '"' + t['issueId'] + '"';
        }).join(',');

        //console.log(test_ids);

        const query = gql` 
        {
          getTestRuns(testIssueIds: [${test_ids}], limit: ${limit}, start: ${start}) {
            total
            start
        
            results{
              id
              status{
                name
                description
              }
              comment
              evidence{
                filename
                downloadLink
              }
              defects
              executedById
              startedOn
              finishedOn
              assigneeId
        
              testType{
                name
              }
        
              steps {
                  id
                  action
                  data
                  result
                  customFields {
                    name
                    value
                  }
                  comment
                  evidence{
                    filename
                    downloadLink
                  }
                  attachments {
                      id
                      filename
                  }
                  defects
                  actualResult
                  status {
                    name
                  }
              }
        
              scenarioType
              gherkin
              examples {
                  id
                  status {
                      name
                      description
                  }
                  duration
              }
        
              unstructured
              
              customFields {
                  id
                  name
                  values
              }
        
              preconditions(limit:10) {
                results{
                    preconditionRef {
                        issueId
                        jira(fields: ["key"])
                    }
                    definition
                }
              }
              test {
                  issueId
                  jira(fields: ["key"])
              }
              testExecution {
                  issueId
                  jira(fields: ["key"])
              }      
            }
          }
        }  
  `
      
        return graphQLClient.request(query).then(function(data) {
            //console.log(JSON.stringify(data['getTestRuns']['results'], undefined, 2))
            return data['getTestRuns']['results'];
            // testruns.push(data['getTestRuns']['results'])
        }).catch(function(error) {
            console.log('Error performing query: ' + error);
        });
      }).catch(function(error) {
        console.log('Error performing first query to obtain project id: ' + error);
      });
  }).catch( (error) => {
      console.log('Error on Authentication: ' + error);
  });
}
*/

async function getTestRuns (testExecIssueIds, start, limit, modifiedSince) {
  return axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {

      var auth_token = response.data;
      const graphQLClient = new GraphQLClient(xray_cloud_graphql_url, {
          headers: {
            authorization: `Bearer ${auth_token}`,
          },
        })

        testexec_ids = testExecIssueIds.map(function(t){
          return '"' + t + '"';
        }).join(',');

        // console.log(testexec_ids);

        const query = gql` 
        {
          getTestRuns(testExecIssueIds: [${testexec_ids}], limit: ${limit}, start: ${start}, modifiedSince: "${modifiedSince}"  ) {
            total
            start
        
            results{
              id
              status{
                name
                description
              }
              comment
              evidence{
                filename
                downloadLink
              }
              defects
              executedById
              startedOn
              finishedOn
              assigneeId
        
              testType{
                name
              }
        
              steps {
                  id
                  action
                  data
                  result
                  customFields {
                    name
                    value
                  }
                  comment
                  evidence{
                    filename
                    downloadLink
                  }
                  attachments {
                      id
                      filename
                  }
                  defects
                  actualResult
                  status {
                    name
                  }
              }
        
              scenarioType
              gherkin
              examples {
                  id
                  status {
                      name
                      description
                  }
                  duration
              }
        
              unstructured
              
              customFields {
                  id
                  name
                  values
              }
        
              preconditions(limit:10) {
                results{
                    preconditionRef {
                        issueId
                        jira(fields: ["key"])
                    }
                    definition
                }
              }
              test {
                  issueId
                  jira(fields: ["key", "issuelinks"])
              }
              testExecution {
                  issueId
                  jira(fields: ["key", "fixVersions", "${revision_cf}"])
									testEnvironments
									testPlans(start: 0, limit: 10) {
										results{
											 jira(fields: ["key"])
										}
									}
              }      
            }
          }
        }  
  `

      return graphQLClient.request(query).then(function(data) {
            //console.log(JSON.stringify(data['getTestRuns']['results'], undefined, 2))
            return data['getTestRuns']['results'];
            // testruns.push(data['getTestRuns']['results'])
      }).catch(function(error) {
        console.log('Error performing query to obtain testruns: ' + error);
      });
  }).catch( (error) => {
      console.log('Error on Authentication: ' + error);
  });
}



/**** main *****/



(async () => {

  let configFile = 'export_testruns.json'
  if (!fs.existsSync(configFile)) {
    fs.writeFileSync(configFile, "{}")
  }
  let config = JSON.parse(fs.readFileSync(configFile));
  let modifiedSince = config['modifiedSince'] || "2021-01-01T00:00:00Z"

  // obtain Test Execution issue ids
  let start = 0
  let limit = 100
  let testexecs = []
  let tes = []
  do {
    tes = await getTestExecutionIds(jql, start, limit)
    start += limit
    testexecs.push(...tes)
  } while (tes.length > 0)

  // obtain the Test Runs for the given Test Execution issue ids, modified since a given data
  let testruns = []
  start = 0
  let trs = []
  do {
    trs = await getTestRuns(testexecs, start, limit, modifiedSince)
    start += limit
    testruns.push(...trs)
  } while (trs.length > 0)
  
  console.log(JSON.stringify(testruns, undefined, 2))
  fs.writeFileSync('testruns.json', JSON.stringify(testruns));

  config['modifiedSince'] = new Date().toISOString().split('.')[0]+"Z"
  fs.writeFileSync(configFile, JSON.stringify(config));
})();
