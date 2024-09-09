import sys, getopt
import re
import xml.etree.ElementTree as ET
import pandas as pd

column = ["Issue ID","Issue Key","Test Type","Test Summary", "Test Priority", "Action","Data","Result","Issue Type", "Precondition", "Precondition Type", "Description", "Unstructured Definition", "Gherkin Definition", "Test Repo"]
row = []

CLEANR = re.compile(r'<.*?>|&([a-z0-9]+|#[0-9]{1,6}|#x[0-9a-f]{1,6});')
EMPTYSPACES = re.compile(r'\n|\r')
QUOTES = re.compile(r'\&quot\;')
CLEANSTARTINGNUM = re.compile(r'\d\.')

def cleanTags(txt):
    if txt:
        cleanTxt = re.sub(QUOTES, '"', txt)
        cleanTxt = re.sub(CLEANR, '', cleanTxt)
        cleanTxt = re.sub(EMPTYSPACES, ' ', cleanTxt)
    else:
        cleanTxt = ''

    return cleanTxt

def convert_to_jira_markdown(text):
    if not text:
        return text

    # Convert \\n to \n
    text = re.sub(r'\\n', r'\n', text)

    # Convert \\" to "
    text = re.sub(r'\\"', r'"', text)

    # Convert \\/ to /
    text = re.sub(r'\\/', r'/', text)

    # Convert \t to &nbsp;
    text = re.sub(r'\\t', r'\t', text)

    # Convert bold (**text**) to Jira bold (*text*)
    text = re.sub(r'\*\*(.*?)\*\*', r'*\1*', text)

    # Convert underline (<u>text</u>) to Jira underline (+text+)
    text = re.sub(r'<u>(.*?)</u>', r'+\1+', text)

    # Convert code blocks (`code`) to Jira code syntax {{code}}
    text = re.sub(r'```(.*?)```', r'{{\1}}', text, flags=re.DOTALL)  # For multiline code blocks
    text = re.sub(r'`([^`]+)`', r'{{\1}}', text)  # For inline code

    # Note: Jira tables require a specific format. If your CSV fields contain HTML tables,
    # you'll need a more complex parser to convert them properly. This script does not handle tables.

    return text

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

def getTestType(automation, stepsType):
    testType = 'Manual'
    #validTypes = ['Functional', 'Smoke', 'regression', 'performance', 'security', 'usability','acceptance','compatability','integration','exploratory']
    if automation is not None and stepsType is not None:
        if stepsType == 'gherkin':
            testType = 'Cucumber'
        elif automation == 'automated':
            testType = 'Generic'
        else:
            testType = 'Manual'

    return testType

def appendRows(issueID='', issueKey='', stepsType=None, testType=None, testSummary=None, testPriority=None, action=None, data=None, result=None, issueType=None, description=None, precondition=None, unstrusturedDefinition=None, gherkinDefinition=None, testRepo=None):
    noActionTypes = ['Scenario', 'Automation']
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": getTestType(testType, stepsType),
                "Test Summary": testSummary if testSummary is not None else '',
                "Test Priority": getPriorityValue(testPriority) if testPriority is not None else '3',
                "Action": convert_to_jira_markdown(action) if action is not None and not pd.isna(action) and testType not in noActionTypes else '',
                "Data": convert_to_jira_markdown(data.strip()) if data is not None and not pd.isna(data) and testType not in noActionTypes else '',
                "Result": convert_to_jira_markdown(result) if result is not None and not pd.isna(result) and testType not in noActionTypes else '',
                "Precondition": precondition if precondition is not None and precondition != 0 else '',
                "Precondition Type": '',
                "Unstructured Definition": unstrusturedDefinition if unstrusturedDefinition is not None and testType == 'ApiTest' else '',
                "Issue Type": 'Test',
                "Description": convert_to_jira_markdown(description) if description is not None and not pd.isna(description)  else '',
                "Gherkin Definition": gherkinDefinition if gherkinDefinition is not None and stepsType == 'gherkin' else '',
                "Test Repo": testRepo})

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
        if type == 'gherkin':
            preconditionType = 'Cucumber'
        elif type == 'Api':
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
    
