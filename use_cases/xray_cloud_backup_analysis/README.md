# Analyze and provide insights on Xray Cloud data based on backups
 
## Overview

This script is a rough attempt to process data from Xray backups and provide some insights based on that.
It provides:

* total storage usage
* storage usage by Jira project
* count of some relevant Xray entities
* top Test Plans with most folders
* top Test Plans with most Tests
* total empty Test Plans (i.e., without Tests)
* total empty Manual Tests (i.e., without steps)
* and more...


## How to

Create two zipped backups from [Xray global settings page](https://docs.getxray.app/display/XRAYCLOUD/Global+Settings%3A+Backup), one for the metadata and one for the Xray-managed attachments; 

![image](https://github.com/Xray-App/xray-code-snippets/assets/34485244/44e8b730-bd88-4dcc-b9e7-d0d053359a96)

You can also do it from the [REST API](https://docs.getxray.app/display/XRAYCLOUD/Backup+-+REST+v2).

Then extract the previous zipped files to two directories that will be used as the source for the script ahead.

Make sure you obtain an "API key" (i.e., a pair of Client ID + Client Secret) from [Xray global settings](https://docs.getxray.app/display/XRAYCLOUD/Global+Settings%3A+API+Keys), as it will be necessary to obtain some information that is not present in the backup files.

## Running directly

### Prerequisites

* Ruby 2.7.x


Use the `` Ruby script, which has the following syntax:

```bash
Usage: bundle exec ruby process_xray_cloud_backup.rb [options]
    -m METADATA_DIRECTORY            Directory with extracted Xray Cloud metadata backup composed of multiple JSON files
    -a ATTACHMENTS_DIRECTORY,        Directory with extracted Xray Cloud attachments backup composed of multiple attachment files
        --metadata
    -i, --id CLIENT_ID               Xray Cloud client id from the related API key
    -s, --secret CLIENT_SECRET       Xray Cloud client secret from the related API key
    -h, --help                       Prints this help
```


```bash
bundle install
bundle exec ruby process_xray_cloud_backup.rb  -m ~/Downloads/dfc07383-782b-3a88-9f9b-bf458a9f1e70 -a ~/Downloads/dfc07383-782b-3a88-9f9b-bf458a9f1e70_attachment -i DA2258616A5944198E9BE40000000000 -s 5bae1aa5b49e5d263781da54ba55cc7deebd7840c68fe2fdfd2a070000000000

```

## Running using Docker

You can also run this script using docker.

### Prerequisites

* Docker
* docker image built using the code from this repo

```
docker build -t xray-cloud-data-analysis .
```

Then you can run the docker image:

```bash
./run_docker.sh   ~/Downloads/dfc07383-782b-3a88-9f9b-bf458a9f1e70  ~/Downloads/dfc07383-782b-3a88-9f9b-bf458a9f1e70_attachment  DA2258616A5944198E9BE40000000000 5bae1aa5b49e5d263781da54ba55cc7deebd7840c68fe2fdfd2a070000000000
```


## Output example


```bash
========================================================================
========== Top test runs by attachment size ============================

entity		testKey		testExecKey		size (MB)		archived
TESTRUN		FIN-82		FIN-54		4		
TEST		EWB-321				3		
TESTRUN		EWB-18		EWB-328		3		
TESTRUN		BTW-161		BTW-162		2		
						2		
TESTRUN		CALC-243		CALC-244		1		
TESTRUN		EWB-515		EWB-523		1		
TESTRUN		EWB-509		EWB-510		1		
TESTRUN		XT-506		XT-562		1		
TESTRUN		XT-506		XT-558		1		

========================================================================

No test or test execution found for attachment {"19520c89-4d66-4891-b508-0d0614f02e82"=>{"tenant"=>"dfc07383-782b-3a88-9f9b-bf458a9f1e70", "filename"=>"TestSession_2021-11-23_11-52-56-631.pdf", "compressed"=>"false", "size"=>2469686}}
========================================================================

Project		Size (MB)
XT		30
EWB		20
FIN		12
BTW		9
CALC		6
ET		5
CALC-archived		2
========================================================================
Total		86 MB
========================================================================
========================================================================
Total tests: 717
Test types: ["Manual", "Generic", "Cucumber", "Exploratory"]
Manual		383
Generic		222
Cucumber		72
Exploratory		26
Generic		3
Exploratory		7
Manual		4

Total tests having no steps: 399
Total preconditions: 179
Total tests having no preconditions: 562
Total preconditions being referenced by tests: 155
Total orphan preconditions: 28

Total test plans: 56
Total test plans having no tests: 12
3 test plans having the most tests:
XT-509		19
EWB-705		16
XT-295		15

3 test plans having the most folders in the Board:
XT-509		7
BTW-154		3
EWB-338		3
3 test plans having the most Test Executions:
XT-4		66
XT-509		52
XT-3		50
Total test executions: 904
Total test runs: 3016
Test environments: ["Production", "qa", "firefox", "chrome", "TestCloud", "Linux", "Chrome", "Windows", "NODE_15.x", "test", "staging", "prod", "firefox", "jdk11", "chrome", "net50", "Dark", "Production"]
```
