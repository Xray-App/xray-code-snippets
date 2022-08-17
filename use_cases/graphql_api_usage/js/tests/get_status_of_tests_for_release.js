/*
# Obtains a summarized list of the Tests, along with their current status for a given release
#
# Returns:
#   - for each Test:
#       - issue key, summary, description
#       - test type
#       - status of the Test for a given scope  (i.e., for a given release)
#
# Refs: 
# - https://xray.cloud.getxray.app/doc/graphql/test.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/gettest.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/gettests.doc.html
*/

var axios = require('axios');
const { GraphQLClient, gql } = require('graphql-request')

var xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2";
var xray_cloud_graphql_url = xray_cloud_base_url + "/graphql";
var client_id = process.env.CLIENT_ID || "215FFD69FE4644728C72182E00000000";
var client_secret = process.env.CLIENT_SECRET || "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000";
var authenticate_url = xray_cloud_base_url + "/authenticate";


async function get_status_of_test_for_release(jql, release) {

  return axios.post(authenticate_url, { "client_id": client_id, "client_secret": client_secret }, {}).then( (response) => {
      console.log('success');
      var auth_token = response.data;

      // console.log("AUTH: " + auth_token);

      const graphQLClient = new GraphQLClient(xray_cloud_graphql_url, {
          headers: {
            authorization: `Bearer ${auth_token}`,
          },
        })

        const query = gql` 
        {
          getTests(jql: "${jql}", limit: 1) {
            total
            results {
              issueId
        
              jira(fields: ["key", "summary", "description"])
        
              testType {name}
        
              folder {
                  path
              }
        
              status(isFinal: true, version: "${release}") {
                name
              }
            }
          }
        }  
  `
      return graphQLClient.request(query).then((data) => {
        console.log("_______________");
          console.log(JSON.stringify(data, undefined, 2))
          console.log("_______________");
          return data;
      }).catch(function(error) {
          console.log('Error performing query: ' + error);
      });
  }).catch( (error) => {
      console.log('Error on Authentication: ' + error);
  });

}


function printTestStatus(data) {
  var tests = data['getTests']['results'].map(function(t) {
    return [ t['jira']['key'], t['status']['name'] ];
  });
  console.log(tests);
}

/***************************************/

// Jira's JQL query to define the list of Tests to process
jql = "key=CALC-3";

(async () => {
  var data = await get_status_of_test_for_release(jql, "v1.0");
  printTestStatus(data);
  data = await get_status_of_test_for_release(jql, "v2.0");
  printTestStatus(data);  
})().catch( (error) => {
  console.log('Error: ' + error);
});
