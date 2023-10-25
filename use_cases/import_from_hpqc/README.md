
# How to import test cases from HP ALM/QC into Xray

## Background

Here you can find a script that extracts the manual tests from HP ALM/QC using the REST API. The script performs the authentication and subsequent requests to extract all manual tests of a domain and project and generate a CSV file ready to be imported by Xray using Test Case Importer.
This repository supports the tutorial [Importing HP ALM/QC test cases using Test Case Importer](https://docs.getxray.app/pages/viewpage.action?pageId=119232683)

[HP ALM/QC](https://www.microfocus.com/en-us/products/alm-quality-center/overview) is an application lifecycle managment and test management tool.

## Contents

In this repo you'll find one example that work with cloud and server version of Xray on how to convert XML files exported from TestRail using a script present in the repository and uploading it to Xray. 

The directory have several files:
* One ini file that holds the username, password and endpoint of your HP ALM/QC
* One CSV file that is the result of executing the script (hpqc2Xray.py)
* One configuration Json file (importConfiguration.json) that you can use to configure the mappings in the Test Case Importer (Cloud)
* One configuration txt file (importConfiguration.txt) that you can use to configure the mappings in the Test Case Importer (Server)
* hpqc2Xray.py - Scripts that will extracts the tests from the REST API and convert it to CSV files. 

### Script usage
Update the hpqc_config.ini file with the username, password and endpoint of your HP ALM/QC instance.
Extract, from HP ALM/QC, the Domain and Project that you want to export tests from.

#### Cloud/Server
```Python
python3 hpqc2Xray.py -p DEMO -d TEST -o comicEstore.csv
```
Parameters accepted by the script:
* -p - Project from where you want to extract the tests in HP ALM/QC
* -d - Domain from where you want to extract the tests in HP ALM/QC
* -o - Name of the output CSV file to import into Xray

The script extract all manual tests from a Domain/Test of HP ALM/QC and generates a CSV compatible file to be imported into Xray.

Notice:
* Only manual tests are extracted from HP ALM/QC.
* Priorities are not defined in HP ALM/QC so they need to be reviewed after importing int Xray.

### Source-code

- [Python](./python/)
