/*
# Obtains a summarized list of the Tests included in a given Test Repository folder
#
# Returns:
#   - Test:
#       - issue key, summary, description
#       - test steps; related custom fields, attachments
#       - list of preconditions
#          - key, summary
#
# Refs: 
# - https://xray.cloud.getxray.app/doc/graphql/test.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/gettests.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/foldersearchinput.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/getprojectsettings.doc.html
*/

var axios = require('axios');
const { GraphQLClient, gql } = require('graphql-request')

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var xray_cloud_graphql_url = xray_cloud_base_url + "/graphql";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";

var authenticate_url = xray_cloud_base_url + "/authenticate";

// Test Repository's project, by its key
project_key = "CALC"

// folder in Test Repo to extract the tests from
folder_path = "/compB"

axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
    console.log('success');
    var auth_token = response.data;

    console.log("AUTH: " + auth_token);

    const graphQLClient = new GraphQLClient(xray_cloud_graphql_url, {
        headers: {
          authorization: `Bearer ${auth_token}`,
        },
      })

      const project_id_query = gql` 
      {
        getProjectSettings(projectIdOrKey: "${project_key}") {
          projectId
        }
      }
`
 
    graphQLClient.request(project_id_query).then(function(data) {

      project_id = data['getProjectSettings']['projectId']
      const query = gql` 
      {
        getTests(projectId: "${project_id}", folder: { path: "${folder_path}", includeDescendants: true }, limit: 100) {
          total
          results {
            issueId
      
            jira(fields: ["key", "summary", "description"])
      
            testType {name}
      
            folder {
                path
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