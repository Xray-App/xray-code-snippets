package com.xblend.xray;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// https://docs.getxray.app/display/XRAYCLOUD/Exporting+Cucumber+Tests+-+REST+v2
// https://docs.getxray.app/display/XRAY/Exporting+Cucumber+Tests+-+REST

public class XrayFeaturesExporter {
    private final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private final String xrayCloudApiBaseUrl = "https://xray.cloud.getxray.app/api/v2";
    private final String xrayCloudAuthenticateUrl = xrayCloudApiBaseUrl + "/authenticate";

    private String jiraBaseUrl;
    private String jiraUsername;
    private String jiraPassword;
    private String jiraPersonalAccessToken;

    private String clientId;
    private String clientSecret;

    private String issueKeys;
    private String filterId;

    private boolean ignoreSslErrors = false;
    private boolean useInternalTestProxy = false;

    private XrayFeaturesExporter(ServerDCBuilder builder) {
        this.jiraBaseUrl = builder.jiraBaseUrl;
        this.jiraUsername = builder.jiraUsername;
        this.jiraPassword = builder.jiraPassword;
        this.jiraPersonalAccessToken = builder.jiraPersonalAccessToken;

        this.issueKeys = builder.issueKeys;
        this.filterId = builder.filterId;

        this.ignoreSslErrors = builder.ignoreSslErrors;
        this.useInternalTestProxy = builder.useInternalTestProxy;
    }

    private XrayFeaturesExporter(CloudBuilder builder) {
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;

        this.issueKeys = builder.issueKeys;
        this.filterId = builder.filterId;

        this.ignoreSslErrors = builder.ignoreSslErrors;
        this.useInternalTestProxy = builder.useInternalTestProxy;
    }

    public static class ServerDCBuilder {

        private final String jiraBaseUrl;
        private String jiraUsername;
        private String jiraPassword;
        private String jiraPersonalAccessToken;

        private String issueKeys;
        private String filterId;

        private Boolean useInternalTestProxy = false;
        private Boolean ignoreSslErrors = false;

        public ServerDCBuilder(String jiraBaseUrl, String jiraUsername, String jiraPassword) {
            this.jiraBaseUrl = jiraBaseUrl;
            this.jiraUsername = jiraUsername;
            this.jiraPassword = jiraPassword;
        }

        public ServerDCBuilder(String jiraBaseUrl, String jiraPersonalAccessToken) {
            this.jiraBaseUrl = jiraBaseUrl;
            this.jiraPersonalAccessToken = jiraPersonalAccessToken;
        }

        public ServerDCBuilder withInternalTestProxy(Boolean useInternalTestProxy) {
            this.useInternalTestProxy = useInternalTestProxy;
            return this;
        }

        public ServerDCBuilder withIgnoreSslErrors(Boolean ignoreSslErrors) {
            this.ignoreSslErrors = ignoreSslErrors;
            return this;
        }

        public ServerDCBuilder withIssueKeys(String issueKeys) {
            this.issueKeys = issueKeys;
            return this;
        }

        public ServerDCBuilder withFilterId(String filterId) {
            this.filterId = filterId;
            return this;
        }

        public XrayFeaturesExporter build() {
            return new XrayFeaturesExporter(this);
        }

    }

    public static class CloudBuilder {

        private final String clientId;
        private final String clientSecret;

        private String issueKeys;
        private String filterId;

        private boolean ignoreSslErrors = false;
        private boolean useInternalTestProxy = false;

        public CloudBuilder(String clientId, String clientSecret) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
        }

        public CloudBuilder withInternalTestProxy(Boolean useInternalTestProxy) {
            this.useInternalTestProxy = useInternalTestProxy;
            return this;
        }

        public CloudBuilder withIgnoreSslErrors(Boolean ignoreSslErrors) {
            this.ignoreSslErrors = ignoreSslErrors;
            return this;
        }

        public CloudBuilder withIssueKeys(String issueKeys) {
            this.issueKeys = issueKeys;
            return this;
        }

        public CloudBuilder withFilterId(String filterId) {
            this.filterId = filterId;
            return this;
        }

        public XrayFeaturesExporter build() {
            return new XrayFeaturesExporter(this);
        }

    }

    public String submit(String outputPath) throws Exception {
        if (clientId != null) {
            return submitStandardCloud(outputPath);
        } else {
            return submitStandardServerDC(outputPath);
        }
    }

    public String submitStandardServerDC(String outputPath) throws Exception {
        OkHttpClient client = CommonUtils.getHttpClient(this.useInternalTestProxy, this.ignoreSslErrors);

        String credentials;
        if (jiraPersonalAccessToken!= null) {
            credentials = "Bearer " + jiraPersonalAccessToken;
        } else {
            credentials = Credentials.basic(jiraUsername, jiraPassword);
        }

        String endpointUrl = jiraBaseUrl + "/rest/raven/2.0/export/test";
        Request request;
        Response response = null;
        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();

        builder.addQueryParameter("fz", "true");
        if (issueKeys != null) {
            builder.addQueryParameter("keys", this.issueKeys);
        }
        if (filterId != null) {
            builder.addQueryParameter("filter", this.filterId);
        }

        request = new Request.Builder().url(builder.build()).get().addHeader("Authorization", credentials).build();
        try {
            response = client.newCall(request).execute();
            // String responseBody = response.body().string();
            if (response.isSuccessful()) {
                unzipContentsToFolder(response.body().byteStream(), outputPath);
                return ("ok");
            } else {
                throw new IOException("Unexpected HTTP code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw (e);
        }
    }

    public String submitStandardCloud(String outputPath) throws Exception {
        OkHttpClient client = CommonUtils.getHttpClient(this.useInternalTestProxy, this.ignoreSslErrors);

        String authenticationPayload = "{ \"client_id\": \"" + clientId + "\", \"client_secret\": \"" + clientSecret
                + "\" }";
        RequestBody body = RequestBody.create(authenticationPayload, MEDIA_TYPE_JSON);
        Request request = new Request.Builder().url(xrayCloudAuthenticateUrl).post(body).build();
        Response response = null;
        String authToken = null;
        try {
            response = client.newCall(request).execute();
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                authToken = responseBody.replace("\"", "");
            } else {
                throw new IOException("failed to authenticate " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        String credentials = "Bearer " + authToken;

        String endpointUrl = xrayCloudApiBaseUrl + "/export/cucumber";
        HttpUrl url = HttpUrl.get(endpointUrl);
        HttpUrl.Builder builder = url.newBuilder();

        // builder.addQueryParameter("fz", "false");
        if (issueKeys != null) {
            builder.addQueryParameter("keys", this.issueKeys);
        }
        if (filterId != null) {
            builder.addQueryParameter("filter", this.filterId);
        }

        request = new Request.Builder().url(builder.build()).get().addHeader("Authorization", credentials).build();
        try {
            response = client.newCall(request).execute();
            // String responseBody = response.body().string();
            if (response.isSuccessful()) {
                unzipContentsToFolder(response.body().byteStream(), outputPath);
                return ("ok");
            } else {
                throw new IOException("Unexpected HTTP code " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw (e);
        }
    }

    private void unzipContentsToFolder(InputStream zippedContents, String outputFolder) throws Exception {
        File destDir = new File(outputFolder);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zippedContents));
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            File newFile = newFile(destDir, zipEntry);

            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
        }
        zis.closeEntry();
        zis.close();
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

}
