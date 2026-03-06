---
agent: 'agent'
model: GPT-4o
tools: []
description: 'Authenticate on Xray API'
---
Your goal is to authenticate on Xray cloud API to obtain a token, using the client id and client secret credentials

## Steps

Make a HTTP request to Xray cloud API to authenticate and obtain a token to be used on GraphQL API requests later on.

To make authentication request, use `curl` such as

```bash
curl -H "Content-Type: application/json" -X POST --data '{ "client_id": "215FFD69FE4644728C72180000000000","client_secret": "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f020000000000" }' "https://eu.xray.cloud.getxray.app/api/v2/authenticate"| tr -d '"'
```
##  Output

Print the authentication token, such as:

`Token: 1234500000000000000000000`

# Don'ts

- never use other tools or commands then the provided oness earlier
