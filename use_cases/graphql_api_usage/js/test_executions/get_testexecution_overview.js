/*
# Obtains an overview of the Test Execution, including a summarized list of the Tests it includes
# Note: this doesn't include detailed info about the related test runs
#
# Returns:
#   - Test Execution:
#       - issue key, summary, description, fixVerson, assignee, workflow status
#       - test environments
#       - associated Test Plan key
#       - list of tests
#           - key, summary
#           - list of preconditions
#             - key, summary
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

      const query = gql` 
      {
          getTestExecutions(jql: "key=${testexecution_key}", limit: 1) {
            results{
      
              issueId
              projectId
      
              jira(fields: ["key", "summary", "description", "fixVersions", "assignee", "status" ])
      
              testEnvironments
            
              testPlans(limit: 1){
                results {
                  jira(fields: ["key"])
                }
              }
      
              tests(limit: 100) {
                total
                results {
                  issueId
                  jira(fields: ["key", "summary"])
                  testType {name}
                  preconditions(limit: 10) {
                    total
                    results {
                      jira(fields: ["key", "summary"])
                    }
                  }
                  
                }
              }
      
            }
      
          }
      }
      
`

    graphQLClient.request(query).then(function(data) {
        console.log(JSON.stringify(data, undefined, 2))
    }).catch(function(error) {
        console.log('Error performing query: ' + error);
    });
}).catch( (error) => {
    console.log('Error on Authentication: ' + error);
});