import sys, getopt
import re
import xml.etree.ElementTree as ET
import markdownify
import pandas as pd

column = ["Issue ID","Issue Key","Test Type","Test Summary", "Test Priority", "Action","Data","Result","Test Repo","Precondition","Issue Type", "Precondition Type", "Unstructured Definition", "Labels", "Component", "Gherkin definition", "Description", "Links"]
row = []

CLEANR = re.compile('<.*?>|&([a-z0-9]+|#[0-9]{1,6}|#x[0-9a-f]{1,6});')
EMPTYSPACES = re.compile('\n|\r')
QUOTES = re.compile('\&quot\;')

def cleanTags(txt):
    if txt:
        cleanTxt = markdownify.markdownify(txt, heading_style="ATX")
        cleanTxt = re.sub(r'\|\s+\|\s+\|\n\|\s+---\s+\|\s+---\s+\|','',cleanTxt)
        cleanTxt = re.sub(r'\|\s+---\s+\|','',cleanTxt)
        cleanTxt = re.sub('\*([a-zA-Z0-9]+)\*', r'_\1_', cleanTxt)
        cleanTxt = re.sub(r'\*_([a-zA-Z0-9]+)_\*', r'*\1*', cleanTxt)
        cleanTxt = re.sub(r'\*\*\*\*\*\*\*\*', r'', cleanTxt)
        #cleanTxt = txt
    else:
        cleanTxt = ''

    return cleanTxt

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

def getPreconditionType(type):
    preconditionType = 'Manual'
    if type is not None:
        if type == 'bdd':
            preconditionType = 'Cucumber'

    return preconditionType

def getTestType(type):
    testType = 'Manual'
    validTypes = ['plain', 'steps', 'bdd']
    if type is not None and type in validTypes:
        if type == 'Automated':
            testType = 'Generic'
        elif type == 'bdd':
            testType = 'Cucumber'
        else:
            testType = 'Manual'

    return testType

ENDPOINT = re.compile(r'(\!\[\]\()')
ENDPOINT2= re.compile(r'(-.*?)(\))')

def handleSteps(text):
    result = ''
    startTag = '![]'
    text = cleanTags(text)

    if startTag in text:    
        result=re.sub(ENDPOINT, r'[Link|',text)
        result=re.sub(ENDPOINT2, r'\1]',result)
    else:
        result = text
    return result

def processIssues(issues):
    issueList=''

    if issues is not None:
        for issue in issues:
            issueKey = issue.find('key')
            if issueList == '':
                issueList = (issueKey.text) if issueKey is not None else ''
            else:
                issueList = issueList + ';' + (issueKey.text) if issueKey is not None else ''

    return issueList

def getShortPrecondition(precondition):
    precond = ''
    
    if precondition is not None:
        precondition = cleanTags(precondition)
        if '\n' in precondition:
            precond = precondition.split('\n',255)[0]
        else:
            precond = precondition[:254]
    return precond

def appendRows(issueID='', issueKey='', testType=None, testSummary=None, testPriority=None, action=None, data=None, result=None, testRepo=None, issueType=None, precondition=None, unstructuredDefinition=None, labels=None, components=None, gherkindefinition=None, description=None, links=None):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": getTestType(testType),
                "Test Summary": cleanTags(testSummary.text) if testSummary is not None else '',
                "Test Priority": getPriorityValue(testPriority.text) if testPriority is not None else '3',
                "Action": handleSteps(action.text) if action is not None else '',
                "Data": handleSteps(data.text) if data is not None else '',
                "Result": handleSteps(result.text) if result is not None else '',
                "Test Repo": testRepo if testRepo else '',
                "Precondition": precondition if precondition is not None else '',
                "Issue Type": issueType,
                "Precondition Type": '',
                "Unstructured Definition": unstructuredDefinition if unstructuredDefinition is not None else '',
                "Labels": labels.text if labels is not None else '',
                "Component": cleanTags(components.text) if components is not None else '',
                "Gherkin definition": gherkindefinition.text if gherkindefinition is not None else '',
                "Description": cleanTags(description.text) if description is not None else '',
                "Links": links if links is not None else ''})

