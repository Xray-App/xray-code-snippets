import sys, getopt
import re
import xml.etree.ElementTree as ET
import pandas as pd

column = ["Issue ID","Issue Key","Test Summary", "Test Priority", "Action","Data","Result", "Description"]
row = []

def getPriorityValue(priorityName):
    priority = 4
    inPriorityName = priorityName.strip().lower()
    if inPriorityName == 'urgent':
        priority = 1
    elif inPriorityName == 'high':
        priority = 2
    elif inPriorityName == 'medium':
        priority = 3
    elif inPriorityName == 'low':
        priority = 4
    else:
        priority = 3
    return priority


def appendRows(issueID='', issueKey='', testSummary=None, testPriority=None, action=None, data=None, result=None, issueType=None, description=None, unstrusturedDefinition=None, gherkinDefinition=None):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Summary": testSummary if testSummary is not None else '',
                "Test Priority": getPriorityValue(testPriority) if testPriority is not None else '3',
                "Action": action if action is not None else '',
                "Data": data if data is not None else '',
                "Result": result if result is not None else '',
                "Description": description if description is not None else ''})

    
def parseqTest2XrayData(inputfile, outputfile):
    df = pd.read_excel(inputfile, sheet_name=1)
    
    # loop through the rows using iterrows()
    issueID = 0
    lastTestID = ''
    testID = ''
    for index, xls_row in df.iterrows():
        preconditionID = 0
        testID = xls_row['Id']
        #precondition = xls_row['Precondition']
        testType = xls_row['Type']
        testStep = xls_row['Test Step #']

        if testType == 'Manual':
            if testID is not lastTestID:
                issueID=issueID+1

            if testID is not lastTestID and testStep == 1:
                #First step including Preconditon (if exists)
                appendRows(issueID=issueID, testSummary=xls_row['Name'], testPriority=xls_row['Priority'], action=xls_row['Test Step Description'], result=xls_row['Test Step Expected Result'], data='', description=xls_row['Description'])
            elif testID is lastTestID and testStep > 1:
                #Steps
                appendRows(issueID=issueID, testSummary=xls_row['Name'], testPriority=xls_row['Priority'],action=xls_row['Test Step Description'],result=xls_row['Test Step Expected Result'], data='')
                
            lastTestID = testID

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
                print ('qtest2Xray.py -i <Excel_inputfile> -o <CSV_outputfile>')
                sys.exit()
            elif opt in ("-i", "--ifile"):
                inputfile = arg
            elif opt in ("-o", "--ofile"):
                outputfile = arg

   except Exception as err:
       print ("An exception occurred:", err)

   #inputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_qtest/server/qTest-Regression-TestCase.xls'
   #outputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_qtest/server/qTest-Regression-TestCase.csv'

   if not inputfile or not outputfile:
        print ('One of the input parameters is missing, please use: qTest2Xray.py -i <Excel_inputfile> -o <CSV_outputfile>')
        sys.exit()

   parseqTest2XrayData(inputfile, outputfile)

if __name__ == "__main__":
   main(sys.argv[1:])
