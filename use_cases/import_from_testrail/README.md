
# How to import test cases from TestRail into Xray

## Background

Here you can find some examples showcasing how you can export test cases from TestRail and import them into Xray using Test Case Importer.
This repository supports the tutorial [Importing TestRail test cases using Test Case Importer](https://docs.getxray.app/display/XRAYCLOUD/Importing+TestRail+test+cases+using+Test+Case+Importer)

[TestRail](https://www.testrail.com/) is a test management tool.

We are using the export to XML capability of TestRail so you can also find one script (one for cloud and one for Server) that converts that XML file into a compatible CSV file, ready to be uploaded.

## Contents

In this repo you'll find a set of examples for cloud and server version of Xray on how to convert XML files exported from TestRail using a script present in the repository and uploading it to Xray. 

In each directory you have (same for cloud and server):
* cloud/Server
    * One XML file that is the result of exporting from TestRail
    * One CSV file that is the result of executing the script (testrail2Xray.py)  against the XML file
    * One configuration Json file that you can use to configure the mappings in the Test Case Importer
    * testrail2Xray.py - Scripts that will convert XML files to CSV files for cloud and server versions respectively. 

### Script usage

#### Install dependencies
```Python
pip install -r requirements.txt
```

#### Cloud
```Python
python3 testrail2Xray.py -i comic_estore.xml -o comicEstore.csv
```
The output for cloud version creates preconditions and test sections (if they exist in TestRail) and link those to the tests in one import. It supports Manual, Automated and Exploratory tests.

#### Server
```Python
python3 testrail2XrayServer.py -i comic_estore.xml -o comicEstore.csv
```
Another option is also available to complete the links sent in the XMl by TestRail. This endpoint entry will be concatenated with the URL from the links in steps.

```Python
python3 testrail2XrayServer.py -i comic_estore.xml -o comicEstore.csv -e https://testrail.com/
```

The output for server version only creates the tests into Xray and the Test Repo structure. Creation and association of preconditions must be done separately. It only supports Manual Tests.


### Source-code

- [Python 3.12.0](https://www.python.org/downloads/release/python-3120/)
