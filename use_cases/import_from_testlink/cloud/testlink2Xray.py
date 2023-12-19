import sys, getopt
import re
import xml.etree.ElementTree as ET
import pandas as pd

column = ["Issue ID","Issue Key","Test Type","Test Summary", "Test Priority", "Action","Data","Result","Test Set","Precondition","Issue Type", "Precondition Type"]
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

def appendRows(issueID='', issueKey='', testType='', testSummary='', testPriority='', action='', data='', result='', testSet='', issueType='', precondition=''):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": 'Manual' if testType=='1' else 'Automated',
                "Test Summary": cleanTags(testSummary),
                "Test Priority": testPriority,
                "Action": cleanTags(action),
                "Data": '',
                "Result": cleanTags(result),
                "Test Set": testSet if testSet else '',
                "Precondition": precondition if precondition else '',
                "Issue Type": issueType,
                "Precondition Type": ''})

def appendPrecondition(issueID='', precondition=''):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": '',
                "Test Summary": cleanTags(precondition),
                "Test Priority": '',
                "Action": '',
                "Data": '',
                "Result": '',
                "Test Set": '',
                "Precondition": '',
                "Issue Type": 'precondition',
                "Precondition Type": 'Manual'})

def appendTestset(issueID='', testSet=''):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": '',
                "Test Summary": cleanTags(testSet),
                "Test Priority": '',
                "Action": '',
                "Data": '',
                "Result": '',
                "Test Set": '',
                "Precondition": '',
                "Issue Type": 'testset',
                "Precondition Type": ''})

def handleTestSuites(root, issueID, outputfile):
    # When exporting from a Test Suite Testlink adds one level
    testsuites = root.findall('testsuite')
    if root.tag == 'testsuite' and len(testsuites) <= 1:
        # Parse Testcases
        name = root.attrib['name']
        appendTestset(issueID,testSet=name)
        issueID=handleTestCases(root, issueID+1, issueID, outputfile=outputfile)
    else:
        for testsuite in testsuites:
            # Parse Testcases
            name = testsuite.attrib['name']
            appendTestset(issueID,testSet=name)
            issueID=handleTestCases(testsuite, issueID+1, issueID, outputfile=outputfile)

def handleTestCases(root, issueID, testsetid, outputfile):
    preconditionID=''
    # When exporting from a Test Suite Testlink adds one level
    if root.tag != 'testcase':
        root = root.findall('testcase')

    for testcase in root:
        # Parse Testcases
        name = testcase.attrib['name']
        summary = testcase.find('summary').text
        precondition = testcase.find('preconditions').text
        if precondition:
            appendPrecondition(issueID=issueID, precondition=precondition)
            preconditionID=issueID
            issueID=issueID+1

        test_Type = testcase.find('execution_type').text
        test_priority = testcase.find('importance').text
        first_step = True
        hasSteps = False
        
        # Parse Steps  
        for step in testcase.findall('steps/step'):
            hasSteps = True
            action = step.find('actions').text
            expectedResult = step.find('expectedresults').text
            if first_step:
                appendRows(issueID=issueID,testType=test_Type,testSummary=summary,testPriority=test_priority,action=action,result=expectedResult,precondition=preconditionID, testSet=testsetid)
                first_step = False
            else:
                appendRows(issueID=issueID,testType=test_Type, action=action,result=expectedResult)
        
        if not hasSteps:
            appendRows(issueID=issueID,testType=test_Type,testSummary=summary,testPriority=test_priority,precondition=preconditionID, testSet=testsetid)
        issueID = issueID+1  
        
        df = pd.DataFrame(row, columns= column)  
        df.set_index("Issue ID", inplace=True)
        df.to_csv(outputfile)
    return issueID


def parseTestlink2XrayData(inputfile, outputfile):
    # Parsing XML file
    xmlParse = ET.parse(inputfile)
    root = xmlParse.getroot()
    issueID = 1

    if root.tag != 'testcase' and root.tag != 'testcases':
        handleTestSuites(root=root, issueID=issueID, outputfile=outputfile)
    else:
        handleTestCases(root=root, issueID=issueID, testsetid='', outputfile=outputfile)

    
def main(argv):
   inputfile = ''
   outputfile = ''

   try:
        opts, args = getopt.getopt(argv,"hi:o:",["ifile=","ofile="])
        for opt, arg in opts:
            if opt == '-h':
                print ('testlink2Xray.py -i <XML_inputfile> -o <CSV_outputfile>')
                sys.exit()
            elif opt in ("-i", "--ifile"):
                inputfile = arg
            elif opt in ("-o", "--ofile"):
                outputfile = arg
   except Exception as err:
       print ("An exception occurred:", err)

   #inputfile='RegressionTestSuite.xml'
   #outputfile='RegressionTestSuite2.csv'
   if not inputfile or not outputfile:
    print ('One of the input parameters is missing, please use: testlink2Xray.py -i <XML_inputfile> -o <CSV_outputfile>')
    sys.exit()

   parseTestlink2XrayData(inputfile, outputfile)

if __name__ == "__main__":
   main(sys.argv[1:])
