
# How to import test cases from Qase into Xray

## Background

Here you can find some examples showcasing how you can export test cases from Qase and import them into Xray using Test Case Importer.
This repository supports the tutorial [Importing Qase test cases using Test Case Importer](https://docs.getxray.app/display/XRAYCLOUD/Importing+Qase+test+cases+using+Test+Case+Importer)

[Qase](https://qase.io/) is a test management tool.

We are using the export to CSV capability of Qase and have produced one script (one for cloud and one for Server) that converts that CSV file into a compatible CSV file, ready to be uploaded.

## Contents

In this repo you'll find a set of examples for cloud and server version of Xray on how to convert CSV files exported from Qase using a script present in the repository and uploading it to Xray. 

In each directory you have (same for cloud and server):
* cloud/Server
    * One CSV file that is the result of exporting from Qase
    * One CSV file that is the result of executing the script (qase2Xray.py)  against the CSV file
    * One configuration Json (Cloud) or txt (Server) file that you can use to configure the mappings in the Test Case Importer
    * qase2Xray.py - Scripts that will convert CSV files to CSV files for cloud and server versions respectively. 

### Script usage

#### Cloud
```Python
python3 qase2Xray.py -i qase_export.csv -o qase_xray_results.csv
```
The output for cloud version creates preconditions and link those to the tests in one import. It supports Scripted and BDD test types.

#### Server
```Python
python3 qase2Xray.py -i qase_export.csv -o qase_xray_results.csv
```

The output for server version only creates the tests into Xray. Creation and association of preconditions must be done separately. It only supports Manual Tests.


### Source-code

- [Python3](https://www.python.org/downloads/release/python-3115/)
