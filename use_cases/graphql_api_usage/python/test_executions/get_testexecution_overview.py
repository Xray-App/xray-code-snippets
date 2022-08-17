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


from gql import gql, Client
from gql.transport.aiohttp import AIOHTTPTransport
#from gql.transport.requests import RequestsHTTPTransport
import requests
import json
import os

xray_cloud_base_url = "https://xray.cloud.getxray.app/api/v2"
xray_cloud_graphql_url = "https://xray.cloud.getxray.app/api/v2/graphql"
client_id = os.getenv('CLIENT_ID', "215FFD69FE4644728C72182E00000000")
client_secret = os.getenv('CLIENT_SECRET',"1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000")

# Test Execution issue key to obtain the info from
testexecution_key = "CALC-141"

# endpoint doc for authenticating and obtaining token from Xray Cloud: https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2
headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
auth_data = { "client_id": client_id, "client_secret": client_secret }
response = requests.post(f'{xray_cloud_base_url}/authenticate', data=json.dumps(auth_data), headers=headers)
auth_token = response.json()

# Select your transport with a defined url endpoint
transport = AIOHTTPTransport(
  url=xray_cloud_graphql_url,
  headers={'Authorization': f'Bearer {auth_token}'}
)

# Create a GraphQL client using the selected transport
client = Client(transport=transport, fetch_schema_from_transport=True)

# GraphQL query
query = gql("""
query
{
    getTestExecutions(jql: "key=%s", limit: 1) {
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

""" % testexecution_key
)

# Execute the query on the transport
result = client.execute(query)
print(json.dumps(result, indent=4))
