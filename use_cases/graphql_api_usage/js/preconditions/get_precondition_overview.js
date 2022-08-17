/*
# Obtains info about a given Precondition, including the list of Tests using it
#
# Returns:
#   - Precondition:
#       - issue key, summary, description
#       - definition
#       - list of tests requiring it
#           - key, summary
#           - test type
*/

var axios = require('axios');
const { GraphQLClient, gql } = require('graphql-request')

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var xray_cloud_graphql_url = xray_cloud_base_url + "/graphql";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

var authenticate_url = xray_cloud_base_url + "/authenticate";

// Precondition issue key to obtain the info from
precondition_key = "CALC-151"

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
        getPreconditions(jql: "key=${precondition_key}", limit: 1) {
          results{
            issueId
            projectId
    
            jira(fields: ["key", "summary", "description"])
    
            preconditionType {
              name
              kind
            }
            definition
    
            tests(limit: 100) {
              total
              results {
                issueId
                jira(fields: ["key", "summary"])
                testType {name}
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