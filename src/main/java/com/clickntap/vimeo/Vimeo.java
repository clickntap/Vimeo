package com.clickntap.vimeo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

public class Vimeo {
    private static final String VIMEO_SERVER = "https://api.vimeo.com";
    private String token;
    private String tokenType;

    public Vimeo(String token) {
        this(token, "bearer");
    }

    public Vimeo(String token, String tokenType) {
        this.token = token;
        this.tokenType = tokenType;
    }

    public VimeoResponse getVideoInfo(String videoEndpoint) throws IOException {
        return apiRequest(videoEndpoint, HttpGet.METHOD_NAME, null, null);
    }

    public VimeoResponse updateVideoMetadata(String videoEndpoint, String name, String description, String license, String privacyView, String privacyEmbed, boolean reviewLink) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("description", description);
        params.put("license", license);
        params.put("privacy.view", privacyView);
        params.put("privacy.embed", privacyEmbed);
        params.put("review_link", reviewLink ? "true" : "false");
        return apiRequest(videoEndpoint, HttpPatch.METHOD_NAME, params, null);
    }

    public VimeoResponse addVideoPrivacyDomain(String videoEndpoint, String domain) throws ClientProtocolException, UnsupportedEncodingException, IOException {
        return apiRequest(new StringBuffer(videoEndpoint).append("/privacy/domains/").append(URLEncoder.encode(domain, "UTF-8")).toString(), HttpPut.METHOD_NAME, null, null);
    }

    public VimeoResponse getVideoPrivacyDomains(String videoEndpoint) throws IOException {
        return apiRequest(new StringBuffer(videoEndpoint).append("/privacy/domains").toString(), HttpGet.METHOD_NAME, null, null);
    }

    public VimeoResponse removeVideo(String videoEndpoint) throws IOException {
        return apiRequest(videoEndpoint, HttpDelete.METHOD_NAME, null, null);
    }

    public VimeoResponse getMe() throws IOException {
        return apiRequest("/me", HttpGet.METHOD_NAME, null, null);
    }

    public VimeoResponse getVideos() throws IOException {
        return apiRequest("/me/videos", HttpGet.METHOD_NAME, null, null);
    }

    public VimeoResponse searchVideos(String query) throws IOException {
        return apiRequest("/me/videos?query=" + query, HttpGet.METHOD_NAME, null, null);
    }

    public VimeoResponse searchVideos(String query, String pageNumber, String itemsPerPage) throws IOException {
        String apiRequestEndpoint = "/me/videos?page=" + pageNumber + "&per_page=" + itemsPerPage + "&query=" + query;
        return apiRequest(apiRequestEndpoint, HttpGet.METHOD_NAME, null, null);
    }

    public VimeoResponse beginUploadVideo(Map<String, String> params) throws IOException {
        return apiRequest("/me/videos", HttpPost.METHOD_NAME, params, null);
    }

    public VimeoResponse uploadVideo(File file, String uploadLinkSecure) throws IOException {
        return apiRequest(uploadLinkSecure, HttpPut.METHOD_NAME, null, file);
    }

    public VimeoResponse endUploadVideo(String completeUri) throws IOException {
        return apiRequest(completeUri, HttpDelete.METHOD_NAME, null, null);
    }

    public String addVideo(File file, boolean upgradeTo1080) throws IOException, VimeoException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", "streaming");
        params.put("redirect_url", "");
        params.put("upgrade_to_1080", upgradeTo1080 ? "true" : "false");
        VimeoResponse response = beginUploadVideo(params);
        if (response.getStatusCode() == 201) {
            uploadVideo(file, response.getJson().getString("upload_link_secure"));
            response = endUploadVideo(response.getJson().getString("complete_uri"));
            if (response.getStatusCode() == 201) {
                return response.getJson().getString("Location");
            }
        }
		throw new VimeoException(new StringBuffer("HTTP Status Code: ").append(response.getStatusCode()).toString());
    }

    public VimeoResponse likesVideo(String videoId) throws IOException {
        return apiRequest(new StringBuffer("/me/likes/").append(videoId).toString(), HttpGet.METHOD_NAME, null, null);
    }

    public VimeoResponse likeVideo(String videoId) throws IOException {
        return apiRequest(new StringBuffer("/me/likes/").append(videoId).toString(), HttpPut.METHOD_NAME, null, null);
    }

    public VimeoResponse unlikeVideo(String videoId) throws IOException {
        return apiRequest(new StringBuffer("/me/likes/").append(videoId).toString(), HttpDelete.METHOD_NAME, null, null);
    }

    public VimeoResponse checkEmbedPreset(String videoEndPoint, String presetId) throws IOException {
        return apiRequest(new StringBuffer(videoEndPoint).append("/presets/").append(presetId).toString(), HttpGet.METHOD_NAME, null, null);
    }

    public VimeoResponse addEmbedPreset(String videoEndPoint, String presetId) throws IOException {
        return apiRequest(new StringBuffer(videoEndPoint).append("/presets/").append(presetId).toString(), HttpPut.METHOD_NAME, null, null);
    }

    public VimeoResponse removeEmbedPreset(String videoEndPoint, String presetId) throws IOException {
        return apiRequest(new StringBuffer(videoEndPoint).append("/presets/").append(presetId).toString(), HttpDelete.METHOD_NAME, null, null);
    }

    public VimeoResponse getTextTracks(String videoEndPoint) throws IOException {
        return apiRequest(new StringBuffer(videoEndPoint).append("/texttracks").toString(), HttpGet.METHOD_NAME, null, null);
    }

    public VimeoResponse getTextTrack(String videoEndPoint, String textTrackId) throws IOException {
        return apiRequest(new StringBuffer(videoEndPoint).append("/texttracks/").append(textTrackId).toString(), HttpGet.METHOD_NAME, null, null);
    }

    public String addTextTrack(String videoEndPoint, File file, boolean active, String type, String language, String name) throws IOException, VimeoException {

        VimeoResponse response = null;

        Map<String, String> params = new HashMap<String, String>();
        params.put("active", active ? "true" : "false");
        params.put("type", type);
        params.put("language", language);
        params.put("name", name);

        VimeoResponse addVideoRespose = apiRequest(new StringBuffer(videoEndPoint).append("/texttracks").toString(), HttpPost.METHOD_NAME, params, null);

        if (addVideoRespose.getStatusCode() == 201) {
            String textTrackUploadLink = addVideoRespose.getJson().getString("link");
            response = apiRequest(textTrackUploadLink, HttpPut.METHOD_NAME, null, file);
            if (response.getStatusCode() == 200) {
                return addVideoRespose.getJson().getString("uri");
            }
        }
        throw new VimeoException(new StringBuffer("HTTP Status Code: ").append(response.getStatusCode()).toString());

    }

    public VimeoResponse updateTextTrack(String videoEndPoint, String textTrackUri, boolean active, String type, String language, String name) throws IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("active", active ? "true" : "false");
        params.put("type", type);
        params.put("language", language);
        params.put("name", name);
        return apiRequest(new StringBuffer(videoEndPoint).append(textTrackUri).toString(), HttpPatch.METHOD_NAME, params, null);
    }

    public VimeoResponse removeTextTrack(String videoEndPoint, String textTrackId) throws IOException {
        return apiRequest(new StringBuffer(videoEndPoint).append("/texttracks/").append(textTrackId).toString(), HttpDelete.METHOD_NAME, null, null);
    }

    private VimeoResponse apiRequest(String endpoint, String methodName, Map<String, String> params, File file) throws IOException {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpRequestBase request = null;
        String url = null;
        if (endpoint.startsWith("http")) {
            url = endpoint;
        } else {
            url = new StringBuffer(VIMEO_SERVER).append(endpoint).toString();
        }
        if (methodName.equals(HttpGet.METHOD_NAME)) {
            request = new HttpGet(url);
        } else if (methodName.equals(HttpPost.METHOD_NAME)) {
            request = new HttpPost(url);
        } else if (methodName.equals(HttpPut.METHOD_NAME)) {
            request = new HttpPut(url);
        } else if (methodName.equals(HttpDelete.METHOD_NAME)) {
            request = new HttpDelete(url);
        } else if (methodName.equals(HttpPatch.METHOD_NAME)) {
            request = new HttpPatch(url);
        }
        request.addHeader("Accept", "application/vnd.vimeo.*+json; version=3.2");
        request.addHeader("Authorization", new StringBuffer(tokenType).append(" ").append(token).toString());
        HttpEntity entity = null;
        if (params != null) {
            ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            for (String key : params.keySet()) {
                postParameters.add(new BasicNameValuePair(key, params.get(key)));
            }
            entity = new UrlEncodedFormEntity(postParameters);
        } else if (file != null) {
            entity = new FileEntity(file, ContentType.MULTIPART_FORM_DATA);
        }
        if (entity != null) {
            if (request instanceof HttpPost) {
                ((HttpPost) request).setEntity(entity);
            } else if (request instanceof HttpPatch) {
                ((HttpPatch) request).setEntity(entity);
            } else if (request instanceof HttpPut) {
                ((HttpPut) request).setEntity(entity);
            }
        }
        CloseableHttpResponse response = client.execute(request);
        String responseAsString = null;
        int statusCode = response.getStatusLine().getStatusCode();
        if (methodName.equals(HttpPut.METHOD_NAME) || methodName.equals(HttpDelete.METHOD_NAME)) {
            JSONObject out = new JSONObject();
            for (Header header : response.getAllHeaders()) {
                out.put(header.getName(), header.getValue());
            }
            responseAsString = out.toString();
        } else if (statusCode != 204) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            responseAsString = out.toString("UTF-8");
            out.close();
        }
        JSONObject json = null;
        try {
            json = new JSONObject(responseAsString);
        } catch (Exception e) {
            json = new JSONObject();
        }
        VimeoResponse vimeoResponse = new VimeoResponse(json, statusCode);
        response.close();
        client.close();
        return vimeoResponse;
    }
}
