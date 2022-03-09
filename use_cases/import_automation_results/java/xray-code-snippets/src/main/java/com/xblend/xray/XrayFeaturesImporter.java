package com.xblend.xray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MultipartBody.Builder;

// https://docs.getxray.app/display/XRAYCLOUD/Importing+Cucumber+Tests+-+REST+v2
// https://docs.getxray.app/display/XRAY/Importing+Cucumber+Tests+-+REST

public class XrayFeaturesImporter {
    private final MediaType MEDIA_TYPE_ZIP = MediaType.parse("application/zip");
    private final MediaType MEDIA_TYPE_FOR_FEATURE_FILES = MediaType.parse("text/plain");
    private final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private final String xrayCloudApiBaseUrl = "https://xray.cloud.getxray.app/api/v2";
    private final String xrayCloudAuthenticateUrl = xrayCloudApiBaseUrl + "/authenticate";

    private String jiraBaseUrl;
    private String jiraUsername;
    private String jiraPassword;
    private String jiraPersonalAccessToken;

    private String clientId;
    private String clientSecret;

    private String projectKey;
    private String projectId;
    private String source;

    private boolean ignoreSslErrors = false;
    private boolean useInternalTestProxy = false;

    private XrayFeaturesImporter(ServerDCBuilder builder) {
        this.jiraBaseUrl = builder.jiraBaseUrl;
        this.jiraUsername = builder.jiraUsername;
        this.jiraPassword = builder.jiraPassword;
        this.jiraPersonalAccessToken = builder.jiraPersonalAccessToken;

        this.projectKey = builder.projectKey;
        this.projectId = builder.projectId;
        this.source = builder.source;

        this.ignoreSslErrors = builder.ignoreSslErrors;
        this.useInternalTestProxy = builder.useInternalTestProxy;
    }

    private XrayFeaturesImporter(CloudBuilder builder) {
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;

        this.projectKey = builder.projectKey;
        this.projectId = builder.projectId;
        this.source = builder.source;

        this.ignoreSslErrors = builder.ignoreSslErrors;
        this.useInternalTestProxy = builder.useInternalTestProxy;
    }

    public static class ServerDCBuilder {

        private final String jiraBaseUrl;
        private String jiraUsername;
        private String jiraPassword;
        private String jiraPersonalAccessToken;

        private String projectKey;
        private String projectId;   // unused
        private String source;      // unused

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

        public XrayFeaturesImporter build() {
            return new XrayFeaturesImporter(this);
        }

    }

    public static class CloudBuilder {

        private final String clientId;
        private final String clientSecret;

        private String projectKey;
        private String projectId;
        private String source;

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

        public CloudBuilder withProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public CloudBuilder withSource(String source) {
            this.source = source;
            return this;
        }

        public XrayFeaturesImporter build() {
            return new XrayFeaturesImporter(this);
        }

    }

    public JSONArray importFrom(String inputPath) throws Exception {
        if (clientId != null) {
            return importCloud(inputPath, null, null);
        } else {
            return importServerDC(inputPath, null);
        }
    }

    public JSONArray importFrom(String inputPath, String testInfo) throws Exception {
        if (clientId != null) {
            return importCloud(inputPath, testInfo, null);
        } else {
            return importServerDC(inputPath, testInfo);
        }
    }

    public JSONArray importFrom(String inputPath, String testInfo, String precondInfo) throws Exception {
        if (clientId != null) {
            return importCloud(inputPath, testInfo, precondInfo);
        } else {
            return importServerDC(inputPath, testInfo);
        }
    }

