/*
# Obtains a list of the test runs in a given Test Execution, and their status
#
# Returns:
#   - Test Execution: key
#   - for each Test Run:
#       - global status, evidence, comment, defects
#       - related Test issue key, summary
#       - list of Preconditions
#          - key, summary, definition
#       - for "manual", structured tests:
#         - test steps; related custom fields, attachments, comment, actual result, status
#       - for gherkin (e.g. "cucumber") tests:
#         - scenario type, scenario, examples
#       - for unstructured (e.g. "generic") tests:
#         - definition
#       - Test Run custom fields
*/

var axios = require('axios');
const { GraphQLClient, gql } = require('graphql-request')

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var xray_cloud_graphql_url = xray_cloud_base_url + "/graphql";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

var authenticate_url = xray_cloud_base_url + "/authenticate";

// Test Execution issue key to obtain the info from
testexecution_key = "CALC-141"

axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    console.log('success');
    var auth_token = response.data;

    console.log("AUTH: " + auth_token);

    const graphQLClient = new GraphQLClient(xray_cloud_graphql_url, {
        headers: {
          authorization: `Bearer ${auth_token}`,
        },
      })

      const detailed_query = gql` {
        getTestExecutions(jql: "key=${testexecution_key}", limit: 1) {
            results{
              issueId
              jira(fields: ["key"])
        
              testRuns(limit: 100){
                results{
                  id
                  status{
                    name
                    description
                  }
                  comment
                  testType{
                    name
                  }
                  evidence{
                    filename
                    downloadLink
                  }
                  defects
                  executedById
                  startedOn
                  finishedOn
                  assigneeId
    
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
                        projectId
     
                  }
                  
                }
            }
    
        }
      }
    }
    
`

// simple query, to obtain the test runs, and their status, for a given Test Execution 
const simple_query = gql`
{
  getTestExecutions(jql: "key=%s", limit: 1) {
      results{
                jira(fields: ["summary"])

        testRuns(limit: 100){
          results{

            test {
                jira(fields: ["key"])
            }

            status{
              name
            }

          }
      }

  }
}
}
`

    graphQLClient.request(detailed_query).then(function(data) {
        console.log(JSON.stringify(data, undefined, 2))
    }).catch(function(error) {
        console.log('Error performing query: ' + error);
    });
}).catch( (error) => {
    console.log('Error on Authentication: ' + error);
});