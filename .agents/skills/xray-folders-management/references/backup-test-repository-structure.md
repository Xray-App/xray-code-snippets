# Backup Test Repository folder structure


## Flow

The user must provide the project / space key, so ask for the Project / Space key, if not provided.


Use the `mcp-graphql` tool to obtain Tests and their location within the Test Repository. For this use, the `getTests` query with the jql argument such as getTests(jql: "project=ST")

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
            jira(fields: ["summary", "key"])

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
    getTests(jql: "project = 'ST'", testType: testType: { name: "Generic" }, limit: 100) {
        total
        start
        limit
        results {
            issueId
            jira(fields: ["summary", "key"])

            folder{
                name
                path
            }
        }
    }
}
```



Please have in mind that results are paginated, so you may need to make additional requests using the `start`  and `limit` arguments on the GraphQL invoked function.

The output should be JSON, unless the user has asked something else otherwise.


## Output example

### Text / CSV: comma-delimited output

Generate a list having the Test issue id, then the issue key, followed by its full path location within the Test Repository.

```
123,CALC-1,/
124,CALC-2,UI/authentication
```

The user may want to save this later on to a CSV file.
