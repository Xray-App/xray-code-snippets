/*
# Obtains a summarized list of the Tests included in a given Test Plan, along with their current status
#
# Returns:
#   - Test Plan:
#       - issue key, summary, description, fixVerson, assignee, workflow status
#       - list of tests
#           - key, summary
#           - list of preconditions
#             - key, summary
#           - consolidated status of the Test for this TP (i.e., "latest" test result linked to this TP)
*/

var axios = require('axios');
const { GraphQLClient, gql } = require('graphql-request')

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var xray_cloud_graphql_url = xray_cloud_base_url + "/graphql";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

var authenticate_url = xray_cloud_base_url + "/authenticate";

// Test Plan issue key to obtain the info from
testplan_key = "CALC-148"

axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    console.log('success');
    var auth_token = response.data;

    console.log("AUTH: " + auth_token);

    const graphQLClient = new GraphQLClient(xray_cloud_graphql_url, {
        headers: {
          authorization: `Bearer ${auth_token}`,
        },
      })

      const testplan_id_query = gql` 
      {
        getTestPlans(jql: "key=${testplan_key}", limit: 1) {
          results{
            issueId
          }
        }
    }
`
 
    graphQLClient.request(testplan_id_query).then(function(data) {

      testplan_id = data['getTestPlans']['results'][0]['issueId']
      const query = gql` 
      {
        getTestPlan(issueId: "${testplan_id}") {
    
            issueId
            projectId
    
            jira(fields: ["key", "summary", "description", "fixVersions", "assignee", "status" ])
    
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
    
                status(isFinal: true, testPlan: "${testplan_id}") {
                  name
                }
                
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
      console.log('Error performing first query to obtain testplan id: ' + error);
    });
}).catch( (error) => {
    console.log('Error on Authentication: ' + error);
});