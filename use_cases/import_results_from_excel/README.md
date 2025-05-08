# CSV to Junit XML

This code provides a Python script that can be used to convert from a CSV file to a Junit XML report. This can be used to easily push results to Xray / Jira, using the proper JUnit related REST API endpoint.
The `csv_to_junit.py` script generates an enhanced JUnit XML report

## Use case

Sometimes we may want to report test results in a Excel or Google Spreadsheet, for convenience for example.
To get visibility of those test results, the user can do something like:

- write the test results on a spreadsheet, having the columsn for: test name, test result, test ouput; the test result should be either PASSED or FAILED.
- in Google Spreadsheets or Excel, export it to CSV using comma as a delimeter
- run the Python `csv_to_junit.py` script
- import results to Xray, targeting the Junit XML import results endpoint

## How to

```bash
pip install -r requirements.txt
```

```bash
python csv_to_junit.py sample.csv junit.xml
```

After generating the JUnit XML report having the test results, these can be easily pushed to Xray (DC or Cloud) by invoking the proper REST API endpoints.

This folder provides examples of how to push it using some basic Unix/Linux shell scripts that should be adapted to your own context.

## Sample CSV

Please see `sample.csv` as an example of the layout of the CSV that should be generated with the test results.