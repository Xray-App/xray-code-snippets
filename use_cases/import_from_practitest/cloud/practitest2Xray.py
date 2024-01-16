import sys, getopt
import re
import xml.etree.ElementTree as ET
import pandas as pd

column = ["Issue ID","Issue Key","Test Type","Test Summary", "Test Priority", "Action","Data","Result","Issue Type", "Precondition", "Precondition Type", "Description", "Unstructured Definition", "Gherkin Definition"]
row = []

CLEANR = re.compile(r'<.*?>|&([a-z0-9]+|#[0-9]{1,6}|#x[0-9a-f]{1,6});')
EMPTYSPACES = re.compile(r'\n|\r')
QUOTES = re.compile(r'\&quot\;')

def cleanTags(txt):
    if txt:
        cleanTxt = re.sub(QUOTES, '"', txt)
        cleanTxt = re.sub(CLEANR, '', cleanTxt)
        cleanTxt = re.sub(EMPTYSPACES, ' ', cleanTxt)
    else:
        cleanTxt = ''

    return cleanTxt

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
    validTypes = ['BDDTest', 'ScriptedTest', 'ApiTest', 'ExploratoryTest']
    if type is not None and type in validTypes:
        if type == 'ApiTest':
            testType = 'Generic'
        elif type == 'BDDTest':
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
                "Unstructured Definition": unstrusturedDefinition if unstrusturedDefinition is not None and testType == 'ApiTest' else '',
                "Issue Type": 'Test',
                "Description": description if description is not None else '',
                "Gherkin Definition": gherkinDefinition if gherkinDefinition is not None and testType == 'BDDTest' else ''})

def getShortPrecondition(precondition):
    precond = ''
    
    if precondition is not None:
        if '\n' in precondition:
            precond = precondition.split('\n',254)[0]
        else:
            precond = precondition[:254]
    return cleanTags(precond)

def getPreconditionType(type):
    preconditionType = 'Manual'
    if type is not None:
        if type == 'BDDTest':
            preconditionType = 'Cucumber'
        elif type == 'ApiTest':
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
    
def parsepractitest2XrayData(inputfile, outputfile):
    df = pd.read_csv(inputfile)
    
    issueID = 1
    lastTestType = ''
    testID = ''
    for index, xls_row in df.iterrows():
        preconditionID = 0
        testID = xls_row['id']
        precondition = xls_row['Preconditions']
        testType = xls_row['Test_type']
        testStep = xls_row['Step position']

        if testStep != ' ':
            testStep = int(xls_row['Step position'])

        if pd.isna(testID):
            issueID=issueID
        else:
            issueID=issueID+1

        if precondition is not None and not pd.isna(precondition) and testStep == 1:
            appendPrecondition(issueID=issueID, precondition=precondition, type=testType)
            preconditionID=issueID
            issueID=issueID+1

        if (testType == 'ScriptedTest' or (lastTestType == 'ScriptedTest' and pd.isna(testType))):
            action=xls_row['Step description']
            if pd.isna(action):
                action = 'Test step imported from another test.'
                
            if testStep == 1:
                #First step including Preconditon (if exists)
                appendRows(issueID=issueID, testType=testType, testSummary=xls_row['Name'], testPriority='', action=action, result=xls_row['Step expected_results'], data='', description=xls_row['Description'], precondition=preconditionID, unstrusturedDefinition='', gherkinDefinition=xls_row['Scenario'])
            elif testStep > 1:
                #Steps
                appendRows(issueID=issueID, testType=testType, testSummary=xls_row['Name'], testPriority='',action=action,result=xls_row['Step expected_results'], data='', unstrusturedDefinition='', gherkinDefinition=xls_row['Scenario'])
            if not pd.isna(testType):
                lastTestType = testType
        elif testType == 'BDDTest':
            issueID = issueID+1
            appendRows(issueID=issueID, testType=testType, testSummary=xls_row['Name'], testPriority='', action='', result='', data='', description=xls_row['Description'], precondition=preconditionID, unstrusturedDefinition='', gherkinDefinition=xls_row['Scenario'])
            lastTestType = testType

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
                print ('practitest2Xray.py -i <CSV_inputfile> -o <CSV_outputfile>')
                sys.exit()
            elif opt in ("-i", "--ifile"):
                inputfile = arg
            elif opt in ("-o", "--ofile"):
                outputfile = arg

   except Exception as err:
       print ("An exception occurred:", err)

   #inputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_practitest/cloud/practitest_export.csv'
   #outputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_practitest/cloud/practitest_xray_results.csv'

   if not inputfile or not outputfile:
        print ('One of the input parameters is missing, please use: practitest2Xray.py -i <CSV_inputfile> -o <CSV_outputfile>')
        sys.exit()

   parsepractitest2XrayData(inputfile, outputfile)

if __name__ == "__main__":
   main(sys.argv[1:])
