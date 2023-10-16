
# How to import test cases from qTest into Xray

## Background

Here you can find some examples showcasing how you can export test cases from qTest and import them into Xray using Test Case Importer.
This repository supports the tutorial [Importing qTest test cases using Test Case Importer](https://docs.getxray.app/display/XRAYCLOUD/Importing+qTest+test+cases+using+Test+Case+Importer)

[qTest](https://www.tricentis.com/products/unified-test-management-qtest) is a test management tool.

We are using the export to excel capability of qTest and you can also find one script (one for cloud and one for Server) that converts that excel file into a compatible CSV file, ready to be uploaded.

## Contents

In this repo you'll find a set of examples for cloud and server version of Xray on how to convert excel files exported from qTest using a script present in the repository and uploading it to Xray. 

In each directory you have (same for cloud and server):
* cloud/Server
    * One excel file that is the result of exporting from qTest
    * One CSV file that is the result of executing the script (qtest2Xray.py)  against the excel file
    * One configuration Json file that you can use to configure the mappings in the Test Case Importer
    * qtest2Xray.py - Scripts that will convert excel files to CSV files for cloud and server versions respectively. 

### Script usage

#### Cloud
```Python
python3 qtest2Xray.py -i qTest-Regression-TestCase.xls -o qTest-Regression-TestCase.csv
```
The output for cloud version creates preconditions and link those to the tests in one import. It supports Manual, Automated, Performance, Scenario and Automation.

#### Server
```Python
python3 qtest2XrayServer.py -i qTest-Regression-TestCase.xls -o qTest-Regression-TestCase.csv
```

The output for server version only creates the tests into Xray. Creation and association of preconditions must be done separately. It only supports Manual Tests.


### Source-code

- [Python](./python/)
