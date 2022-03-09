package com.xblend.xray;

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

/*
	This class provides some examples for uploading test results to Xray (either server/datacenter or cloud).
	
	You can see some low-level implementation examples in different methods, which show how to perform the
	the HTTP POST request to the respective Xray REST API endpoint. There are some differences depending
	on the report format, endpoint used, and whether it's Xray server/DC or Xray cloud.
	
	The main() method shows a different approach, using an abstraction layer, that supports and hides some
	of the details and the differences mentioned earlier; this alternative uses the auxiliary classes and the
	builder pattern along with a fluent API. You may not need all of what is supported by those classes but
	it can still be useful to take some ideas.
*/
public class ImportResultsExamples 
{

	/*
		A possible raw implementation for uploading test results using the Xray JSON format
		to Xray server/DC, using the standard REST API endpoint, which provides the ability of specifying
		some well-known attributes/fields for the Test Execution that will be created.
	*/
	private static String submitXrayServerDCXrayJsonStandardEndpointExample(String reportFile) throws IOException{
		final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

        String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
        String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
        String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
		String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

		System.out.println("Importing a Xray JSON report to a Xray Server/Data Center instance...");

		OkHttpClient client = new OkHttpClient();
        String credentials;
        if (jiraPersonalAccessToken!= null) {
            credentials = "Bearer " + jiraPersonalAccessToken;
        } else {
            credentials = Credentials.basic(jiraUsername, jiraPassword);
        } 

        String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution"; 
		RequestBody requestBody = null;
		try {
			String reportContent = new String ( Files.readAllBytes( Paths.get(reportFile) ) );
			requestBody = RequestBody.create(reportContent, MEDIA_TYPE_JSON);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw e1;
		}
        Request request = new Request.Builder().url(endpointUrl).post(requestBody).addHeader("Authorization", credentials).build();
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

	/*
		A possible raw implementation for uploading test results in Xray JSON format
		to Xray cloud, using the standard REST API endpoint, which provides the ability of specifying
		some well-known attributes/fields for the Test Execution that will be created.
	*/
	private static String submitXrayCloudXrayJsonStandardEndpointExample(String reportFile) throws IOException {
		final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

		String clientId = System.getenv().getOrDefault("CLIENT_ID", "215FFD69FE4644728C72182E00000000");
		String clientSecret = System.getenv().getOrDefault("CLIENT_SECRET", "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000");
		String xrayCloudApiBaseUrl = "https://xray.cloud.getxray.app/api/v2";
		String authenticateUrl = xrayCloudApiBaseUrl + "/authenticate";

		System.out.println("Importing a Xray JSON report to a Xray Cloud instance...");

		OkHttpClient client = new OkHttpClient();
		String authenticationPayload = "{ \"client_id\": \"" + clientId +"\", \"client_secret\": \"" + clientSecret +"\" }";
		RequestBody body = RequestBody.create(authenticationPayload, MEDIA_TYPE_JSON);
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

		String endpointUrl =  xrayCloudApiBaseUrl + "/import/execution"; 
		RequestBody requestBody = null;
		try {
			String reportContent = new String ( Files.readAllBytes( Paths.get(reportFile) ) );
			requestBody = RequestBody.create(reportContent, MEDIA_TYPE_JSON);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw e1;
		}

		request = new Request.Builder().url(endpointUrl).post(requestBody).addHeader("Authorization", credentials).build();
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

	/*
		A possible raw implementation for uploading test results in Xray JSON format
		to Xray server/DC, using the "multipart" REST API endpoint for that format.
		Multipart endpoints support the ability of providing the test report along with raw fields, as JSON,
		for the Test Execution and/or for the Test issues that may be created.
	*/
	private static String submitXrayServerDCXrayJsonMultipartEndpointExample(String reportFile) throws IOException {
		final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

        String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
        String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
        String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
		String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

		System.out.println("Importing a Xray JSON report to a Xray Server/Data Center instance, customizing the Test Execution fields...");

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

        String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution/multipart"; 
        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();
        MultipartBody requestBody = null;
        try {
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("result", reportFile, RequestBody.create(new File(reportFile), MEDIA_TYPE_JSON))
                    .addFormDataPart("info", "info.json", RequestBody.create(testExecInfo.toString(), MEDIA_TYPE_JSON))
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

	/*
		A possible raw implementation for uploading test results in Xray JSON format
		to Xray cloud, using the "multipart" REST API endpoint for that format.
		Multipart endpoints support the ability of providing the test report along with raw fields, as JSON,
		for the Test Execution and/or for the Test issues that may be created.
	*/
	private static String submitXrayCloudXrayJsonMultipartEndpointExample(String reportFile) throws IOException {
		String clientId = System.getenv().getOrDefault("CLIENT_ID", "215FFD69FE4644728C72182E00000000");
		String clientSecret = System.getenv().getOrDefault("CLIENT_SECRET", "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000");

		final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
	
		String xrayCloudApiBaseUrl = "https://xray.cloud.getxray.app/api/v2";
		String authenticateUrl = xrayCloudApiBaseUrl + "/authenticate";
	
		System.out.println("Importing a JUnit XML report to a Xray Cloud instance, customizing the Test Execution fields...");

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
		RequestBody body = RequestBody.create(authenticationPayload, MEDIA_TYPE_JSON);
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

		String endpointUrl =  xrayCloudApiBaseUrl + "/import/execution/multipart";	
		HttpUrl url = HttpUrl.get(endpointUrl);
		HttpUrl.Builder builder = url.newBuilder();
		MultipartBody requestBody = null;
		try {
			requestBody = new MultipartBody.Builder()
					.setType(MultipartBody.FORM)
					.addFormDataPart("results", reportFile, RequestBody.create(new File(reportFile), MEDIA_TYPE_JSON))
					.addFormDataPart("info", "info.json", RequestBody.create(testExecInfo.toString(), MEDIA_TYPE_JSON))
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



	/*
		A possible raw implementation for uploading test results from JUnit
		to Xray server/DC, using the standard REST API endpoint, which provides the ability of specifying
		some well-known attributes/fields for the Test Execution that will be created.
	*/
	private static String submitXrayServerDCJunitStandardEndpointExample(String reportFile) throws IOException{
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

		System.out.println("Importing a JUnit XML report to a Xray Server/Data Center instance...");

		OkHttpClient client = new OkHttpClient();
        String credentials;
        if (jiraPersonalAccessToken!= null) {
            credentials = "Bearer " + jiraPersonalAccessToken;
        } else {
            credentials = Credentials.basic(jiraUsername, jiraPassword);
        } 

        String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution/junit"; 
        MultipartBody requestBody = null;
        try {
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", reportFile, RequestBody.create(new File(reportFile), MEDIA_TYPE_XML))
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

	/*
		A possible raw implementation for uploading test results from JUnit
		to Xray cloud, using the standard REST API endpoint, which provides the ability of specifying
		some well-known attributes/fields for the Test Execution that will be created.
	*/
	private static String submitXrayCloudJunitStandardEndpointExample(String reportFile) throws IOException {
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

		System.out.println("Importing a JUnit XML report to a Xray Cloud instance...");

		OkHttpClient client = new OkHttpClient();
		String authenticationPayload = "{ \"client_id\": \"" + clientId +"\", \"client_secret\": \"" + clientSecret +"\" }";
		RequestBody body = RequestBody.create(authenticationPayload, MEDIA_TYPE_JSON);
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

		String endpointUrl =  xrayCloudApiBaseUrl + "/import/execution/junit"; 
		RequestBody requestBody = null;
		try {
			String reportContent = new String ( Files.readAllBytes( Paths.get(reportFile) ) );
			requestBody = RequestBody.create(reportContent, MEDIA_TYPE_XML);
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

	/*
		A possible raw implementation for uploading test results from JUnit
		to Xray server/DC, using the "multipart" REST API endpoint for that format.
		Multipart endpoints support the ability of providing the test report along with raw fields, as JSON,
		for the Test Execution and/or for the Test issues that may be created.
	*/
	private static String submitXrayServerDCJunitMultipartEndpointExample(String reportFile) throws IOException {
		final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
		final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");

        String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
        String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
        String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
		String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

		System.out.println("Importing a JUnit XML report to a Xray Server/Data Center instance, customizing the Test Execution fields...");

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

        String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution/junit/multipart"; 
        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();
        MultipartBody requestBody = null;
        try {
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    // for cucumber reports use "result" instead of "file"
                    .addFormDataPart("file", reportFile, RequestBody.create(new File(reportFile), MEDIA_TYPE_XML))
                    .addFormDataPart("info", "info.json", RequestBody.create(testExecInfo.toString(), MEDIA_TYPE_JSON))
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

	/*
		A possible raw implementation for uploading test results from JUnit
		to Xray cloud, using the "multipart" REST API endpoint for that format.
		Multipart endpoints support the ability of providing the test report along with raw fields, as JSON,
		for the Test Execution and/or for the Test issues that may be created.
	*/
	private static String submitXrayCloudJunitMultipartEndpointExample(String reportFile) throws IOException {
		String clientId = System.getenv().getOrDefault("CLIENT_ID", "215FFD69FE4644728C72182E00000000");
		String clientSecret = System.getenv().getOrDefault("CLIENT_SECRET", "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000");

		final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
		final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");
	
		String xrayCloudApiBaseUrl = "https://xray.cloud.getxray.app/api/v2";
		String authenticateUrl = xrayCloudApiBaseUrl + "/authenticate";
	
		System.out.println("Importing a JUnit XML report to a Xray Cloud instance, customizing the Test Execution fields...");

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
		RequestBody body = RequestBody.create(authenticationPayload, MEDIA_TYPE_JSON);
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

		String endpointUrl =  xrayCloudApiBaseUrl + "/import/execution/junit/multipart";	
		HttpUrl url = HttpUrl.get(endpointUrl);
		HttpUrl.Builder builder = url.newBuilder();
		MultipartBody requestBody = null;
		try {
			requestBody = new MultipartBody.Builder()
					.setType(MultipartBody.FORM)
					.addFormDataPart("results", reportFile, RequestBody.create(new File(reportFile), MEDIA_TYPE_XML))
					.addFormDataPart("info", "info.json", RequestBody.create(testExecInfo.toString(), MEDIA_TYPE_JSON))
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



	/*
		A possible raw implementation for uploading test results from Robot Framework (e.g., output.xml)
		to Xray server/DC, using the standard REST API endpoint, which provides the ability of specifying
		some well-known attributes/fields for the Test Execution that will be created.
	*/
	private static String submitXrayServerDCRobotFrameworkStandardEndpointExample(String reportFile) throws IOException{
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
                    .addFormDataPart("file", reportFile, RequestBody.create(new File(reportFile), MEDIA_TYPE_XML))
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

	/*
		A possible raw implementation for uploading test results from Robot Framework (e.g., output.xml)
		to Xray cloud, using the standard REST API endpoint, which provides the ability of specifying
		some well-known attributes/fields for the Test Execution that will be created.
	*/
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
		RequestBody body = RequestBody.create(authenticationPayload, MEDIA_TYPE_JSON);
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
			requestBody = RequestBody.create(reportContent, MEDIA_TYPE_XML);
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

	/*
		A possible raw implementation for uploading test results from Robot Framework (e.g., output.xml)
		to Xray server/DC, using the "multipart" REST API endpoint for that format.
		Multipart endpoints support the ability of providing the test report along with raw fields, as JSON,
		for the Test Execution and/or for the Test issues that may be created.
	*/
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
                    .addFormDataPart("file", reportFile, RequestBody.create(new File(reportFile), MEDIA_TYPE_XML))
                    .addFormDataPart("info", "info.json", RequestBody.create(testExecInfo.toString(), MEDIA_TYPE_JSON))
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

	/*
		A possible raw implementation for uploading test results from Robot Framework (e.g., output.xml)
		to Xray cloud, using the "multipart" REST API endpoint for that format.
		Multipart endpoints support the ability of providing the test report along with raw fields, as JSON,
		for the Test Execution and/or for the Test issues that may be created.
	*/
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
		RequestBody body = RequestBody.create(authenticationPayload, MEDIA_TYPE_JSON);
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
					.addFormDataPart("results", reportFile, RequestBody.create(new File(reportFile), MEDIA_TYPE_XML))
					.addFormDataPart("info", "info.json", RequestBody.create(testExecInfo.toString(), MEDIA_TYPE_JSON))
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

	/*
	These are just some importing results to Xray, using either the raw code shown in some methods herein
	implemented, or using the auxiliary classes that implement the builder pattern and a fluent API.
	*/
    public static void main( String[] args )
    {
		System.out.println(System.getProperty("user.dir"));

        // credentials used for Xray server/datacenter requests (either Jira username+password or Personal Access Token)
		String jiraBaseUrl = System.getenv().getOrDefault("JIRA_BASE_URL", "http://192.168.56.102");
        String jiraUsername = System.getenv().getOrDefault("JIRA_USERNAME", "admin");
        String jiraPassword = System.getenv().getOrDefault("JIRA_PASSWORD", "admin");
		String jiraPersonalAccessToken = System.getenv().getOrDefault("JIRA_TOKEN", "OTE0ODc2NDE2NTgxOnrhigwOreFoyNIA9lXTZaOcgbNY");

        // credentials used for Xray cloud requests
		String clientId = System.getenv().getOrDefault("CLIENT_ID", "215FFD69FE4644728C72182E00000000");
		String clientSecret = System.getenv().getOrDefault("CLIENT_SECRET", "1c00f8f22f56a8684d7c18cd6147ce2787d95e4da9f3bfb0af8f02ec00000000");

		// auxiliary variables
		String xrayJsonDCReport = "./src/main/resources/xray_dc.json";
		String xrayJsonCloudReport = "./src/main/resources/xray_cloud.json";
		String junitReport = "./src/main/resources/junit.xml";
		String robotReport = "./src/main/resources/robot.xml";
		String cucumberReport = "./src/main/resources/cucumber.json";
		String response = null;

		/*
			// raw implementations, as methods that make the necessary REST API requests
			try {
				response = submitXrayServerDCXrayJsonStandardEndpointExample(xrayJsonDCReport);
				response = submitXrayCloudXrayJsonStandardEndpointExample(xrayJsonCloudReport);
				response = submitXrayServerDCXrayJsonMultipartEndpointExample(xrayJsonDCReport);
				response = submitXrayCloudXrayJsonMultipartEndpointExample(xrayJsonCloudReport);
			
				response = submitXrayServerDCJunitStandardEndpointExample(junitReport);
				response = submitXrayCloudJunitStandardEndpointExample(junitReport);
				response = submitXrayServerDCJunitMultipartEndpointExample(junitReport);
				response = submitXrayCloudJunitMultipartEndpointExample(junitReport);

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
			/*
				Next follows some examples showcasing the importing of Xray JSON reports
			*/

			// EXAMPLE (Xray server/datacenter ): Importing a Xray JSON report
			xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).build();
			// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).build();
			response = xrayImporter.submit(XrayResultsImporter.XRAY_FORMAT, xrayJsonDCReport);
			System.out.println("response: " + response);

			// EXAMPLE (Xray cloud): Importing a Xray JSON report
			xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).withProjectKey("BOOK").build();
			response = xrayImporter.submit(XrayResultsImporter.XRAY_FORMAT, xrayJsonCloudReport);
			System.out.println("response: " + response);

			// EXAMPLE (Xray server/datacenter): Importing a Xray JSON report, customizing the Test Execution fields...
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
			response = xrayImporter.submitMultipartServerDC(XrayResultsImporter.XRAY_FORMAT, xrayJsonDCReport, testExecInfo, testInfo);
			System.out.println("response: " + response);

			// EXAMPLE (Xray cloud): Importing a Xray JSON report, customizing the Test Execution fields...
			testExecInfo = new JSONObject()
			.put("fields", new JSONObject()
				.put("summary", "Test execution for automated tests")
				.put("project", new JSONObject().put("key", "CALC"))
				.put("issuetype", new JSONObject().put("name", "Test Execution"))
				.put("fixVersions", new JSONObject[] { new JSONObject().put("name", "v1.0") })
				)
			.put("xrayFields", new JSONObject()
				.put("testPlanKey", "CALC-1369")
				.put("environments", new String[] { "dev" })
				);
			xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).build();
			response = xrayImporter.submitMultipartCloud(XrayResultsImporter.XRAY_FORMAT, xrayJsonCloudReport, testExecInfo, testInfo);
			System.out.println("response: " + response);



			/*
				Next follows some examples showcasing the importing of JUnit XML reports
			*/

			// EXAMPLE (Xray server/datacenter ): Importing a JUnit XML report
			xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).withProjectKey("CALC").build();
			// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).withProjectKey("CALC").build();
			response = xrayImporter.submit(XrayResultsImporter.JUNIT_FORMAT, junitReport);
			System.out.println("response: " + response);

			// EXAMPLE (Xray cloud): Importing a JUnit XML report
			xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).withProjectKey("BOOK").build();
			response = xrayImporter.submit(XrayResultsImporter.JUNIT_FORMAT, junitReport);
			System.out.println("response: " + response);

			// EXAMPLE (Xray server/datacenter): Importing a JUnit XML report, customizing the Test Execution fields...
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
			response = xrayImporter.submitMultipartServerDC(XrayResultsImporter.JUNIT_FORMAT, junitReport, testExecInfo, testInfo);
			System.out.println("response: " + response);

			// EXAMPLE (Xray cloud): Importing a JUnit XML report, customizing the Test Execution fields...
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
			response = xrayImporter.submitMultipartCloud(XrayResultsImporter.JUNIT_FORMAT, junitReport, testExecInfo, testInfo);
			System.out.println("response: " + response);

			/*
				Next follows some examples showcasing the importing of Robot Framework XML reports
			*/

			// EXAMPLE (Xray server/datacenter): Importing a Robot Framework XML report
			xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).withProjectKey("CALC").build();
			// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).withProjectKey("CALC").build();
			response = xrayImporter.submit(XrayResultsImporter.ROBOT_FORMAT, robotReport);
			System.out.println("response: " + response);

			// EXAMPLE (Xray cloud): Importing a Robot Framework XML report
			xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).withProjectKey("BOOK").build();
			response = xrayImporter.submit(XrayResultsImporter.ROBOT_FORMAT, robotReport);
			System.out.println("response: " + response);

			// EXAMPLE (Xray server/datacenter): Importing a Robot Framework XML report, customizing the Test Execution fields...
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

			// EXAMPLE (Xray cloud): Importing a Robot Framework XML report, customizing the Test Execution fields...
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


			/*
				Next follows some examples showcasing the importing of Cucumber JSON reports
			*/

			// EXAMPLE (Xray server/datacenter): Importing a Cucumber JSON report
			xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraUsername, jiraPassword).build();
			// xrayImporter = new XrayResultsImporter.ServerDCBuilder(jiraBaseUrl, jiraPersonalAccessToken).build();
			response = xrayImporter.submit(XrayResultsImporter.CUCUMBER_FORMAT, cucumberReport);
			System.out.println("response: " + response);

			// EXAMPLE (Xray cloud): Importing a Cucumber JSON report
			xrayImporter = new XrayResultsImporter.CloudBuilder(clientId, clientSecret).build();
			response = xrayImporter.submit(XrayResultsImporter.CUCUMBER_FORMAT, cucumberReport);
			System.out.println("response: " + response);

			// EXAMPLE (Xray server/datacenter): Importing a Cucumber JSON report, customizing the Test Execution fields...
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

			// EXAMPLE (Xray cloud): Importing a Cucumber JSON report, customizing the Test Execution fields...
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
