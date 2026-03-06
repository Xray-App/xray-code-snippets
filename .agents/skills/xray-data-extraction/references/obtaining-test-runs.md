# Obtaining Test Runs


## Flow

The user must provide the project / space key, so ask for the Project / Space key, if not provided.
The user may, optionally, identify that wants information for current open sprint in a project. 
The user may also identify that wants to obtain testing data for a given version / release.

Use the `mcp-graphql` tool to obtain Test Runs based on Test Executions. For this use, the `getTestExecutions` query with the jql argument such as getTestExecutions(jql: "project=ST")

Example of query considering project with key "ST".

```graphql
query
    {
    getTestExecutions(jql: "project = 'ST'",limit: 10) {
        total
        start
        limit
        results {
            issueId
            jira(fields: ["summary", "key"])
            testRuns(limit: 100){
                results{
                    id
                    status{
                        name
                    }
                    test {
                        issueId
                        jira(fields: ["key", "summary"])
    
                    }  
                }
            }
        }
    }
 }
```

If the user has chosen to show information for a open sprint in a project, then the jql to use should be something similar to `project = ST and sprint in openSprints()`, in this case assuming ST as the project key.
If the user has referred a version / release, then include the `fixVersion` on the jql, such as `project= ST and fixVersion = v1.0` for version v1.0.

Please have in mind that results are paginated, so you may need to make additional requests using the `start`  and `limit` arguments on the GraphQL invoked function.



Example of output:

Test Runs overview, grouped by status:
PASSED ✅: 30
FAILED ❌: 20
TODO ⚠️: 50 
OTHER: 10

Test Runs for Test Execution [CALC-123](remotejirainstance.atlassian.net/browse/CALC-123):
- Test [CALC-1](remotejirainstance.atlassian.net/browse/CALC-1): PASSED
- Test [CALC-2](remotejirainstance.atlassian.net/browse/CALC-2): FAILED
...

Test Runs for Test Execution [CALC-567](remotejirainstance.atlassian.net/browse/CALC-567):
- Test [CALC-3](remotejirainstance.atlassian.net/browse/CALC-3): PASSED
- Test [CALC-4](remotejirainstance.atlassian.net/browse/CALC-4): FAILED
...
