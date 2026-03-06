# Obtaining requirements and coverage information

## Flow

The user must provide the project / space key, so ask for the Project / Space key, if not provided.
The user may, optionally, identify that wants to show coverage for current open sprint in a project. 
The user may also identify that wants to calculate coverage for a given version / release.


Use the `mcp-graphql` tool to obtain covered items (e.g., Story, Epic and similar issues) and their status based on coverage information. For this use, the `getCoverableIssues` query with the jql argument such as getCoverableIssues(jql: "project=ST")

Example of query considering project with key "ST".

```graphql
query
    {
        getCoverableIssues(jql: "project=ST", limit: 100) {
            results{
                issueId
                jira(fields: ["key", "summary", "components", "priority"])
                status(isFinal: true) {
                    name
                }
            }
    }
 }
```

If the user has chosen to show coverage for a open sprint in a project, then the jql to use should be something similar to `project = ST and sprint in openSprints()`, in this case assuming ST as the project key.
If the user has referred a version / release, then include the `fixVersion` on the jql, such as `project= ST and fixVersion = v1.0` for version v1.0.

Please have in mind that results are paginated, so you may need to make additional requests using the `start`  and `limit` arguments on the GraphQL invoked function.

To build the URL For the Overall Requirement/Test Coverage report, you need to use the project key; the project.id argument is static.

The bar below bust show colors based on the different statuses. The shown percentage after the bar corresponds to the tests passing percentage.

Example of output:

[Overall requirement coverage](https://remotejirainstance.atlassian.net/plugins/servlet/ac/com.xpandit.plugins.xray/test-coverage-report-page?project.key=ST&project.id=10001):
30% OK ✅, 50% NOK ❌, 20% UNCOVERED ⚠️
🟩🟩🟩🟥🟥⬜⬜⬜⬜⬜ 30% OK

⚠️ UNCOVERED:
- [CALC-1](remotejirainstance.atlassian.net/browse/CALC-1): homepage
* Priority: Medium
* Components: core, UI
- [CALC-2](remotejirainstance.atlassian.net/browse/CALC-2): billing
* Priority: Medium
* Components: core, UI

❌ NOK:
- [CALC-3](remotejirainstance.atlassian.net/browse/CALC-3): login
* Priority: High
* Components: UI


# Don'ts

- never use other tools or commands then the provided ones earlier
