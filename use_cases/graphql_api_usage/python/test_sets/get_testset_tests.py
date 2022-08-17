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

# Test Set issue key to obtain the info from
testset_key = "CALC-147"

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
    getTestSets(jql: "key=%s", limit: 1) {
        results{
          jira(fields: ["summary"])

          tests(limit: 100){
            results{

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
                }
              } 

              gherkin

              unstructured

            }
        }

    }
  }
}

""" % testset_key
)

# Execute the query on the transport
result = client.execute(query)
print(json.dumps(result, indent=4))
