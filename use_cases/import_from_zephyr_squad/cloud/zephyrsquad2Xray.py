import sys, getopt
import re
import xml.etree.ElementTree as ET
import pandas as pd

column = ["Issue ID","Issue Key","Test Type","Test Summary", "Test Priority", "Action","Data","Result","Issue Type", "Labels", "Component", "Description", "Links"]
row = []

def getPriorityValue(priorityName):
    priority = 4

    if priorityName == 'Critical':
        priority = 1
    elif priorityName == 'High':
        priority = 2
    elif priorityName == 'Medium':
        priority = 3
    elif priorityName == 'Low':
        priority = 4
    else:
        priority = 3
    return priority


def appendRows(issueID='', issueKey='', testSummary=None, testPriority=None, action=None, data=None, result=None, issueType=None, description=None, links=None):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": 'Manual',
                "Test Summary": testSummary if testSummary is not None else '',
                "Test Priority": getPriorityValue(testPriority) if testPriority is not None else '3',
                "Action": action if action is not None else '',
                "Data": data if data is not None else '',
                "Result": result if result is not None else '',
                "Issue Type": 'Test',
                "Description": description if description is not None else '',
                "Links": links if links is not None else ''})

def parseZephyrSquad2XrayData(inputfile, outputfile):
    df = pd.read_excel(inputfile, engine='openpyxl')
    
    # loop through the rows using iterrows()
    issueID = 0
    for index, xls_row in df.iterrows():
        issueKey = xls_row['issuekey']
        if issueKey is None or pd.isnull(issueKey):
            appendRows(issueID=issueID, action=xls_row['TestStep'], result=xls_row['Test Result'], data=xls_row['Test Data'], description=xls_row['Description'])
        else:
            issueID=issueID+1
            appendRows(issueID=issueID, testSummary=xls_row['Summary'], testPriority=xls_row['Priority'],action=xls_row['TestStep'],result=xls_row['Test Result'], data=xls_row['Test Data'], description=xls_row['Description'])

    df = pd.DataFrame(row, columns=column)  
    df.set_index("Issue ID", inplace=True)
    df.to_csv(outputfile)

def main(argv):
   inputfile = ''
   outputfile = ''

   try:
        opts, args = getopt.getopt(argv,"hi:o:e:",["ifile=","ofile=","efile="])
        for opt, arg in opts:
            if opt == '-h':
                print ('zephyrsquad2Xray.py -i <Excel_inputfile> -o <CSV_outputfile>')
                sys.exit()
            elif opt in ("-i", "--ifile"):
                inputfile = arg
            elif opt in ("-o", "--ofile"):
                outputfile = arg

   except Exception as err:
       print ("An exception occurred:", err)

   #inputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_zephyr_squad/cloud/ZFJ-issue-export.xlsx'
   #outputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_zephyr_squad/cloud/ZFJ-issue-export.csv'

   if not inputfile or not outputfile:
        print ('One of the input parameters is missing, please use: zephyrsquad2Xray.py -i <Excel_inputfile> -o <CSV_outputfile>')
        sys.exit()

   parseZephyrSquad2XrayData(inputfile, outputfile)

if __name__ == "__main__":
   main(sys.argv[1:])
