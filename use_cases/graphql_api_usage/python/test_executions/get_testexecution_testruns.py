# Obtains a list of the test runs in a given Test Execution, and their status
#
# Returns:
#   - Test Execution: key
#   - for each Test Run:
#       - global status, evidence, comment, defects
#       - related Test issue key, summary
#       - list of Preconditions
#          - key, summary, definition
#       - for "manual", structured tests:
#         - test steps; related custom fields, attachments, comment, actual result, status
#       - for gherkin (e.g. "cucumber") tests:
#         - scenario type, scenario, examples
#       - for unstructured (e.g. "generic") tests:
#         - definition
#       - Test Run custom fields

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

# Provide a GraphQL query
detailed_query = gql(""" 
query
{
    getTestExecutions(jql: "key=%s", limit: 1) {
        results{
          issueId
          jira(fields: ["key"])
    
          testRuns(limit: 100){
            results{
              id
              status{
                name
                description
              }
              comment
              testType{
                name
              }
              evidence{
                filename
                downloadLink
              }
              defects
              executedById
              startedOn
              finishedOn
              assigneeId

              steps {
                  id
                  action
                  data
                  result
                  customFields {
                    name
                    value
                  }
                  comment
                  evidence{
                    filename
                    downloadLink
                  }
                  attachments {
                      id
                      filename
                  }
                  defects
                  actualResult
                  status {
                    name
                  }
              }

              scenarioType
              gherkin
              examples {
                  id
                  status {
                      name
                      description
                  }
                  duration
              }

              unstructured
              
              customFields {
                  id
                  name
                  values
              }

              preconditions(limit:10) {
                results{
                    preconditionRef {
                        issueId
                        jira(fields: ["key"])
                    }
                    definition
                }
              }
              test {
                  issueId
                  jira(fields: ["key"])
                    projectId
 
              }
              
            }
        }

    }
  }
}

""" % testexecution_key
)


# simple query, to obtain the test runs, and their status, for a given Test Execution 
simple_query = gql("""
query
{
    getTestExecutions(jql: "key=%s", limit: 1) {
        results{
                  jira(fields: ["summary"])

          testRuns(limit: 100){
            results{

              test {
                  jira(fields: ["key"])
              }

              status{
                name
              }

            }
        }

    }
  }
}

""" % testexecution_key
)

# Execute the query on the transport
result = client.execute(detailed_query)
print(json.dumps(result, indent=4))
