package com.xblend.xray;

import org.json.JSONArray;

/*
	This class provides some examples for exporting/generating .feature files based on existing
    gherkin/cucumber Tests defined in Xray.
    It also provides examples for importing test scenarios (i.e., Scenario/Scenario Outline and
    Background) from existing .feature files into Xray.
	
	These examples use an abstraction layer provided by the the auxiliary classes that implement
    the builder pattern along with a fluent API.
    You may not need all of what is supported by those classes but it can still be useful to take
    some ideas.
*/
public class FeaturesImportExportExamples {

    public static void main( String[] args )
    {
        // credentials used for Xray cloud requests
        String clientId = System.getenv().getOrDefault("CLIENT_ID", "215FFD69FE4644728C72182E05DACA49");
		String clientSecret = System.getenv().getOrDefault("CLIENT_SECRET", "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ecae9331e7");

        // credentials used for Xray server/datacenter requests (either Jira username+password or Personal Access Token)
        String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
        String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
        String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
		// String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

        /*
            Next follows some examples showcasing the export/generate of .feature files based on
            information on Xray, namely gherkin/cucumber Tests and related Preconditons, if any
        */

        // EXAMPLE1 (Xray cloud): export/generate .feature files locally from existing gherkin/cucumber Test issues, explicitly mentioned or indirectly obtained from related issues 
        String response = null;
        try {
            XrayFeaturesExporter xrayFeaturesExporter;
            xrayFeaturesExporter = new XrayFeaturesExporter.CloudBuilder(clientId, clientSecret)
                .withIssueKeys("CALC-3").build();
            response = xrayFeaturesExporter.submit("/tmp/features");
			System.out.println("response: " + response);
        } catch (Exception ex) {
            ex.printStackTrace();;
        }
        
        // EXAMPLE2 (Xray server/datacenter): export/generate .feature files locally from existing gherkin/cucumber Test issues, explicitly mentioned or indirectly obtained from related issues 
        response = null;
        try {
            XrayFeaturesExporter xrayFeaturesExporter;
            xrayFeaturesExporter = new XrayFeaturesExporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword)
                .withIssueKeys("CALC-89").build();
            response = xrayFeaturesExporter.submit("/tmp/features");
			System.out.println("response: " + response);
        } catch (Exception ex) {
            ex.printStackTrace();;
        }


        /*
            Next follows some examples showcasing the import of test scenarios from Scenario/Scenario Outline
            (and eventually Background) defined in local gherkin/cucumber .feature files into Xray,
             namely to gherkin/cucumber Tests and Preconditons, if any
        */

        // EXAMPLE3 (Xray server/datacenter): import test scenarios from a local gherkin/cucumber .feature to Xray
        try {
            XrayFeaturesImporter xrayFeaturesImporter;
            xrayFeaturesImporter = new XrayFeaturesImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword)
                .withProjectKey("CALC").build();
            JSONArray resp = xrayFeaturesImporter.importFrom("/tmp/features/1_CALC-89.feature");
			System.out.println("resp: " + resp.toString());
        } catch (Exception ex) {
            ex.printStackTrace();;
        }

        // EXAMPLE4 (Xray cloud): import test scenarios from a local gherkin/cucumber .feature to Xray
        try {
            XrayFeaturesImporter xrayFeaturesImporter;
            xrayFeaturesImporter = new XrayFeaturesImporter.CloudBuilder(clientId, clientSecret)
                .withProjectKey("CALC").build();
            JSONArray resp = xrayFeaturesImporter.importFrom("/tmp/features/1_CALC-89.feature");
			System.out.println("resp: " + resp.toString());
        } catch (Exception ex) {
            ex.printStackTrace();;
        }

    }
}
