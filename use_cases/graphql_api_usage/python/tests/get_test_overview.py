# Obtains a detailed overview of the Test, including the list of required preconditions
#
# Returns:
#   - Test:
#       - issue key, summary, description
#        - test type and its kind
#        - for structured tests (e.g., manual scripted test cases):
#           - step, data, expected result, attachments
#           - test step custom fields
#        - for unstructured tests (e.g., automated and exploratory tests):
#           - definition
#        - for gherkin tests (e.g., cucumber):
#           - gherkin definition
#       - list of preconditions
#          - key, summary
#
# Refs: 
# - https://xray.cloud.getxray.app/doc/graphql/test.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/gettest.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/gettests.doc.html

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

# Test issue key to obtain the info from
test_key = "CALC-3"

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


query = gql("""
query
{
    getTests(jql: "key=%s", limit: 1) {
      results{
        issueId
      }
    }
}

""" % test_key
)

# obtain the Test issue id from the given Test issue key, as issue ids are not visible to users
result = client.execute(query)
test_id = result['getTests']['results'][0]['issueId']


# GraphQL query
query = gql("""
query
{
    getTest(issueId: "%s") {
        issueId
        projectId

        jira(fields: ["key", "summary", "description" ])

        testType {name}

        folder {
            path
        }

        steps {
            id
            action
            data
            result
            attachments {
                id
                filename
                downloadLink
            }
            customFields {
              id
              name
              value
            }
        }

        scenarioType
        gherkin

        unstructured

        preconditions(limit: 10) {
          total
          results {
            jira(fields: ["key", "summary"])
          }
        }
    }
}

""" % (test_id)
)

# Execute the query on the transport
result = client.execute(query)
print(json.dumps(result, indent=4))