def appendPrecondition(issueID='', precondition=None, type=None):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": '',
                "Test Summary": getShortPrecondition(precondition.text) if precondition is not None else '',
                "Test Priority": '',
                "Action": '',
                "Data": '',
                "Result": '',
                "Test Repo": '',
                "Precondition": '',
                "Issue Type": 'precondition',
                "Precondition Type": getPreconditionType(type) if type is not None else 'Manual',
                "Unstructured Definition": '',
                "Labels": '',
                "Component": '',
                "Description": cleanTags(precondition.text) if precondition is not None else '',
                "Links":''})

#def handleSteps():


def handleTestCases(root, issueID, outputfile, repoName):
    testcases = root.findall('testCases/testCase')
    baseRepoName= repoName

    for testcase in testcases:
        folder = testcase.find('folder')
        testRepoName = folder.text if folder is not None else '' 

        if baseRepoName is not None and testRepoName is not None:
            testRepoName = baseRepoName+'/'+testRepoName

        preconditionID = ''
        title = testcase.find('name')
        component = testcase.find('component')
        labels = testcase.find('labels')
        priority = testcase.find('priority')
        preconditions = testcase.find('precondition')
        type = testcase.find('testScript').attrib['type']
        objective = testcase.find('objective')
        links = processIssues(testcase.findall('issues/issue'))

        if preconditions is not None:
            appendPrecondition(issueID=issueID, precondition=preconditions, type=type)
            preconditionID=issueID
            issueID=issueID+1
        
        if type == 'plain':
            details = testcase.find('testScript/details')
            details.text = '<strong>Objective<strong>: '+objective.text+'\n'+ '<strong>Details<strong>: ' +details.text
            appendRows(issueID=issueID,testType=type,testSummary=title,testPriority=priority,precondition=preconditionID, testRepo=testRepoName, labels=labels, description=details,links=links)
            issueID = issueID+1  
        elif type == 'bdd':
            details = testcase.find('testScript').find('details')
            appendRows(issueID=issueID,testType=type,testSummary=title,testPriority=priority,precondition=preconditionID, testRepo=testRepoName, labels=labels, components=component, gherkindefinition=details, description=objective,links=links)
            issueID = issueID+1 
        elif type == 'steps':
            steps = testcase.find('testScript').findall('steps/step')
            hasSteps = False
            first_step = True
            for step in steps:
                index = step.attrib['index']
                content = step.find('description')
                expected = step.find('expectedResult')
                additional_info = step.find('testData')
                hasSteps = True
                
                if first_step:
                    appendRows(issueID=issueID,testType=type,testSummary=title,testPriority=priority,action=content,result=expected,precondition=preconditionID, testRepo=testRepoName, data=additional_info, labels=labels, description=objective,links=links)
                    first_step = False
                else:
                    appendRows(issueID=issueID,testType=type, action=content,result=expected, data=additional_info, description=objective)
                
            if not hasSteps:
                appendRows(issueID=issueID,testType=type,testSummary=title,testPriority=priority,precondition=preconditionID, testRepo=testRepoName, data=additional_info, description=objective,links=links)
                
            issueID = issueID+1  

        df = pd.DataFrame(row, columns=column)  
        df.set_index("Issue ID", inplace=True)
        df.to_csv(outputfile)
    return issueID


def parseZephyrScale2XrayData(inputfile, outputfile):
    # Parsing XML file
    xmlParse = ET.parse(inputfile)
    root = xmlParse.getroot()
    issueID = 1

    handleTestCases(root=root, issueID=issueID, outputfile=outputfile, repoName=None)
    
def main(argv):
   inputfile = ''
   outputfile = ''

   try:
        opts, args = getopt.getopt(argv,"hi:o:",["ifile=","ofile="])
        for opt, arg in opts:
            if opt == '-h':
                print ('zephyrscale2Xray.py -i <XML_inputfile> -o <CSV_outputfile>')
                sys.exit()
            elif opt in ("-i", "--ifile"):
                inputfile = arg
            elif opt in ("-o", "--ofile"):
                outputfile = arg
   except Exception as err:
       print ("An exception occurred:", err)

   #inputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_zephyr_scale/cloud/atm-exporter.xml'
   #outputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_zephyr_scale/cloud/atm-exporter.csv'
   if not inputfile or not outputfile:
        print ('One of the input parameters is missing, please use: zephyrscale2Xray.py -i <XML_inputfile> -o <CSV_outputfile>')
        sys.exit()

   parseZephyrScale2XrayData(inputfile, outputfile)

if __name__ == "__main__":
   main(sys.argv[1:])