    public JSONArray importServerDC(String inputPath, String testInfo) throws Exception {
        OkHttpClient client = CommonUtils.getHttpClient(this.useInternalTestProxy, this.ignoreSslErrors);

        File inputFile = new File(inputPath);
        String credentials;
        if (jiraPersonalAccessToken!= null) {
            credentials = "Bearer " + jiraPersonalAccessToken;
        } else {
            credentials = Credentials.basic(jiraUsername, jiraPassword);
        }

        String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/import/feature";
        Response response = null;
        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();
        MultipartBody requestBody = null;

        if (projectKey != null) {
            builder.addQueryParameter("projectKey", this.projectKey);
        }

        MediaType mediaType;
        if (inputPath.toLowerCase().endsWith(".zip")) {
            mediaType = MEDIA_TYPE_ZIP;
        } else if (inputPath.toLowerCase().endsWith(".feature")) {
            mediaType = MEDIA_TYPE_FOR_FEATURE_FILES;
        } else {
            // it may be a directory; check it, and if so zip it before sending it
            mediaType = MEDIA_TYPE_ZIP;
            if (inputFile.isDirectory()) {
                Path tempZip = Files.createTempFile("dummy", ".zip");
                //System.out.println(tempZip.toFile().getAbsolutePath());
                zipDirectory(inputPath, tempZip.toFile().getAbsolutePath());
                inputFile = tempZip.toFile();
            }
        }

        String partName = "file";
        try {
            Builder requestBodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(partName, inputFile.getName(), RequestBody.create(inputFile, mediaType));
            if (testInfo != null) {
                requestBodyBuilder = requestBodyBuilder.addFormDataPart("testInfo", "info.json", RequestBody.create(testInfo, MEDIA_TYPE_JSON));
            }
            requestBody = requestBodyBuilder.build();
        } catch (Exception e1) {
            e1.printStackTrace();
            throw e1;
        }

        Request request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();
            if (response.isSuccessful()){
                JSONArray responseObj = new JSONArray(responseBody);
                return responseObj;
            } else {
                //System.err.println(responseBody);
                throw new IOException("Unexpected HTTP code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public JSONArray importCloud(String inputPath, String testInfo, String precondInfo) throws Exception {
        OkHttpClient client = CommonUtils.getHttpClient(this.useInternalTestProxy, this.ignoreSslErrors);
        
        File inputFile = new File(inputPath);
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

        String endpointUrl =  xrayCloudApiBaseUrl + "/import/feature";
        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();
        MultipartBody requestBody = null;

        if (projectKey != null) {
            builder.addQueryParameter("projectKey", this.projectKey);
        }
        if (projectId != null) {
            builder.addQueryParameter("projectId", this.projectId);
        }
        if (source != null) {
            builder.addQueryParameter("source", this.source);
        }

        MediaType mediaType;
        if (inputPath.toLowerCase().endsWith(".zip")) {
            mediaType = MEDIA_TYPE_ZIP;
        } else if (inputPath.toLowerCase().endsWith(".feature")) {
            mediaType = MEDIA_TYPE_FOR_FEATURE_FILES;
        } else {
            // it may be a directory; check it, and if so zip it before sending it
            mediaType = MEDIA_TYPE_ZIP;
            if (inputFile.isDirectory()) {
                Path tempZip = Files.createTempFile("dummy", ".zip");
                //System.out.println(tempZip.toFile().getAbsolutePath());
                zipDirectory(inputPath, tempZip.toFile().getAbsolutePath());
                inputFile = tempZip.toFile();
            }
        }

        String partName = "file";
        try {
            Builder requestBodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(partName, inputFile.getName(), RequestBody.create(inputFile, mediaType));
            if (testInfo != null) {
                requestBodyBuilder = requestBodyBuilder.addFormDataPart("testInfo", "testinfo.json", RequestBody.create(testInfo, MEDIA_TYPE_JSON));
            }
            if (precondInfo != null) {
                requestBodyBuilder = requestBodyBuilder.addFormDataPart("precondInfo", "precondinfo.json", RequestBody.create(precondInfo, MEDIA_TYPE_JSON));
            }
            requestBody = requestBodyBuilder.build();
        } catch (Exception e1) {
            e1.printStackTrace();
            throw e1;
        }

        request = new Request.Builder().url(builder.build()).post(requestBody).addHeader("Authorization", credentials).build();
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();
            if (response.isSuccessful()){
                JSONArray responseObj = new JSONArray();
                responseObj.put(new JSONObject(responseBody));
                return responseObj;
            } else {
                // System.err.println(responseBody);
                throw new IOException("Unexpected HTTP code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }


    public static void zipDirectory(String sourceDir, String outputZip) throws IOException {
        FileOutputStream fos = new FileOutputStream(outputZip);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceDir);

        zipFile(fileToZip, fileToZip.getName(), zipOut, false);
        zipOut.close();
        fos.close();
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, boolean createDir) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (createDir) {
                if (fileName.endsWith("/")) {
                    zipOut.putNextEntry(new ZipEntry(fileName));
                } else {
                    zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                }
                zipOut.closeEntry();
            }

            File[] children = fileToZip.listFiles();
            for (File childFile : children) {

                // zip only .feature files and subdirs
                if (!(childFile.isDirectory() || childFile.getName().toLowerCase().endsWith(".feature")))
                    continue;

                if (createDir) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut, true);
                } else {
                    zipFile(childFile, childFile.getName(), zipOut, true);
                }
                
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }
}
