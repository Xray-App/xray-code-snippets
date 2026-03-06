---
agent: 'agent'
model: GPT-4o
tools: ['jira', 'mcp-graphql']
description: 'Show Test Plan summary'
---
Your goal is to show a very brief overall progress of the given Test Plan, detailing the percentage of tests passing and other statuses.


## Flow
Ask for the Test Plan issue key, if not provided.

Use the `mcp-graphql` tool to obtain the Tests on the Test Plan and their status. For this use, the `getTestPlans` query with the jql argument such as getTestPlans(jql: "CALC-123")

Example of query:

```graphql
query
    {
        getTestPlans(jql: "key=ST-3", limit: 10) {
            results{
							issueId
							jira(fields: ["key"])
							tests(limit: 100){
								results{
									jira(fields: ["key"])
									status{
										name
									}
								}
							}

        }
    }
 }
```

Please have in mind that results are paginated, so you may need to make additional requests using the `start`  and `limit` arguments on the GraphQL invoked function.

The bar below bust show colors based on the different statuses. The shown percentage after the bar corresponds to the tests passing percentage.

Example of output: 

Progress of Test Plan [CALC-123](remotejirainstance.atlassian.net/browse/CALC-123): regression testing
30% tests passing ✅, 20% failing ❌, 50% to do ⚠️
🟩🟩🟩🟥🟥⬜⬜⬜⬜⬜ 30% PASSING


❌ Failed tests:
- [CALC-1](remotejirainstance.atlassian.net/browse/CALC-1): valid login scenario
- [CALC-2](remotejirainstance.atlassian.net/browse/CALC-1): invalid login scenario

# Don'ts

- never use other tools or commands then the provided oness earlier
