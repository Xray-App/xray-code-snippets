
# How to import test cases from TestLink into Xray

## Background

Here you can find some examples showcasing how you can export test cases from TestLink and import them into Xray using Test Case Importer.
This repository supports the tutorial [Importing TestLink test cases using Test Case Importer](https://docs.getxray.app/display/XRAYCLOUDDRAFT/Importing+TestLink+test+cases+using+Test+Case+Importer)

[TestLink](https://testlink.org/) is an open source test management tool.

As TestLink can only export to XML format you can also find one script (one for cloud and one for Server) that converts that XML file into a compatible CSV file, ready to be uploaded.

## Contents

In this repo you'll find a set of examples for cloud and server version of Xray on how to convert XML files exported from TestLink using a script present in the repository and uploading it to Xray. 

In each directory you have (same for cloud and server):
* cloud/Server
    * one_test_case
        * One XML file that is the result of exporting one test case from TestLink
        * One CSV file that is the result of executing the script (testlink2Xray.py)  against the XML file
        * One configuration Json file that you can use to configure the mappings in the Test Case Importer
    * one_test_project
        * One XML file that is the result of exporting one test project (with 2 test suites and several test cases) from TestLink
        * One CSV file that is the result of executing the script (testlink2Xray.py) against the XML file
        * One configuration Json file that you can use to configure the mappings in the Test Case Importer
    * one_test_suite
        * One XML file that is the result of exporting one test suite (with several test cases) from TestLink
        * One CSV file that is the result of executing the script against the XML file
        * One configuration Json file that you can use to configure the mappings in the Test Case Importer
    * testlink2Xray.py/testlink2XrayServer.py - Scripts that will convert XML files to CSV files for cloud and server versions respectively. 

### Script usage

#### Cloud
```Python
python3 testlink2Xray.py -i one_test_case/LoginValidation.testcase.xml -o one_test_case/LoginValidation.testcase.csv
```
The output for cloud version creates preconditions and test sets (if they exist in TestLink) and link those to the tests in one import.

#### Server
```Python
python3 testlink2XrayServer.py -i one_test_case/LoginValidation.testcase.xml -o one_test_case/LoginValidation.testcase.csv
```
The output for server version only creates the tests into Xray. Creation and association of preconditions and test sets must be done separately.


### Source-code

- [Python](./python/)
