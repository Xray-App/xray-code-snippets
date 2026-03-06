# Restore Test Repository folder structure


## Initial steps

The user must provide the project / space key, so ask for the Project / Space key, if not provided.

## Flow

Always have this present:
- Use the `mcp-graphql` tool to interact with Xray and organize Tests within the Test Repository of a given project.
- always try to perform bulk operations instead of iterating one by one over each Test
- some folders, by their full path, may already exist; in that case, reuse them

Steps:
- obtain project id based on project key
- obtain all Test ids for each different folder (by full path)
- for each distinct target folder, check if it exists and if not create it
- for each distinct target folder, move all the related Tests

## Useful GraphQL operations for organizing tests and managing folders in Xray

- addTestsToFolder: used to add Tests to a Folder, based on the Tests issue ids; can be used to add Tests to the Test Repository of a given project, by its id, or to a Test Plan, based on its issue id
- addIssuesToFolder: used to add issues, including Tests, to a Folder.
- updateTestFolder: used update the Test folder on the Test Repository
- getTests: GraphQL query to obtain the list of existing tests in Xray, which can be useful to identify the issue ids of the tests that need to be moved to the target folders in Xray.
- getFolder: graphql query to obtain information about existing folders in Xray, which can be useful to check if the target folders already exist before creating them
- createFolder: mutation to create folders in Xray based on the identified package structure or other criteria defined by the user
- deleteFolder: if the user wants to delete the old structure after moving the tests, this mutation can be used to delete the old folders in Xray that are no longer needed after moving the tests to the new structure.
- renameFolder: rename an existing folder in the Test Repository
- getProjectSettings: to get the project id based on the project key


Please have in mind that on GraphQL queries, results are paginated, so you may need to make additional requests using the `start`  and `limit` arguments on the GraphQL invoked function.

## Don't

- don't create auxility scripts to restore the folder structure; instead make GraphQL requests using the tppçs from the connected `mcp-graphql` MCP server
- don't try to use GraphQL operations assuming their existence; only use GraphQL operations mentioned explicitly or obtained by introspecting the schema
 
## Input example

### Text / CSV: comma-delimited output

The input for this operation will be based on a CSV fole having a list of Tests, with thest issue id, then the issue key, followed by its full path location within the Test Repository.

```
123,CALC-1,/
124,CALC-2,UI/authentication
```

## Output example

### Success

Test Repository folder structure restored for 2 Tests.

### Failure

Unable to restore folder structure due to error: <explanation of error>.