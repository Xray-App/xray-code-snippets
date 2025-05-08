# submit from the command line
BASE_URL=https://xray.cloud.getxray.app
PROJECT_KEY=CALC
token=$(curl -H "Content-Type: application/json" -X POST --data @"cloud_auth.json" "$BASE_URL/api/v2/authenticate"| tr -d '"')
curl -H "Content-Type: application/xml" -X POST -H "Authorization: Bearer $token"  --data @"example.xml" "$BASE_URL/api/v2/import/execution/junit?projectKey=$PROJECT_KEY"

