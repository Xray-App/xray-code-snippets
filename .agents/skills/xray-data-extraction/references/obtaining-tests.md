# Obtaining Tests


## Flow


### Getting Tests of a project

The user must provide the project / space key, so ask for the Project / Space key, if not provided.


Use the `mcp-graphql` tool to obtain Tests. For this use, the `getTests` query with the jql argument such as getTests(jql: "project=ST")

Example of query considering project with key "ST".

```graphql
query
{
    getTests(jql: "project = 'ST'", limit: 100) {
        total
        start
        limit
        results {
            issueId
            jira(fields: ["summary", "key", "components", "priority"])

            testType {
                name
                kind
            }

            unstructured

            coverableIssues(limit: 1) {
                results {
                    issueId
                    jira(
                        fields: ["key", "summary", "components", "priority", "parent"]
                    )
                }
            }

            folder{
                name
                path
            }
        }
    }
}
```

If the user wants just tests of a given test type, it's possible to pass that on the GraphQL query.

```graphql
query
{
    getTests(jql: "project = 'ST'", testType: testType: { name: "Generic" },, limit: 100) {
        total
        start
        limit
        results {
            issueId
            jira(fields: ["summary", "key", "components", "priority"])

            testType {
                name
                kind
            }

            unstructured

            coverableIssues(limit: 1) {
                results {
                    issueId
                    jira(
                        fields: ["key", "summary", "components", "priority", "parent"]
                    )
                }
            }

            folder{
                name
                path
            }
        }
    }
}
```

Please have in mind that results are paginated, so you may need to make additional requests using the `start`  and `limit` arguments on the GraphQL invoked function.

Also, GraphQL queries have a maximum limit of 100 results! So never exceed that limit on queries!

The output should be JSON, unless the user has asked something else otherwise.


### Getting tests for a Test Execution

Use the `mcp-graphql` tool to obtain Tests. For this use, the `getTestExecutions` query with the jql argument such as getTestExecutions(jql: "key=ST-297")

Example of query considering project with key "ST".

```graphql
   query
    {
        getTestExecutions(jql: "key=ST-297", limit: 1) {
            results{
            issueId
            jira(fields: ["key"])
										
                tests(limit: 100) {
                    results{
                        issueId
                        jira(fields:["key"])
                        
                        testType {
                            name
                            kind
                        }

                        unstructured
                        
                    }
                }
			
        }
    }
 }
```

### Getting tests for a Test Plan

Use the `mcp-graphql` tool to obtain Tests. For this use, the `getTestPlans` query with the jql argument such as getTestPlans(jql: "key=ST-297")

Example of query considering project with key "ST".

```graphql
   query
    {
        getTestPlans(jql: "key=ST-3", limit: 1) {
            results{
            issueId
            jira(fields: ["key"])
										
                tests(limit: 100) {
                    results{
                        issueId
                        jira(fields:["key"])
                        
                        testType {
                            name
                            kind
                        }

                        unstructured
                        
                    }
                }
			
        }
    }
 }
```

### Getting tests based on some other criteria

Try to use GraphQL query `getTests`, together with the `jql` argument to get the relevant tests, using valid JQL.


## Output example

There are 2 possible outputs: JSON (default) or text, comma-delimited.

### JSON output (more complete)

Generate a JSON object containing a list of Tests, with the Test issue id, issue key, summary, its test type name, then generic definition (also known as "unstructured"), summary, priority, components (as an array of strings).
Include also the covered issues, if any, with essential information about them, including: issue id, issue key, summary, name of priority, name of components (as an array of strings).

Note: for tests that are non generic, leave the generic definition field empty on the list. Example follows next.


```json
{

   "tests": [
        {
        "issueId":123,
        "key":"CALC-1",
        "url":"remotejirainstance.atlassian.net/browse/CALC-1",
        "testType":"manual",
        "genericDefinition":"",
        "summary":"valid login scenario",
        "priority": "Medium"
        "components": ["core"],
        "coveredIssues": {
            "issueId": 22
            "key": "CALC-22"
            "summary":"valid login scenario",
            "priority": "Medium",
            "components": ["authentication"]
        }
    },
    {
        "issueId":124,
        "key":"CALC-2",
        "url":"remotejirainstance.atlassian.net/browse/CALC-2",
        "testType":"generic",
        "genericDefinition":"generic definition",
        "summary":"invalid login scenario",
        "priority": "High",
        "components": ["integration", "UI"],
        "coveredIssues": {
            "issueId": 22
            "key": "CALC-22"
            "summary":"login",
            "priority": "Medium",
            "components": ["authentication"]
        }
    } 
   ]
}
```

### Text, comma-delimited output

Generate a list having the Test issue id, then the issue key, followed by its test type name, then generic definition (also known as "unstructured"), then Summary.
For tests that are non generic, leave the generic definition field empty on the list. Example follows next.

- 123,[CALC-1](remotejirainstance.atlassian.net/browse/CALC-1),manual,,valid login scenario
- 124,[CALC-2](remotejirainstance.atlassian.net/browse/CALC-2),generic,generic definition,invalid login scenario



