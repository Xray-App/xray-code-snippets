#!/bin/bash

FILE=example.xml
PROJECT_KEY=CALC
JIRA_BASE_URL=https://example.com
JIRA_USERNAME=someuser
JIRA_PASSWORD=somepassword
curl -H "Content-Type: multipart/form-data" -u $JIRA_USERNAME:$JIRA_PASSWORD -F "file=@$FILE" "$JIRA_BASEURL/rest/raven/2.0/import/execution/junit?projectKey=$PROJECT_KEY"
