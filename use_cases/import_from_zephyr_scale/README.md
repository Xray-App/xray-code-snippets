
# How to import test cases from Zephyr Scale into Xray

## Background

Here you can find some examples showcasing how you can export test cases from Zephyr Scale and import them into Xray using Test Case Importer.
This repository supports the tutorial [Importing Zephyr Scale test cases using Test Case Importer](https://docs.getxray.app/display/XRAYCLOUD/Importing+Zephyr+Scale+test+cases+using+Test+Case+Importer)

[Zephyr Scale](https://smartbear.com/test-management/zephyr-scale/) is a test management tool inside Jira.

We are using the export to XML capability of Zephyr Scale and have produced one script (one for cloud and one for Server) that converts that XML file into a compatible CSV file, ready to be uploaded.

## Contents

In this repo you'll find a set of examples for cloud and server version of Xray on how to convert XML files exported from Zephyr Scale using a script present in the repository and uploading it to Xray. 

In each directory you have (same for cloud and server):
* cloud/Server
    * One XML file that is the result of exporting from Zephyr Scale
    * One CSV file that is the result of executing the script (zephyrscale2Xray.py)  against the XML file
    * One configuration Json file that you can use to configure the mappings in the Test Case Importer
    * zephyrscale2Xray.py - Scripts that will convert XML files to CSV files for cloud and server versions respectively. 

### Script usage

#### Cloud
```Python
python3 zephyrscale2Xray.py -i atm-exporter.xml -o atm-exporter.csv
```
The output for cloud version creates preconditions and test folders (if they exist in Zephyr Scale) and link those to the tests in one import. It supports Steps-by-Steps, Steps (Manual) and BDD.

#### Server
```Python
python3 zephyrscale2XrayServer.py -i atm-exporter.xml -o atm-exporter.csv
```
Another option is also available to complete the links sent in the XMl by Zephyr Scale. This endpoint entry will be concatenated with the URL from the links in steps.

```Python
python3 zephyrscale2XrayServer.py -i atm-exporter.xml -o atm-exporter.csv -e https://zephyrscale.com/
```


The output for server version only creates the tests into Xray and the Test Repo structure. Creation and association of preconditions must be done separately. It only supports Manual Tests.


### Source-code

- [Python3](.https://www.python.org/downloads/release/python-3115/)
