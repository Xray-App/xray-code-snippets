# read a CSV file having test name and result and convert it to JUnit XML file
# Usage: python csv_to_junit.py <input_csv_file> <output_junit_file>
import csv
import os
import sys



csv_file = sys.argv[1]
junit = sys.argv[2]
if not os.path.exists(csv_file):
    print(f"File {csv_file} does not exist")
    sys.exit(1)

def csv_to_junit(csv_file, junit_file):
    class TestCase:
        def __init__(self, name, result, output=""):
            self.name = name
            self.result = result
            self.output = output
            self.time = 0
        def to_xml(self):
            if self.result == "PASS":
                return f"""
                <testcase name="{self.name}" time="{self.time}">
                    <system-out><![CDATA[
                    {self.output}
                    ]]></system-out>
                </testcase>
                """
            else:
                return f"""
                <testcase name="{self.name}" time="{self.time}">
                    <failure message="{self.result}"><![CDATA[{self.output}]]></failure>
                    <system-err><![CDATA[
                    {self.output}
                    ]]></system-err>
                </testcase>
                """
    class TestSuite:
        def __init__(self, name):
            self.name = name
            self.testcases = []
        def add_testcase(self, testcase):
            self.testcases.append(testcase)
        def to_xml(self):
            testcases_xml = "\n".join([testcase.to_xml() for testcase in self.testcases])
            return f"""
            <testsuite name="{self.name}">
                {testcases_xml}
            </testsuite>
            """
    with open(csv_file, 'r') as f:
        reader = csv.reader(f)
        next(reader)
        test_suite = TestSuite("Test Suite")
        for row in reader:
            test_name, test_result, test_output = row
            test_case = TestCase(test_name, test_result, test_output)
            test_suite.add_testcase(test_case)
    with open(junit_file, 'w') as f:
        f.write(f"""<?xml version="1.0" encoding="UTF-8"?>
        <testsuites>
            {test_suite.to_xml()}
        </testsuites>
        """)
if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python csv_to_junit.py <input_csv_file> <output_junit_file>")
        sys.exit(1)
    csv_to_junit(csv_file, junit)
    print(f"JUnit XML file {junit} created successfully")

# Example CSV file format:
# test_name,result
# test1,success
# test2,failure
# Example usage:
# python csv_to_junit.py input.csv output.xml

