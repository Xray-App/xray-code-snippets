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

# Test Repository's project, by its key
project_key = "CALC"

# folder in Test Repo to extract the tests from
folder_path = "/compB"

# obtain the project id from the given project key
query = gql("""
query
{
    getProjectSettings(projectIdOrKey: "%s") {
      projectId
    }
}

""" % project_key
)
result = client.execute(query)
project_id = result['getProjectSettings']['projectId']

# GraphQL query
query = """
query
{
  getTests(projectId: "%s", folder: { path: "%s", includeDescendants: true }, limit: 100) {
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

""" % (project_id, folder_path)

result = client.execute(gql(query))
print(json.dumps(result, indent=4))
