package com.xblend.xray;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;
import java.util.Arrays;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class XrayResultsImporter {
    private final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");
    private final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml");

    private final String xrayCloudApiBaseUrl = "https://xray.cloud.getxray.app/api/v2";
	private final String xrayCloudAuthenticateUrl = xrayCloudApiBaseUrl + "/authenticate";

    public static final String XRAY_FORMAT = "xray";
    public static final String JUNIT_FORMAT = "junit";
    public static final String TESTNG_FORMAT = "testng";
    public static final String ROBOT_FORMAT = "robot";
    public static final String XUNIT_FORMAT = "xunit";
    public static final String NUNIT_FORMAT = "nunit";
    public static final String CUCUMBER_FORMAT = "cucumber";
    public static final String BEHAVE_FORMAT = "behave";

    private String jiraBaseUrl;
    private String jiraUsername;
    private String jiraPassword;
    private String jiraPersonalAccessToken;

    private String clientId;
    private String clientSecret;

    private String projectKey;
    private String fixVersion;
    private String revision;
    private String testPlanKey;
    private String testExecKey;
    private String testEnvironment;

    private Boolean ignoreSslErrors = false;
    private Boolean useInternalTestProxy = false;

    private XrayResultsImporter(ServerDCBuilder builder){
        this.jiraBaseUrl = builder.jiraBaseUrl;
        this.jiraUsername = builder.jiraUsername;
        this.jiraPassword = builder.jiraPassword;
        this.jiraPersonalAccessToken = builder.jiraPersonalAccessToken;
        this.projectKey = builder.projectKey;

        this.ignoreSslErrors = builder.ignoreSslErrors;
        this.useInternalTestProxy = builder.useInternalTestProxy;
    }

    private XrayResultsImporter(CloudBuilder builder){
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        this.projectKey = builder.projectKey;
        this.fixVersion = builder.fixVersion;
        this.revision = builder.revision;
        this.testPlanKey = builder.testPlanKey;
        this.testExecKey = builder.testExecKey;
        this.testEnvironment = builder.testEnvironment;

        this.ignoreSslErrors = builder.ignoreSslErrors;
        this.useInternalTestProxy = builder.useInternalTestProxy;
    }

    public static class ServerDCBuilder {

        private final String jiraBaseUrl;
        private String jiraUsername;
        private String jiraPassword;
        private String jiraPersonalAccessToken;

        private String projectKey;
        private String fixVersion;
        private String revision;
        private String testPlanKey;
        private String testExecKey;
        private String testEnvironment;

        private Boolean ignoreSslErrors = false;
        private Boolean useInternalTestProxy = false;

        public ServerDCBuilder(String jiraBaseUrl, String jiraUsername, String jiraPassword) {
            this.jiraBaseUrl = jiraBaseUrl;
            this.jiraUsername = jiraUsername;
            this.jiraPassword = jiraPassword;
        }

        public ServerDCBuilder(String jiraBaseUrl, String jiraPersonalAccessToken) {
            this.jiraBaseUrl = jiraBaseUrl;
            this.jiraPersonalAccessToken = jiraPersonalAccessToken;
        }

        public ServerDCBuilder withIgnoreSslErrors(Boolean ignoreSslErrors) {
            this.ignoreSslErrors = ignoreSslErrors;
            return this;
        }

        public ServerDCBuilder withInternalTestProxy(Boolean useInternalTestProxy) {
            this.useInternalTestProxy = useInternalTestProxy;
            return this;
        }

        public ServerDCBuilder withProjectKey(String projectKey) {
            this.projectKey = projectKey;
            return this;
        }
    
        public ServerDCBuilder withVersion(String fixVersion) {
            this.fixVersion = fixVersion;
            return this;
        }

        public ServerDCBuilder withRevision(String revision) {
            this.revision = revision;
            return this;
        }

        public ServerDCBuilder withTestPlanKey(String testPlanKey) {
            this.testPlanKey = testPlanKey;
            return this;
        }

        public ServerDCBuilder withTestExecKey(String testExecKey) {
            this.testExecKey = testExecKey;
            return this;
        }

        public ServerDCBuilder withTestEnvironment(String testEnvironment) {
            this.testEnvironment = testEnvironment;
            return this;
        }

        public XrayResultsImporter build() {
            return new XrayResultsImporter(this);
        }
    
    }

    public static class CloudBuilder {

        private final String clientId;
        private final String clientSecret;

        private String projectKey;
        private String fixVersion;
        private String revision;
        private String testPlanKey;
        private String testExecKey;
        private String testEnvironment;

        private Boolean ignoreSslErrors = false;
        private Boolean useInternalTestProxy = false;

        public CloudBuilder(String clientId, String clientSecret) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }

        public CloudBuilder withIgnoreSslErrors(Boolean ignoreSslErrors) {
            this.ignoreSslErrors = ignoreSslErrors;
            return this;
        }

        public CloudBuilder withInternalTestProxy(Boolean useInternalTestProxy) {
            this.useInternalTestProxy = useInternalTestProxy;
            return this;
        }

        public CloudBuilder withProjectKey(String projectKey) {
            this.projectKey = projectKey;
            return this;
        }

        public CloudBuilder withVersion(String fixVersion) {
            this.fixVersion = fixVersion;
            return this;
        }

        public CloudBuilder withRevision(String revision) {
            this.revision = revision;
            return this;
        }

        public CloudBuilder withTestPlanKey(String testPlanKey) {
            this.testPlanKey = testPlanKey;
            return this;
        }

        public CloudBuilder withTestExecKey(String testExecKey) {
            this.testExecKey = testExecKey;
            return this;
        }

        public CloudBuilder withTestEnvironment(String testEnvironment) {
            this.testEnvironment = testEnvironment;
            return this;
        }

        public XrayResultsImporter build() {
            return new XrayResultsImporter(this);
        }
    
    }    

    public String submit(String format, String reportFile) throws Exception {
        if (clientId != null) {
            return submitStandardCloud(format, reportFile);
        } else {
            return submitStandardServerDC(format, reportFile);
        }
    }

    public String submitMultipartServerDC(String format, String reportFile, JSONObject testExecInfo, JSONObject testInfo) throws Exception {        
        OkHttpClient client = CommonUtils.getHttpClient(this.useInternalTestProxy, this.ignoreSslErrors);

        String credentials;
        if (jiraPersonalAccessToken!= null) {
            credentials = "Bearer " + jiraPersonalAccessToken;
        } else {
            credentials = Credentials.basic(jiraUsername, jiraPassword);
        } 

        String supportedFormats[] = new String [] { XRAY_FORMAT, JUNIT_FORMAT, TESTNG_FORMAT, ROBOT_FORMAT, NUNIT_FORMAT, XUNIT_FORMAT, CUCUMBER_FORMAT, BEHAVE_FORMAT }; 
        if (!Arrays.asList(supportedFormats).contains(format)) {
            throw new Exception("unsupported report format: " + format);
        }

        MediaType mediaType;
        String xmlBasedFormats[] = new String [] { JUNIT_FORMAT, TESTNG_FORMAT, ROBOT_FORMAT, NUNIT_FORMAT, XUNIT_FORMAT}; 
        if (Arrays.asList(xmlBasedFormats).contains(format)) {
            mediaType = MEDIA_TYPE_XML;
        } else {
            mediaType = MEDIA_TYPE_JSON;
        }

        String endpointUrl;
        if (XRAY_FORMAT.equals(format)) {
            endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution/multipart"; 
        } else {
            endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution/" + format + "/multipart"; 
        }
        
        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();
        MultipartBody requestBody = null;
        String partName;
        // for xray json, cucumber and behave reports use "result" instead of "file" for the multipart entity
        if (XRAY_FORMAT.equals(format) || CUCUMBER_FORMAT.equals(format) || BEHAVE_FORMAT.equals(format)) {
            partName = "result";
        } else {
            partName = "file";
        }
        try {
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(partName, reportFile, RequestBody.create(new File(reportFile), mediaType))
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
            String responseBody = response.body().string();
            if (response.isSuccessful()){
                JSONObject responseObj = new JSONObject(responseBody);
                // System.out.println("Test Execution: "+((JSONObject)(responseObj.get("testExecIssue"))).get("key"));
                return responseBody;
            } else {
                //System.err.println(responseBody);
                throw new IOException("Unexpected HTTP code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String submitMultipartCloud(String format, String reportFile, JSONObject testExecInfo, JSONObject testInfo) throws Exception {  	
        OkHttpClient client = CommonUtils.getHttpClient(this.useInternalTestProxy, this.ignoreSslErrors);

		String authenticationPayload = "{ \"client_id\": \"" + clientId +"\", \"client_secret\": \"" + clientSecret +"\" }";
		RequestBody body = RequestBody.create(authenticationPayload, MEDIA_TYPE_JSON);
		Request request = new Request.Builder().url(xrayCloudAuthenticateUrl).post(body).build();
		Response response = null;
		String authToken = null;
		try {
			response = client.newCall(request).execute();
			String responseBody = response.body().string();
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

        String supportedFormats[] = new String [] { XRAY_FORMAT, JUNIT_FORMAT, TESTNG_FORMAT, ROBOT_FORMAT, NUNIT_FORMAT, XUNIT_FORMAT, CUCUMBER_FORMAT }; 
        if (!Arrays.asList(supportedFormats).contains(format)) {
            throw new Exception("unsupported report format: " + format);
        }

        MediaType mediaType;
        String xmlBasedFormats[] = new String [] { JUNIT_FORMAT, TESTNG_FORMAT, ROBOT_FORMAT, NUNIT_FORMAT, XUNIT_FORMAT}; 
        if (Arrays.asList(xmlBasedFormats).contains(format)) {
            mediaType = MEDIA_TYPE_XML;
        } else {
            mediaType = MEDIA_TYPE_JSON;
        }

        String endpointUrl;
        if ("xray".equals(format)) {
            endpointUrl =  xrayCloudApiBaseUrl + "/import/execution/multipart";
        } else {
            endpointUrl =  xrayCloudApiBaseUrl + "/import/execution/" + format + "/multipart";
        }

        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();
        MultipartBody requestBody = null;
        try {
            requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("results", reportFile, RequestBody.create(new File(reportFile), mediaType))
                    .addFormDataPart("info", "info.json", RequestBody.create(testExecInfo.toString(), MEDIA_TYPE_JSON))
                    .build();
        } catch (Exception e1) {
            e1.printStackTrace();
            throw e1;
        }

        request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
        response = null;
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();
            if (response.isSuccessful()){
                JSONObject responseObj = new JSONObject(responseBody);
                // System.out.println("Test Execution: "+responseObj.get("key"));
                return responseBody;
            } else {
                //System.err.println(responseBody);
                throw new IOException("Unexpected HTTP code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

    }

    public String submitStandardServerDC(String format, String reportFile) throws Exception {        
        OkHttpClient client = CommonUtils.getHttpClient(this.useInternalTestProxy, this.ignoreSslErrors);

        String credentials;
        if (jiraPersonalAccessToken!= null) {
            credentials = "Bearer " + jiraPersonalAccessToken;
        } else {
            credentials = Credentials.basic(jiraUsername, jiraPassword);
        } 
       
        String supportedFormats[] = new String [] { XRAY_FORMAT, JUNIT_FORMAT, TESTNG_FORMAT, ROBOT_FORMAT, NUNIT_FORMAT, XUNIT_FORMAT, CUCUMBER_FORMAT, BEHAVE_FORMAT }; 
        if (!Arrays.asList(supportedFormats).contains(format)) {
            throw new Exception("unsupported report format: " + format);
        }

        MediaType mediaType;
        String xmlBasedFormats[] = new String [] { JUNIT_FORMAT, TESTNG_FORMAT, ROBOT_FORMAT, NUNIT_FORMAT, XUNIT_FORMAT}; 
        if (Arrays.asList(xmlBasedFormats).contains(format)) {
            mediaType = MEDIA_TYPE_XML;
        } else {
            mediaType = MEDIA_TYPE_JSON;
        }

        String endpointUrl;
        if (XRAY_FORMAT.equals(format)) {
            endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution";
        } else {
            endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/execution/" + format;
        }

        Request request;
        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();
        // for cucumber and behave reports send the report directly on the body
        try {
            if (XRAY_FORMAT.equals(format) || CUCUMBER_FORMAT.equals(format) || BEHAVE_FORMAT.equals(format) ) {
                String reportContent = new String ( Files.readAllBytes( Paths.get(reportFile) ) );
                RequestBody requestBody = RequestBody.create(reportContent, mediaType);
                request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
            } else {
                MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", reportFile, RequestBody.create(new File(reportFile), mediaType))
                .build();

                // for cucumber and behave formats, these URL parameters are not yet available
                if (projectKey != null) {
                    builder.addQueryParameter("projectKey", this.projectKey);
                }
                if (fixVersion != null) {
                    builder.addQueryParameter("fixVersion", this.fixVersion);
                }
                if (revision != null) {
                    builder.addQueryParameter("revision", this.revision);
                }
                if (testPlanKey != null) {
                    builder.addQueryParameter("testPlanKey", this.testPlanKey);
                }
                if (testExecKey != null) {
                    builder.addQueryParameter("testExecKey", this.testExecKey);
                }
                if (testEnvironment != null) {
                    builder.addQueryParameter("testEnvironment", this.testEnvironment);
                }
                request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            throw e1;
        }

        Response response = null;
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();
            if (response.isSuccessful()){
                JSONObject responseObj = new JSONObject(responseBody);
                // System.out.println("Test Execution: "+((JSONObject)(responseObj.get("testExecIssue"))).get("key"));
                return(responseBody);
            } else {
                //System.err.println(responseBody);
                throw new IOException("Unexpected HTTP code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public String submitStandardCloud(String format, String reportFile) throws Exception {
        OkHttpClient client = CommonUtils.getHttpClient(useInternalTestProxy, ignoreSslErrors);

        String authenticationPayload = "{ \"client_id\": \"" + clientId +"\", \"client_secret\": \"" + clientSecret +"\" }";
        RequestBody body = RequestBody.create(authenticationPayload, MEDIA_TYPE_JSON);
        Request request = new Request.Builder().url(xrayCloudAuthenticateUrl).post(body).build();
        Response response = null;
        String authToken = null;
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();
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

        String supportedFormats[] = new String [] { XRAY_FORMAT, JUNIT_FORMAT, TESTNG_FORMAT, ROBOT_FORMAT, NUNIT_FORMAT, XUNIT_FORMAT, CUCUMBER_FORMAT };
        if (!Arrays.asList(supportedFormats).contains(format)) {
            throw new Exception("unsupported report format: " + format);
        }

        MediaType mediaType;
        String xmlBasedFormats[] = new String [] { JUNIT_FORMAT, TESTNG_FORMAT, ROBOT_FORMAT, NUNIT_FORMAT, XUNIT_FORMAT}; 
        if (Arrays.asList(xmlBasedFormats).contains(format)) {
            mediaType = MEDIA_TYPE_XML;
        } else {
            mediaType = MEDIA_TYPE_JSON;
        }
    

        String endpointUrl;
        if (XRAY_FORMAT.equals(format)) {
            endpointUrl = xrayCloudApiBaseUrl + "/import/execution";
        } else {
            endpointUrl = xrayCloudApiBaseUrl + "/import/execution/" + format;
        }
        RequestBody requestBody = null;
        try {
            String reportContent = new String ( Files.readAllBytes( Paths.get(reportFile) ) );
            requestBody = RequestBody.create(reportContent, mediaType);
        } catch (Exception e1) {
            e1.printStackTrace();
            throw e1;
        }

        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();

        // cucumber, behave and xray endpoints dont support these URL parameters

        if (projectKey != null) {
            builder.addQueryParameter("projectKey", this.projectKey);
        }
        if (fixVersion != null) {
            builder.addQueryParameter("fixVersion", this.fixVersion);
        }
        if (revision != null) {
            builder.addQueryParameter("revision", this.revision);
        }
        if (testPlanKey != null) {
            builder.addQueryParameter("testPlanKey", this.testPlanKey);
        }
        if (testExecKey != null) {
            builder.addQueryParameter("testExecKey", this.testExecKey);
        }
        if (testEnvironment != null) {
            builder.addQueryParameter("testEnvironment", this.testEnvironment);
        }

        request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();            
            if (response.isSuccessful()){
                JSONObject responseObj = new JSONObject(responseBody);
                // System.out.println("Test Execution: "+((JSONObject)(responseObj.get("testExecIssue"))).get("key"));
                return(responseBody);
            } else {
                //System.err.println(responseBody);
                throw new IOException("Unexpected HTTP code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

    }

}

