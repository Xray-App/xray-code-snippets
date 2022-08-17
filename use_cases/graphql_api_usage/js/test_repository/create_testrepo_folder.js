/*
# Creates a folder in a Test Repository of a given project
#
# Returns:
#
# Refs: 
# - https://xray.cloud.getxray.app/doc/graphql/createfolder.doc.html
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

// full path of the folder to be created in the Test Repository
folder_path = "/parent/child_folder/dummy"

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
      const mutation = gql` 
      mutation CreateFolder($projectId: String!, $path: String!)
      {
        createFolder(
            projectId: $projectId,
            path: $path
        ) {
            folder {
                name
                path
                testsCount
            }
            warnings
        }
      }
`
      // console.log(JSON.stringify(data, undefined, 2))
    
      const variables = {
        projectId: project_id,
        path: folder_path,
      }
      graphQLClient.request(mutation, variables).then(function(data) {
          console.log(JSON.stringify(data, undefined, 2))
      }).catch(function(error) {
          console.log('Error performing mutation: ' + error);
      });
    }).catch(function(error) {
      console.log('Error performing first query to obtain project id: ' + error);
    });
}).catch( (error) => {
    console.log('Error on Authentication: ' + error);
});