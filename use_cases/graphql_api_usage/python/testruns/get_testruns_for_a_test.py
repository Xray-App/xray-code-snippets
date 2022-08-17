# Obtains the test runs for a given list of Tests
# 
# Returns:
#   - for each Test Run:
#       - global status, evidence, comment, defects
#       - related Test issue key, summary
#       - related Test Execution issue key, summary
#       - list of Preconditions
#          - key, summary, definition
#       - for "manual", structured tests:
#         - test steps; related custom fields, attachments, comment, actual result, status
#       - for gherkin (e.g. "cucumber") tests:
#         - scenario type, scenario, examples
#       - for unstructured (e.g. "generic") tests:
#         - definition
#       - Test Run custom fields
#
# Refs: 
# - https://xray.cloud.getxray.app/doc/graphql/gettestruns.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/gettests.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/testrunresults.doc.html
# - https://xray.cloud.getxray.app/doc/graphql/testrun.doc.html

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


# Jira JQL query to define the list of Test issues to export test runs from
# jql = "project=CALC and issuetype = Test"
jql = "key=CALC-23"

# obtain the issue ids for the given Tests
query = gql("""
query
{
    getTests(jql: "%s", limit: 100) {
      results{
        issueId
      }
    }
}

""" % jql
)
result = client.execute(query)
test_ids = list( map(lambda t: t['issueId'], result['getTests']['results']))

# we'll need the test_ids formatted as strings delimited by command, on the GraphQL query
formatted_test_ids = ",".join( list(map(lambda id: """ "%s" """ % id, test_ids)) )
print(formatted_test_ids)

# GraphQL query
query = gql(""" 
query
{
  getTestRuns(testIssueIds: [%s], limit: 100) {
    total


    results{
      id
      status{
        name
        description
      }
      comment
      evidence{
        filename
        downloadLink
      }
      defects
      executedById
      startedOn
      finishedOn
      assigneeId

      testType{
        name
      }

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
      }
      testExecution {
          issueId
          jira(fields: ["key"])
      }      
    }
  }
}

""" % formatted_test_ids
)

# Execute the query on the transport
result = client.execute(query)
print(json.dumps(result, indent=4))
