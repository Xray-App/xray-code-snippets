import sys, getopt
import re
import xml.etree.ElementTree as ET
import pandas as pd

column = ["Issue ID","Issue Key","Test Type","Test Summary", "Test Priority", "Action","Data","Result","Issue Type", "Precondition", "Precondition Type", "Description", "Unstructured Definition", "Gherkin Definition"]
row = []

def getPriorityValue(priorityName):
    priority = 4

    if priorityName == 'Urgent':
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

def getTestType(type):
    testType = 'Manual'
    validTypes = ['Manual', 'Automation', 'Performance', 'Scenario']
    if type is not None and type in validTypes:
        if type == 'Automation':
            testType = 'Generic'
        elif type == 'Scenario':
            testType = 'Cucumber'
        else:
            testType = 'Manual'

    return testType

def appendRows(issueID='', issueKey='', testType=None, testSummary=None, testPriority=None, action=None, data=None, result=None, issueType=None, description=None, precondition=None, unstrusturedDefinition=None, gherkinDefinition=None):
    noActionTypes = ['Scenario', 'Automation']
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": getTestType(testType),
                "Test Summary": testSummary if testSummary is not None else '',
                "Test Priority": getPriorityValue(testPriority) if testPriority is not None else '3',
                "Action": action if action is not None and testType not in noActionTypes else '',
                "Data": data if data is not None and testType not in noActionTypes else '',
                "Result": result if result is not None and testType not in noActionTypes else '',
                "Precondition": precondition if precondition is not None and precondition != 0 else '',
                "Precondition Type": '',
                "Unstructured Definition": unstrusturedDefinition if unstrusturedDefinition is not None and testType == 'Automation' else '',
                "Issue Type": 'Test',
                "Description": description if description is not None else '',
                "Gherkin Definition": gherkinDefinition if gherkinDefinition is not None and testType == 'Scenario' else ''})

def getShortPrecondition(precondition):
    precond = ''
    
    if precondition is not None:
        #precondition = precondition
        if '\n' in precondition:
            precond = precondition.split('\n',255)[0]
        else:
            precond = precondition[:254]
    return precond

def getPreconditionType(type):
    preconditionType = 'Manual'
    if type is not None:
        if type == 'Scenario':
            preconditionType = 'Cucumber'
        elif type == 'Automation':
            preconditionType = 'Generic'

    return preconditionType

def appendPrecondition(issueID='', precondition=None, type=None):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": '',
                "Test Summary": getShortPrecondition(precondition) if precondition is not None else '',
                "Test Priority": '',
                "Action": '',
                "Data": '',
                "Result": '',
                "Precondition": '',
                "Issue Type": 'precondition',
                "Precondition Type": getPreconditionType(type) if type is not None else 'Manual',
                "Unstructured Definition": '',
                "Description": precondition if precondition is not None else '',
                "Links":''})
    
def parseqTest2XrayData(inputfile, outputfile):
    df = pd.read_excel(inputfile, sheet_name=1)
    
    # loop through the rows using iterrows()
    issueID = 0
    lastTestID = ''
    testID = ''
    for index, xls_row in df.iterrows():
        preconditionID = 0
        testID = xls_row['Id']
        precondition = xls_row['Precondition']
        testType = xls_row['Type']
        testStep = xls_row['Test Step #']

        if testID is not lastTestID:
            issueID=issueID+1

        if precondition is not None and not pd.isna(precondition) and testStep == 1:
            appendPrecondition(issueID=issueID, precondition=precondition, type=testType)
            preconditionID=issueID
            issueID=issueID+1
        if testID is not lastTestID and testStep == 1:
            #First step including Preconditon (if exists)
            appendRows(issueID=issueID, testType=xls_row['Type'], testSummary=xls_row['Name'], testPriority=xls_row['Priority'], action=xls_row['Test Step Description'], result=xls_row['Test Step Expected Result'], data='', description=xls_row['Description'], precondition=preconditionID, unstrusturedDefinition='', gherkinDefinition=xls_row['Test Step Description'])
        elif testID is lastTestID and testStep > 1:
            #Steps
            appendRows(issueID=issueID, testType=xls_row['Type'], testSummary=xls_row['Name'], testPriority=xls_row['Priority'],action=xls_row['Test Step Description'],result=xls_row['Test Step Expected Result'], data='', unstrusturedDefinition='', gherkinDefinition=xls_row['Test Step Description'])
            
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

   #inputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_qtest/cloud/qTest-Regression-TestCase.xls'
   #outputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_qtest/cloud/qTest-Regression-TestCase.csv'

   if not inputfile or not outputfile:
        print ('One of the input parameters is missing, please use: qTest2Xray.py -i <Excel_inputfile> -o <CSV_outputfile>')
        sys.exit()

   parseqTest2XrayData(inputfile, outputfile)

if __name__ == "__main__":
   main(sys.argv[1:])
