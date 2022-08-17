/*
# Obtains the test runs for a given list of Tests
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

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var xray_cloud_graphql_url = xray_cloud_base_url + "/graphql";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

var authenticate_url = xray_cloud_base_url + "/authenticate";

// Jira JQL query to define the list of Test issues to export test runs from
// jql = "project=CALC and issuetype = Test"
jql = "key=CALC-23"

axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    console.log('success');
    var auth_token = response.data;

    console.log("AUTH: " + auth_token);

    const graphQLClient = new GraphQLClient(xray_cloud_graphql_url, {
        headers: {
          authorization: `Bearer ${auth_token}`,
        },
      })

      const test_ids_query = gql` 
      query
      {
          getTests(jql: "${jql}", limit: 100) {
            results{
              issueId
            }
          }
      }
`
 
    graphQLClient.request(test_ids_query).then(function(data) {
      // test_ids = map(lambda t: t['issueId'], data['getTests']['results']))
      test_ids = data['getTests']['results'].map(function(t){
        return '"' + t['issueId'] + '"';
      }).join(',');

      console.log(test_ids);

      const query = gql` 
      {
        getTestRuns(testIssueIds: [${test_ids}], limit: 100) {
          total
      
      
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
      // console.log(JSON.stringify(data, undefined, 2))
    
      graphQLClient.request(query).then(function(data) {
          console.log(JSON.stringify(data, undefined, 2))
      }).catch(function(error) {
          console.log('Error performing query: ' + error);
      });
    }).catch(function(error) {
      console.log('Error performing first query to obtain project id: ' + error);
    });
}).catch( (error) => {
    console.log('Error on Authentication: ' + error);
});