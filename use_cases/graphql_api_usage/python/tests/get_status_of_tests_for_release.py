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


# Jira's JQL query to define the list of Tests to process
jql = "key=CALC-3"


# GraphQL query
query_template = """
query
{
  getTests(jql: "%s", limit: 1) {
    total
    results {
      issueId

      jira(fields: ["key", "summary", "description"])

      testType {name}

      folder {
          path
      }

      status(isFinal: true, version: "%s") {
        name
      }
    }
  }
}

"""

# obtain the current status of the selected Tests, for v1.0
print("============= v1.0 =============")
query = gql( (query_template % (jql, "v1.0")) )
result = client.execute(query)
print(json.dumps(result, indent=4))
print(list( map(lambda t: { t['jira']['key'] : t['status']['name'] }, result['getTests']['results'])))

# obtain the current status of the selected Tests, for v2.0
print("============= v2.0 =============")
query = gql( query_template % (jql, "v2.0") )
result = client.execute(query)
print(json.dumps(result, indent=4))
print(list( map(lambda t: { t['jira']['key'] : t['status']['name'] }, result['getTests']['results'])))
