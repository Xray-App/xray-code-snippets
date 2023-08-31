
# How to import test cases from Zephyr Squad into Xray

## Background

Here you can find some examples showcasing how you can export test cases from Zephyr Squad and import them into Xray using Test Case Importer.
This repository supports the tutorial [Importing Zephyr Squad test cases using Test Case Importer](https://docs.getxray.app/display/XRAYCLOUD/Importing+Zephyr+Squad+test+cases+using+Test+Case+Importer)

[Zephyr Squad](https://smartbear.com/test-management/zephyr-squad/) is a test management tool inside Jira.

We are using the export to Excel capability of Zephyr Squad and have produced one script that converts that Excel file into a compatible CSV file, ready to be uploaded.

## Contents

In this repo you'll find a set of examples for cloud version of Xray on how to convert Excel files exported from Zephyr Squad using a script present in the repository and uploading it to Xray. 

In each directory you have:
* cloud
    * One Excel file that is the result of exporting from Zephyr Squad
    * One CSV file that is the result of executing the script (zephyrsquad2Xray.py)  against the Excel file
    * One configuration Json file that you can use to configure the mappings in the Test Case Importer
    * zephyrsquad2Xray.py - Scripts that will convert Excel files to CSV files for cloud version. 

### Script usage

#### Cloud
```Python
python3 zephyrsquad2Xray.py -i ZFJ-issue-exporter.xml -o ZFJ-issue-exporter.csv
```
The output for cloud version supports Detail and Lite test types.



### Source-code

- [Python3](.https://www.python.org/downloads/release/python-3115/)
