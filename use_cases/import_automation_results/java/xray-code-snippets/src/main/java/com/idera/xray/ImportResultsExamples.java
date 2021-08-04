package com.idera.xray;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ImportResultsExamples 
{

	private static String submitXrayServerDCRobotFrameworkStandardEndpointExample(String reportFile) throws IOException{
		final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
		final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");

        String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
        String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
        String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
		String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

		String projectKey = "CALC";
		String fixVersion = "v1.0";
		String revision = null;
		String testPlanKey = "CALC-8895";
		String testEnvironment = "chrome";

		System.out.println("Importing a Robot Framework XML report to a Xray Server/Data Center instance...");

		OkHttpClient client = new OkHttpClient();
        String credentials;
        if (jiraPersonalAccessToken!= null) {
            credentials = "Bearer " + jiraPersonalAccessToken;
        } else {
            credentials = Credentials.basic(jiraUsername, jiraPassword);
        } 

        String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution/robot"; 
        MultipartBody requestBody = null;
        try {
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", reportFile, RequestBody.create(MEDIA_TYPE_XML, new File(reportFile)))
                    .build();
        } catch (Exception e1) {
            e1.printStackTrace();
			throw e1;
        }

        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();
        if (projectKey != null) {
            builder.addQueryParameter("projectKey", projectKey);
        }
        if (fixVersion != null) {
            builder.addQueryParameter("fixVersion", fixVersion);
        }
        if (revision != null) {
            builder.addQueryParameter("revision", revision);
        }
        if (testPlanKey != null) {
            builder.addQueryParameter("testPlanKey", testPlanKey);
        }
        if (testEnvironment != null) {
            builder.addQueryParameter("testEnvironment", testEnvironment);
        }        

        Request request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            // System.out.println("=============");
            // System.out.println(response);
            String responseBody = response.body().string();
            // System.out.println(responseBody);
            // System.out.println("=============");
            
            if (response.isSuccessful()){
                JSONObject responseObj = new JSONObject(responseBody);
                System.out.println("Test Execution: "+((JSONObject)(responseObj.get("testExecIssue"))).get("key"));
                return(responseBody);
            } else {
                throw new IOException("Unexpected HTTP code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw(e);
        }		
	}

	private static String submitXrayCloudRobotFrameworkStandardEndpointExample(String reportFile) throws IOException {
		final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
		final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");

		String clientId = System.getenv().getOrDefault("CLIENT_ID", "215FFD69FE4644728C72182E00000000");
		String clientSecret = System.getenv().getOrDefault("CLIENT_SECRET", "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000");
		String xrayCloudApiBaseUrl = "https://xray.cloud.getxray.app/api/v2";
		String authenticateUrl = xrayCloudApiBaseUrl + "/authenticate";
	
		String projectKey = "BOOK";
		String fixVersion = "1.0";
		String revision = null;
		String testPlanKey = null;
		String testEnvironment = null;

		System.out.println("Importing a Robot Framework XML report to a Xray Cloud instance...");

		OkHttpClient client = new OkHttpClient();
		String authenticationPayload = "{ \"client_id\": \"" + clientId +"\", \"client_secret\": \"" + clientSecret +"\" }";
		RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, authenticationPayload);
		Request request = new Request.Builder().url(authenticateUrl).post(body).build();
		Response response = null;
		String authToken = null;
		try {
			response = client.newCall(request).execute();
			// System.out.println("=============");
			// System.out.println(response);
			String responseBody = response.body().string();
			// System.out.println(responseBody);
			// System.out.println("=============");
			
			if (response.isSuccessful()){
				authToken = responseBody.replace("\"", "");	
			} else {
				throw new IOException("failed to authenticate " + response);
			}
		} catch (IOException e) {
			e.printStackTrace();
			// throw e;
		}
		String credentials = "Bearer " + authToken;

		String endpointUrl =  xrayCloudApiBaseUrl + "/import/execution/robot"; 
		RequestBody requestBody = null;
		try {
			String reportContent = new String ( Files.readAllBytes( Paths.get(reportFile) ) );
			requestBody = RequestBody.create(MEDIA_TYPE_XML, reportContent);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw e1;
		}

		HttpUrl url = HttpUrl.get(endpointUrl);
		HttpUrl.Builder builder = url.newBuilder();
        if (projectKey != null) {
            builder.addQueryParameter("projectKey", projectKey);
        }
        if (fixVersion != null) {
            builder.addQueryParameter("fixVersion", fixVersion);
        }
        if (revision != null) {
            builder.addQueryParameter("revision", revision);
        }
        if (testPlanKey != null) {
            builder.addQueryParameter("testPlanKey", testPlanKey);
        }
        if (testEnvironment != null) {
            builder.addQueryParameter("testEnvironment", testEnvironment);
        } 

		request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
		response = null;
		try {
			response = client.newCall(request).execute();
			// System.out.println("=============");
			// System.out.println(response);
			String responseBody = response.body().string();
			// System.out.println(responseBody);
			// System.out.println("=============");
						
			if (response.isSuccessful()){
				JSONObject responseObj = new JSONObject(responseBody);
				System.out.println("Test Execution: " + responseObj.get("key"));
				return(responseBody);
			} else {
				throw new IOException("Unexpected HTTP code " + response);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	private static String submitXrayServerDCRobotFrameworkMultipartEndpointExample(String reportFile) throws IOException {
		final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
		final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");

        String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
        String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
        String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
		String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

		System.out.println("Importing a Robot Framework XML report to a Xray Server/Data Center instance, customizing the Test Execution fields...");

		JSONObject testExecInfo = new JSONObject()
		.put("fields", new JSONObject()
			.put("summary", "Test execution for automated tests")
			.put("project", new JSONObject().put("key", "CALC"))
			.put("issuetype", new JSONObject().put("name", "Test Execution"))
			.put("fixVersions", new JSONObject[] { new JSONObject().put("name", "v1.0") })
		);
        // System.out.println(testExecInfo.toString());

        OkHttpClient client = new OkHttpClient();
        String credentials;
        if (jiraPersonalAccessToken!= null) {
            credentials = "Bearer " + jiraPersonalAccessToken;
        } else {
            credentials = Credentials.basic(jiraUsername, jiraPassword);
        } 

        String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution/robot/multipart"; 
        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();
        MultipartBody requestBody = null;
        try {
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    // for cucumber reports use "result" instead of "file"
                    .addFormDataPart("file", reportFile, RequestBody.create(MEDIA_TYPE_XML, new File(reportFile)))
                    .addFormDataPart("info", "info.json", RequestBody.create(MEDIA_TYPE_JSON, testExecInfo.toString()))
                    .build();
        } catch (Exception e1) {
            e1.printStackTrace();
            throw e1;
        }

        Request request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            // System.out.println("=============");
            // System.out.println(response);
            String responseBody = response.body().string();
            // System.out.println(responseBody);
            // System.out.println("=============");
            
            if (response.isSuccessful()){
                JSONObject responseObj = new JSONObject(responseBody);
                System.out.println("Test Execution: "+((JSONObject)(responseObj.get("testExecIssue"))).get("key"));
                return responseBody;
            } else {
                throw new IOException("Unexpected HTTP code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
	}


	private static String submitXrayCloudRobotFrameworkMultipartEndpointExample(String reportFile) throws IOException {
		String clientId = System.getenv().getOrDefault("CLIENT_ID", "215FFD69FE4644728C72182E00000000");
		String clientSecret = System.getenv().getOrDefault("CLIENT_SECRET", "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000");

		final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
		final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");
	
		String xrayCloudApiBaseUrl = "https://xray.cloud.getxray.app/api/v2";
		String authenticateUrl = xrayCloudApiBaseUrl + "/authenticate";
	
		System.out.println("Importing a Robot Framework XML report to a Xray Cloud instance, customizing the Test Execution fields...");

		JSONObject testExecInfo = new JSONObject()
			.put("fields", new JSONObject()
				.put("summary", "Test execution for automated tests")
				.put("project", new JSONObject().put("key", "CALC"))
				.put("issuetype", new JSONObject().put("name", "Test Execution"))
				.put("fixVersions", new JSONObject[] { new JSONObject().put("name", "v1.0") })
				)
			.put("xrayFields", new JSONObject()
				.put("testPlanKey", "CALC-1224")
				.put("environments", new String[] { "Chrome" })
				)
			;
		// System.out.println(testExecInfo.toString());

		OkHttpClient client = new OkHttpClient();
		String authenticationPayload = "{ \"client_id\": \"" + clientId +"\", \"client_secret\": \"" + clientSecret +"\" }";
		RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, authenticationPayload);
		Request request = new Request.Builder().url(authenticateUrl).post(body).build();
		Response response = null;
		String authToken = null;
		try {
			response = client.newCall(request).execute();
			// System.out.println("=============");
			// System.out.println(response);
			String responseBody = response.body().string();
			// System.out.println(responseBody);
			// System.out.println("=============");
			
			if (response.isSuccessful()){
				authToken = responseBody.replace("\"", "");	
			} else {
				throw new IOException("failed to authenticate " + response);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		String credentials = "Bearer " + authToken;

		String endpointUrl =  xrayCloudApiBaseUrl + "/import/execution/robot/multipart";	
		HttpUrl url = HttpUrl.get(endpointUrl);
		HttpUrl.Builder builder = url.newBuilder();
		MultipartBody requestBody = null;
		try {
			requestBody = new MultipartBody.Builder()
					.setType(MultipartBody.FORM)
					.addFormDataPart("results", reportFile, RequestBody.create(MEDIA_TYPE_XML, new File(reportFile)))
					.addFormDataPart("info", "info.json", RequestBody.create(MEDIA_TYPE_JSON, testExecInfo.toString()))
					.build();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
		
		response = null;
		try {
			response = client.newCall(request).execute();
			// System.out.println("=============");
			// System.out.println(response);
			String responseBody = response.body().string();
			// System.out.println(responseBody);
			// System.out.println("=============");
			
			if (response.isSuccessful()){
				JSONObject responseObj = new JSONObject(responseBody);
				System.out.println("Test Execution: "+responseObj.get("key"));
				return responseBody;
			} else {
				throw new IOException("Unexpected HTTP code " + response);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

    public static void main( String[] args )
    {
        // These are just some raw Java client examples of importing results to Xray...

		System.out.println(System.getProperty("user.dir"));
        String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
        String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
        String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
		String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

		String clientId = System.getenv().getOrDefault("CLIENT_ID", "215FFD69FE4644728C72182E00000000");
		String clientSecret = System.getenv().getOrDefault("CLIENT_SECRET", "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000");

		String robotReport = "./xray-code-snippets/src/main/resources/robot.xml";
		String cucumberReport = "./xray-code-snippets/src/main/resources/cucumber.json";
		String response = null;

		/*
			try {
				response = submitXrayServerDCRobotFrameworkStandardEndpointExample(robotReport);
				response = submitXrayCloudRobotFrameworkStandardEndpointExample(robotReport);
				response = submitXrayServerDCRobotFrameworkMultipartEndpointExample(robotReport);
				response = submitXrayCloudRobotFrameworkMultipartEndpointExample(robotReport);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			System.exit(0);
		*/



		XrayResultsImporter xrayImporter;
		JSONObject testExecInfo;
		JSONObject testInfo;
		try {
			// Importing a Robot Framework XML report to a Xray Server/Data Center instance...
			xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).withProjectKey("CALC").build();
			// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).withProjectKey("CALC").build();
			response = xrayImporter.submit(XrayResultsImporter.ROBOT_FORMAT, robotReport);
			System.out.println("response: " + response);


			// Importing a Robot Framework XML report to a Xray Cloud instance...
			xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).withProjectKey("BOOK").build();
			response = xrayImporter.submit(XrayResultsImporter.ROBOT_FORMAT, robotReport);
			System.out.println("response: " + response);


			// Importing a Robot Framework XML report to a Xray Server/Data Center instance, customizing the Test Execution fields...
			testInfo = new JSONObject();
			testExecInfo = new JSONObject()
			.put("fields", new JSONObject()
				.put("summary", "Test execution for automated tests")
				.put("project", new JSONObject().put("key", "CALC"))
				.put("issuetype", new JSONObject().put("name", "Test Execution"))
				.put("fixVersions", new JSONObject[] { new JSONObject().put("name", "v1.0") })
				.put("customfield_11807", new String[] {"CALC-8895"} ) // in this Jira instance, customfield_11805 corresponds to the Test Plan custom field; please check yours
				.put("customfield_11805", new String[] {"chrome"} )  // in this Jira instance, customfield_11805 corresponds to the Test Environments custom field; please check yours
				);
			xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).build();
			// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).build();
			response = xrayImporter.submitMultipartServerDC(XrayResultsImporter.ROBOT_FORMAT, robotReport, testExecInfo, testInfo);
			System.out.println("response: " + response);


			// Importing a Robot Framework XML report to a Xray Cloud instance, customizing the Test Execution fields...
			testExecInfo = new JSONObject()
			.put("fields", new JSONObject()
				.put("summary", "Test execution for automated tests")
				.put("project", new JSONObject().put("key", "CALC"))
				.put("issuetype", new JSONObject().put("name", "Test Execution"))
				.put("fixVersions", new JSONObject[] { new JSONObject().put("name", "v1.0") })
				)
			.put("xrayFields", new JSONObject()
				.put("testPlanKey", "CALC-1224")
				.put("environments", new String[] { "Chrome" })
				);
			xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).build();
			response = xrayImporter.submitMultipartCloud(XrayResultsImporter.ROBOT_FORMAT, robotReport, testExecInfo, testInfo);
			System.out.println("response: " + response);


			// Importing a Cucumber JSON report to a Xray Server/Data Center instance...
			xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).build();
			// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).build();
			response = xrayImporter.submit(XrayResultsImporter.CUCUMBER_FORMAT, cucumberReport);
			System.out.println("response: " + response);


			// Importing a Cucumber JSON report to a Xray Cloud instance...
			xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).build();
			response = xrayImporter.submit(XrayResultsImporter.CUCUMBER_FORMAT, cucumberReport);
			System.out.println("response: " + response);


			// Importing a Cucumber JSON report to a Xray Server/Data Center instance, customizing the Test Execution fields...
			testInfo = new JSONObject();
			testExecInfo = new JSONObject()
			.put("fields", new JSONObject()
				.put("summary", "Test execution for automated tests")
				.put("project", new JSONObject().put("key", "CALC"))
				.put("issuetype", new JSONObject().put("name", "Test Execution"))
				.put("fixVersions", new JSONObject[] { new JSONObject().put("name", "v1.0") })
				.put("customfield_11807", new String[] {"CALC-8895"} ) // in this Jira instance, customfield_11805 corresponds to the Test Plan custom field; please check yours
				.put("customfield_11805", new String[] {"chrome"} )  // in this Jira instance, customfield_11805 corresponds to the Test Environments custom field; please check yours
			);
			xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).build();
			// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).withProjectKey("CALC").build();
			response = xrayImporter.submitMultipartServerDC(XrayResultsImporter.CUCUMBER_FORMAT, cucumberReport, testExecInfo, testInfo);
			System.out.println("response: " + response);


			// Importing a Cucumber JSON report to a Xray Cloud instance, customizing the Test Execution fields...
			testExecInfo = new JSONObject()
			.put("fields", new JSONObject()
				.put("summary", "Test execution for automated tests")
				.put("project", new JSONObject().put("key", "CALC"))
				.put("issuetype", new JSONObject().put("name", "Test Execution"))
				.put("fixVersions", new JSONObject[] { new JSONObject().put("name", "v1.0") })
				)
			.put("xrayFields", new JSONObject()
				.put("testPlanKey", "CALC-1224")
				.put("environments", new String[] { "Chrome" })
				)
			;
			xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).build();
			response = xrayImporter.submitMultipartCloud(XrayResultsImporter.CUCUMBER_FORMAT, cucumberReport, testExecInfo, testInfo);
			System.out.println("response: " + response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.exit(0);
    }
}
