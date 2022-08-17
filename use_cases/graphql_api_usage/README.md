
# Usage of GraphQL API in Xray Cloud

## Background

Here you can find some examples showcasing the usage of Xray cloud's GraphQL API.
This type of API is only available for Xray cloud.
Xray cloud provides a [REST API](https://docs.getxray.app/display/XRAYCLOUD/Version+2), focused on a few operations, while GraphQL API is more flexible in terms of the queries you can make and even the operations you may want to perform. GraphQL doesn't replace the REST API though.

Usage GraphQL is [detailed on Xray documentation site](https://docs.getxray.app/display/XRAYCLOUD/GraphQL+API).

GraphQL requests are authenticated. An initial [authentication request](https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2) must be done to obtain a token that will be used in the GraphQL requests. The authentication request is part of Xray's REST API. Therefore, to use GraphQL, we also need to use REST API in an initial step, even if it's just for this purpose.

## Code snippets

In this repo you'll find a set of example scripts, showcasing:

* Preconditions
  * overview
* Tests
  * overview
  * status of test(s) for a release/version
* Test Sets
  * overview
  * detailed list of related Tests
* Test Executions
  * overview
  * detailed list of related Tests
  * detailed list of Testruns
* Test Plans
  * overview, including consolidated status of Tests within this Test Plan
  * detailed list of related Tests, and list of linked Test Executions
* Testruns
  * detailed list of Testruns for one (or more) Test(s)
* Test Repository
  * create folder
  * get detailed Tests in a folder


### Source-code

- [Python](./python/)
- [JavaScript](./js/)
