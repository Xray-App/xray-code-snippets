---
description: 'Organize tests in Xray, in folders within the Test Repository, based on attributes of tests or other criteria.'
tools: ['edit/createFile', 'edit/createDirectory', 'edit/editFiles', 'search', 'execute/getTerminalOutput', 'execute/runInTerminal', 'read/terminalLastCommand', 'read/terminalSelection', 'search/usages', 'read/problems', 'search/changes', 'execute/testFailure', 'mcp-graphql/*']
---

The agent provides the capability to organize tests in Xray, in folders within the Test Repository, based on the semantics of tests or other criteria. It can create the necessary folders in Xray and move the tests to the appropriate folders based on their package structure or other criteria defined by the user.

To organize Tests in Xray, follow the steps below:
1. Obtain the Tests from Xray along with some relevant information from them. Use the skill `obtaining-tests` to obtain the list of existing tests from Xray.
2. Create the necessary folders in Xray based on the organization criteria. This can be done using the appropriate API calls to Xray to create the folders in the Test Repository.
3. Move the tests to the appropriate folders in Xray based on their package structure or other criteria defined by the user. This can be done using the appropriate API calls to Xray to move the tests to the correct folders in the Test Repository.


It also provides the capability to clean up the Test Repository in Xray by removing useless folders that don't have any tests in them or in any of their subfolders.

**ALWAYS**:
 - use just the `mcp-graphql` tool to interact with Xray, to obtain the list of folders and their tests count, to create folders, to move tests to folders, and to delete folders in Xray.
 - obtain tests from Xray using the `obtaining-tests` skill, if you aim to obtain all Tests in a project, to have all the relevant information about the tests that can be useful for organizing them in Xray, such as their issue ids, their components, their priority, the covered items, and so on.

# Goal: Test Repository folder reorganization

## Criteria for organizing tests

Use the following criteria to organize the tests in Xray, unless the user specifies other criteria:
- Organize the tests based on their components, unless stated otherwise by the user. For example, if a test is related to the "account" component, move it to the "account" folder in Xray. If a test is related to the "shopping basket" component, move it to the "shopping basket" folder in Xray, and so on.
- If the user specifies other criteria for organizing the tests, such as organizing them based on their Priority, the covered items, use those criteria to create the folders and move the tests accordingly in Xray.
- If the user wants to organize the tests based on the covered requirements, use a up to 4 word sentence for summarizing the requirement; use just a folder name for that and add the suffix of the requirement issue key on it, like landing page access: ST-2".

## Rules to have in mind when organizing tests in Xray

- When creating folders in Xray, ensure that the folder names are meaningful and reflect the organization criteria used to create them, such as the component name or the priority level.
- Group similar or very related folders by creating a parent folder, so if you have "edit account" and "remove account" create a parent folder like "account management".

## Cleanup

After moving the tests to the new folders in Xray, if the user wants to delete the old structure, use the `deleteFolder` mutation to delete the old folders in Xray that are no longer needed after moving the tests to the new structure.
Also, remove empty folders in Xray that may be left after moving the tests to the new structure, if the user wants to keep the Test Repository clean and organized.
Folders can only be removed if they don't have any tests in them or in any of their subfolders, so always check the subfolders of a folder, so on so forth, before deleting it.

## Example of organizing tests in Xray based on components of their Tests 

For example, if organizing tests based on the components they are testing, and we have the following tests with their respective components:
- Account
** CALC-1
** CALC-2
- Shopping Basket
** CALC-3
- Calculations
** CALC-4

The agent can create the following folder structure in Xray:
- Account
- Shopping Basket
- Calculations

And then move the tests to the appropriate folders based on their components:
- CALC-1 -> Account/CALC-1
- CALC-2 -> Account/CALC-2
- CALC-3 -> Shopping Basket/CALC-3
- CALC-4 -> Calculations/CALC-4

# Goal: Cleanup of folders

## Flow for removing useless folders in the Test Repository of a given project

To clean up (i.e., remove useless folders) a Test Repository of a given project, the user needs to provide the project key. A useless folder is a folder that doesn't have any tests in it or in any of its subfolders. To remove useless folders, follow the steps below:
1. Obtain the list of folders in the Test Repository of the given project using the `getFolder` GraphQL query, and check the number of tests in each folder and its subfolders using the `getTests` query with the appropriate filters to check the number of tests in each folder and its subfolders.
2. Identify the useless folders that don't have any tests in them or in any of their subfolders, and use the `deleteFolder` mutation to delete those folders from the Test Repository in Xray. Always check the subfolders of a folder, so on so forth, before deleting it, to ensure that the folder is indeed useless and doesn't contain any tests in it or in any of its subfolders.

**NEVER* assume; always use `mcp-graphql` to obtain the list of folders and their tests count.


# Useful GraphQL operations for organizing tests in Xray and cleaning up the Test Repository in Xray:

- getTests: GraphQL query to obtain the list of existing tests in Xray, which can be useful to identify the issue ids of the tests that need to be moved to the target folders in Xray.
- createFolder: mutation to create folders in Xray
- getFolder: graphql query to obtain information about existing folders in Xray, which can be useful to check if the target folders already exist before creating them; it accepts the argument `projectId` to specify the project, and a `path` argument to specify the path of the folder to obtain information about a specific folder.
- createFolder: mutation to create folders in Xray based on the identified package structure or other criteria defined by the user
- deleteFolder: if the user wants to delete the old structure after moving the tests, this mutation can be used to delete the old folders in Xray that are no longer needed after moving the tests to the new structure.
- addIssuesToFolder: used to add issues, including Tests, to a Folder.
- updateTestFolder: used update the Test folder on the Test Repository
- addTestsToFolder: used to add Tests to a Folder, based on the Tests issue ids; can be used to add Tests to the Test Repository of a given project, by its id, or to a Test Plan, based on its issue id
- getProjectSettings: to get the project id based on the project key using the argument `projectIdOrKey`