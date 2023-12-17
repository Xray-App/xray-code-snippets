import sys, getopt
import re
import xml.etree.ElementTree as ET


column = ["Issue ID","Issue Key","Test Type","Test Summary", "Test Priority", "Action","Data","Result","Test Repo","Precondition","Issue Type", "Precondition Type", "Unstructured Definition", "Labels"]
row = []

CLEANR = re.compile('<.*?>|&([a-z0-9]+|#[0-9]{1,6}|#x[0-9a-f]{1,6});')
EMPTYSPACES = re.compile('\n|\r')
QUOTES = re.compile('\&quot\;')

def cleanTags(txt):
    if txt:
        cleanTxt = re.sub(QUOTES, '"', txt)
        cleanTxt = re.sub(CLEANR, '', cleanTxt)
        #cleanTxt = re.sub(EMPTYSPACES, ' ', cleanTxt)
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

def getTestType(type):
    testType = 'Manual'
    validTypes = ['Manual', 'Exploratory', 'Automated']
    if type is not None and type in validTypes:
        if type == 'Automated':
            testType = 'Generic'
        else:
            testType = type

    return testType

def appendRows(issueID='', issueKey='', testType=None, testSummary=None, testPriority=None, action=None, data=None, result=None, testRepo=None, issueType=None, precondition=None, unstructuredDefinition=None, labels=None):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": getTestType(testType),
                "Test Summary": cleanTags(testSummary.text) if testSummary is not None else '',
                "Test Priority": getPriorityValue(testPriority.text) if testPriority is not None else '3',
                "Action": cleanTags(action.text) if action is not None else '',
                "Data": cleanTags(data.text) if data is not None else '',
                "Result": cleanTags(result.text) if result is not None else '',
                "Test Repo": testRepo if testRepo else '',
                "Precondition": precondition if precondition is not None else '',
                "Issue Type": issueType,
                "Precondition Type": '',
                "Unstructured Definition": unstructuredDefinition if unstructuredDefinition is not None else '',
                "Labels": labels.text if labels is not None else ''})

def appendPrecondition(issueID='', precondition=None):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": '',
                "Test Summary": cleanTags(precondition.text) if precondition is not None else '',
                "Test Priority": '',
                "Action": '',
                "Data": '',
                "Result": '',
                "Test Repo": '',
                "Precondition": '',
                "Issue Type": 'precondition',
                "Precondition Type": 'Manual',
                "Unstructured Definition": '',
                "Labels": ''})

def handleTestSections(root, issueID, outputfile, repoName):
    if root.tag == 'suite':
        testsections = root.findall('sections/section')
    else:
        testsections = root.findall('section')

    baseRepoName= repoName
    for testsection in testsections:
        testRepoName = testsection.find('name').text
        testRepoDescription = testsection.find('description')

        if baseRepoName is not None and testRepoName is not None:
            testRepoName = baseRepoName+'/'+testRepoName

        cases = testsection.findall('cases/case')
        for testcase in cases:
            preconditionID = ''
            id = testcase.find('id')
            title = testcase.find('title')
            template = testcase.find('template')
            labels = testcase.find('type')
            priority = testcase.find('priority')
            estimate = testcase.find('estimate')

            custom = testcase.find('custom')
            if custom:
                automation_type = custom.find('automation_type')
                type = automation_type.find('value').text.strip() if automation_type is not None else 'Manual'
                preconditions = custom.find('preconds')
                if preconditions is not None:
                    appendPrecondition(issueID=issueID, precondition=preconditions)
                    preconditionID=issueID
                    issueID=issueID+1
                                   
                steps = custom.find('steps')
                steps_separated = custom.find('steps_separated')

                mission = custom.find('mission')
                goals = custom.find('goals')

                if steps is not None:
                    # Text field with steps
                    expected = custom.find('expected')
                    appendRows(issueID=issueID,testType=type,testSummary=title,testPriority=priority,action=steps,result=expected,precondition=preconditionID, testRepo=testRepoName, labels=labels)
                    issueID = issueID+1  
                elif steps_separated is not None:
                    # Steps in different cells
                    first_step = True
                    hasSteps = False
                    for step in steps_separated:
                        index = step.find('index')
                        content = step.find('content')
                        expected = step.find('expected')
                        additional_info = step.find('additional_info')
                        hasSteps = True
                        if first_step:
                            appendRows(issueID=issueID,testType=type,testSummary=title,testPriority=priority,action=content,result=expected,precondition=preconditionID, testRepo=testRepoName, data=additional_info, labels=labels)
                            first_step = False
                        else:
                            appendRows(issueID=issueID,testType=type, action=content,result=expected, data=additional_info)
                    if not hasSteps:
                        appendRows(issueID=issueID,testType=type,testSummary=title,testPriority=priority,precondition=preconditionID, testRepo=testRepoName, data=additional_info)
                    issueID = issueID+1  
                elif goals is not None or mission is not None:
                    # Exploratory Tests
                    uDefinition = ''
                    if mission is not None:
                        uDefinition = '*Mission:* '+mission.text+'\n'

                    if goals is not None:
                        uDefinition = uDefinition + ' *Goals:* '+goals.text+'\n'

                    appendRows(issueID=issueID,testType='Exploratory',testSummary=title,testPriority=priority,precondition=preconditionID, testRepo=testRepoName, unstructuredDefinition=uDefinition, labels=labels)
                    issueID = issueID+1  
                else:
                    #ignore all other types 
                    continue
            else:
                # Empty line
                appendRows(issueID=issueID,testType=labels.text,testSummary=title,testPriority=priority, testRepo=testRepoName, labels=labels)
                issueID = issueID+1  
        
        innerSection = testsection.find('sections')
        if innerSection is not None:
            issueID = handleTestSections(root=innerSection, issueID=issueID, outputfile=outputfile, repoName=testRepoName)

        df = pd.DataFrame(row, columns=column)  
        df.set_index("Issue ID", inplace=True)
        df.to_csv(outputfile)
    return issueID


def parseTestrail2XrayData(inputfile, outputfile):
    # Parsing XML file
    xmlParse = ET.parse(inputfile)
    root = xmlParse.getroot()
    issueID = 1

    handleTestSections(root=root, issueID=issueID, outputfile=outputfile, repoName=None)
    
def main(argv):
   inputfile = ''
   outputfile = ''

   try:
        opts, args = getopt.getopt(argv,"hi:o:",["ifile=","ofile="])
        for opt, arg in opts:
            if opt == '-h':
                print ('testrail2Xray.py -i <XML_inputfile> -o <CSV_outputfile>')
                sys.exit()
            elif opt in ("-i", "--ifile"):
                inputfile = arg
            elif opt in ("-o", "--ofile"):
                outputfile = arg
   except Exception as err:
       print ("An exception occurred:", err)

   inputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_testrail/comic_estore.xml'
   outputfile='/Users/cristianocunha/Documents/Projects/tutorials/xray-code-snippets/use_cases/import_from_testrail/comicEStore.csv'
   if not inputfile or not outputfile:
    print ('One of the input parameters is missing, please use: testrail2Xray.py -i <XML_inputfile> -o <CSV_outputfile>')
    sys.exit()

   parseTestrail2XrayData(inputfile, outputfile)

if __name__ == "__main__":
   main(sys.argv[1:])