def parseqase2XrayData(inputfile, outputfile):
    df = pd.read_csv(inputfile)
    
    issueID = 1
    lastTestType = ''
    testID = ''
    suites = {"0":"EMPTY"}
    for index, xls_row in df.iterrows():
        preconditionID = 0
        testID = xls_row['v2.id']
        precondition = xls_row['preconditions']
        stepsType = xls_row['steps_type'] #classic or gherkin
        testType = xls_row['type'] #performance, functional, etc 
        suiteID = xls_row['suite_id']
        automation = xls_row['automation'] # automated or is-not-automated
        suite = xls_row['suite']

        if pd.isna(testID):
            #Process suites
            suiteParentID = xls_row['suite_parent_id']
            if not pd.isna(suiteParentID):
                suites.update({int(suiteID):  suites[int(suiteParentID)] + '/' + suite})
            else:                
                suites.update({int(suiteID):suite})
        else:
            if not pd.isna(precondition):
                #process preconditions
                appendPrecondition(issueID=issueID, precondition=precondition, type=stepsType)
                preconditionID=issueID
                issueID=issueID+1
            
            #Process tests
            tags = xls_row['tags']
            priority = xls_row['priority']
            automation = xls_row['automation'] #automated or is-not-automated
            steps_actions = xls_row['steps_actions']
            steps_result = xls_row['steps_result']
            steps_data = xls_row['steps_data']
            suite_id = xls_row['suite_id']
            suite_parent_id = xls_row['suite_parent_id']

            if not pd.isna(steps_actions):
                splitActions = steps_actions.splitlines()
                splitDatas = steps_data.splitlines()
                splitResults = steps_result.splitlines()
                index = 0

                if stepsType == 'gherkin':
                    appendRows(issueID=issueID, stepsType=stepsType, testType=automation, testSummary=xls_row['title'], testPriority=priority, action='', result='', data='', description=xls_row['description'], precondition=preconditionID, unstrusturedDefinition='', gherkinDefinition=re.sub(CLEANSTARTINGNUM, '',steps_actions), testRepo=suites[int(suite_id)])
                else:
                    for action in splitActions:
                        if index == 0:
                            appendRows(issueID=issueID, stepsType=stepsType, testType=automation, testSummary=xls_row['title'], testPriority=priority, action=re.sub(CLEANSTARTINGNUM, '', splitActions[index]), result=re.sub(CLEANSTARTINGNUM, '', splitResults[index]), data=re.sub(CLEANSTARTINGNUM, '', splitDatas[index]), description=xls_row['description'], precondition=preconditionID, unstrusturedDefinition='', gherkinDefinition='', testRepo=suites[int(suite_id)])
                            index = index+1
                        else:
                            appendRows(issueID=issueID, stepsType=stepsType, testType=automation, testSummary='', testPriority=priority,action=re.sub(CLEANSTARTINGNUM, '', splitActions[index]),result=re.sub(CLEANSTARTINGNUM, '', splitResults[index]), data=re.sub(CLEANSTARTINGNUM, '', splitDatas[index]), unstrusturedDefinition='', gherkinDefinition='')
                            index = index+1
                    
                issueID=issueID+1
            elif automation == 'automated' or automation == 'is-not-automated':
                    appendRows(issueID=issueID,stepsType=stepsType, testType=automation,testSummary=xls_row['title'],testPriority=priority, description=xls_row['description'], testRepo=suites[int(suite_id)] if not pd.isna(suite_id) else '') 
                    issueID=issueID+1
 
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
                print ('qase2Xray.py -i <CSV_inputfile> -o <CSV_outputfile>')
                sys.exit()
            elif opt in ("-i", "--ifile"):
                inputfile = arg
            elif opt in ("-o", "--ofile"):
                outputfile = arg

   except Exception as err:
       print ("An exception occurred:", err)

   #inputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_qase/cloud/qase_export.csv'
   #outputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_qase/cloud/CC_XRay_Export_result.csv'

   if not inputfile or not outputfile:
        print ('One of the input parameters is missing, please use: qase2Xray.py -i <CSV_inputfile> -o <CSV_outputfile>')
        sys.exit()

   parseqase2XrayData(inputfile, outputfile)

if __name__ == "__main__":
   main(sys.argv[1:])
