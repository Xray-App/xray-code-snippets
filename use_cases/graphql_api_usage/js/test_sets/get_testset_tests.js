/*
# Obtains a detailed list of the Tests included in a given Test Set
#
# Returns:
#   - Test Set: key
#   - for each Test:
#        - issue key
#        - summary
#        - test type and its kind
#        - for structured tests (e.g., manual scripted test cases):
#           - step, data, expected result, attachments
#           - test step custom fields
#        - for unstructured tests (e.g., automated and exploratory tests):
#           - definition
#        - for gherkin tests (e.g., cucumber):
#           - gherkin definition
*/

var axios = require('axios');
const { GraphQLClient, gql } = require('graphql-request')

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var xray_cloud_graphql_url = xray_cloud_base_url + "/graphql";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

var authenticate_url = xray_cloud_base_url + "/authenticate";

// Test Set issue key to obtain the info from
testset_key = "CALC-147"

axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    console.log('success');
    var auth_token = response.data;

    console.log("AUTH: " + auth_token);

    const graphQLClient = new GraphQLClient(xray_cloud_graphql_url, {
        headers: {
          authorization: `Bearer ${auth_token}`,
        },
      })

      const query = gql` {

        getTestExecutions(jql: "key=CALC-141", limit: 1) {
            results{
              jira(fields: ["summary"])
    
              testRuns(limit: 100){
                results{
    
                  test {
                      jira(fields: ["key", "summary"])
                      testType {
                          name
                          kind
                      }
    
                      steps {
                        id
                        data
                        action
                        result
                        attachments {
                          id
                          filename
                          downloadLink
                        }
                      } 
    
                      gherkin
    
                      unstructured
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