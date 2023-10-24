import json, configparser
import sys, getopt
import requests
from requests.auth import HTTPBasicAuth
import xml.etree.ElementTree as ET
import pandas as pd
import re
import markdownify
import html2text
from bs4 import BeautifulSoup

config = configparser.ConfigParser()
class tests:
    def __init__(self, summary, description, type, id):
        self.summary = summary
        self.description = description
        self.type = type
        self.id = id

column = ["Issue ID","Issue Key","Test Type","Test Summary", "Test Priority", "Action","Data","Result", "IssueType", "Description"]
row = []

def handleCells(cell):
    result = ''
    if cell.name == 'td':
        result += markdownify.markdownify((cell.get_text().strip()), heading_style="ATX")
    return result

def handleTables(tables):
    if tables and tables is not None:
        
        result = ''
        for table_content in tables:
            if table_content.name == 'table':
                # Convert the table and its contents
                table_markup = '\n|'
                for row in table_content.find_all('tr'):
                    row_markup = '|'.join(handleCells(cell) for cell in row.find_all(['th', 'td']))
                    table_markup += row_markup + '|\n|'
                result += table_markup  # Accumulate the table markup
            
        return result
    else:
        return tables

def cleanTags(txt):
    converter = html2text.HTML2Text()
    converter.bypass_tables = True
    if txt:
        cleanTxt = converter.handle(txt)
        
        if 'table' in cleanTxt:
            soup = BeautifulSoup(cleanTxt, 'html.parser')
            if soup:
                tables = soup.find_all('table')
                if tables and tables is not None:
                    resultTables = handleTables(tables)
                    cleanTxt = re.sub(r'<table>[^†]*</table>', resultTables, cleanTxt)
    else:
        cleanTxt = ''

    cleanTxt = re.sub(r'\\\*\\\*', r'*', cleanTxt)
    cleanTxt = re.sub(r'\*\*', r'*', cleanTxt)
    return cleanTxt

def appendRows(issueID='', issueKey='', testSummary=None, action=None, data=None, result=None, description=None):
    row.append({"Issue ID": issueID,
                "Issue Key": '',
                "Test Type": 'Manual',
                "Test Summary": cleanTags(testSummary) if testSummary is not None else '',
                "Test Priority": '3',
                "Action": cleanTags(action) if action is not None else '',
                "Data": cleanTags(data) if data is not None else '',
                "Result": cleanTags(result) if result is not None else '',
                "Issue Type": 'Test',
                "Description": cleanTags(description) if description is not None else '',})
    
def parsehpqc2XrayData(project, domain, outputfile):
    almUserName = config.get('hpqc','username') #"admin"
    almPassword = config.get('hpqc','password') #"wYE!M%H189&a"
    testList = []

    almURL = config.get('hpqc', 'base_endpoint')
    authEndPoint = almURL + "authentication-point/authenticate"
    qcSessionEndPoint = almURL + "rest/site-session"

    cookies = dict()

    headers = {
        'cache-control': "no-cache",
        'Accept': "application/xml",
        'Content-Type': "application/json"
    }

    response = requests.post(authEndPoint, auth=HTTPBasicAuth(almUserName, almPassword), headers=headers)
    if response.status_code == 200:
        cookieName = response.headers.get('Set-Cookie')
        LWSSO_COOKIE_KEY = cookieName[cookieName.index("LWSSO_COOKIE_KEY=") + 17: cookieName.index(";")]
        cookies['LWSSO_COOKIE_KEY'] = LWSSO_COOKIE_KEY
        print('logged in successfully')
    response = requests.post(qcSessionEndPoint, headers=headers, cookies=cookies)
    if response.status_code == 200 | response.status_code == 201:
        setCookies = response.headers.get('Set-Cookie').split(",")
        for setCookie in setCookies:
            cookieName = setCookie[0: setCookie.index("=")].strip()
            cookieValue = setCookie[setCookie.index("=") + 1: setCookie.index(";")]
            cookies[cookieName] = cookieValue
            if cookieName == 'XSRF-TOKEN':
                headers['X-XSRF-TOKEN'] = cookieValue

    #if not idList:
        issueID = 1
        #First call to get all tests in the domain + Project
        readTestsperDomainProject = almURL + f"rest/domains/{domain}/projects/{project}/tests"
        domainData = {'domain': {'name': domain}}
        response = requests.get(readTestsperDomainProject, headers=headers, cookies=cookies, data=json.dumps(domainData))

        xmlParse = ET.fromstring(response.content)
        testentities = xmlParse.findall('Entity')

        for entity in testentities:
            fields = entity.find('Fields')
            for field in fields.iter('Field'):
                if field.get('Name') == 'name':
                    summary = field.find('Value').text
                if field.get('Name') == 'description':
                    description = field.find('Value').text
                if field.get('Name') == 'subtype-id':
                    type = field.find('Value').text
                if field.get('Name') == 'id':
                    id = field.find('Value').text

            testList.append(tests(summary, description, type, id))
        
        for test in testList:

            #Get test steps
            readTestDetailsSteps = almURL + f"rest/domains/{domain}/projects/{project}/design-steps?query={{parent-id[{test.id}]}}"
            domainData = {'domain': {'name': domain}}
            response = requests.get(readTestDetailsSteps, headers=headers, cookies=cookies, data=json.dumps(domainData))
            stepsRoot = ET.fromstring(response.content)
            entities = stepsRoot.findall('Entity')
            for entity in entities:

                testSteps = entity.find('Fields')
                for testStep in testSteps.iter('Field'):
                    if testStep.get('Name') == 'description':
                        action = testStep.find('Value').text
                    if testStep.get('Name') == 'expected':
                        expected = testStep.find('Value').text
                    if testStep.get('Name') == 'step-order':
                        stepOrder = testStep.find('Value').text

                if test.type.lower() == 'manual':
                    if stepOrder == '1':
                        appendRows(issueID=issueID, testSummary=test.summary, action=action,result=expected, data='', description=test.description)
                    else:
                        appendRows(issueID=issueID, action=action,result=expected, data='')
            issueID = issueID + 1
        df = pd.DataFrame(row, columns=column)  
        df.set_index("Issue ID", inplace=True)
        df.to_csv(outputfile)

def main(argv):
   project = ''
   domain = ''
   outputfile = ''

   try:
        opts, args = getopt.getopt(argv,"hp:d:o:",["project=","domain=","ofile="])
        for opt, arg in opts:
            if opt == '-h':
                print ('hpqc2Xray.py -p <PROJECT> -d <DOMAIN> -o <CSV_outputfile>')
                sys.exit()
            elif opt in ("-p", "--project"):
                project = arg
            elif opt in ("-d", "--domain"):
                domain = arg
            elif opt in ("-o", "--ofile"):
                outputfile = arg
   except Exception as err:
       print ("An exception occurred:", err)

   if not outputfile or not project or not domain:
    print ('One of the input parameters is missing, please use: hpqc2Xray.py -p <PROJECT> -d <DOMAIN> -o <CSV_outputfile>')
    sys.exit()

   config.read('./hpqc_config.ini')
   parsehpqc2XrayData(project, domain, outputfile)

if __name__ == "__main__":
   main(sys.argv[1:])