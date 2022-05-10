package com.clickntap.vimeo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

public class Vimeo {
  private static final String UTF_8 = "UTF-8";
  private static final String VIMEO_VERSION = "3.4";
  private static final String VIMEO_SERVER = "https://api.vimeo.com";
  private String token;
  private String tokenType;
  private URL proxy;

  public Vimeo(String token) {
    this(token, "bearer");
  }

  public Vimeo(String token, String tokenType) {
    this.token = token;
    this.tokenType = tokenType;
  }

  public URL getProxy() {
    return proxy;
  }

  public void setProxy(URL proxy) {
    this.proxy = proxy;
  }

  public VimeoResponse getVideoInfo(String endpoint) throws IOException {
    return get(endpoint);
  }

  public VimeoResponse get(String endpoint) throws IOException {
    return get(endpoint, null, null);
  }

  public VimeoResponse get(String endpoint, Object params, Map<String, String> headers) throws IOException {
    return apiRequest(endpoint, HttpGet.METHOD_NAME, params, headers);
  }

  public VimeoResponse post(String endpoint) throws IOException {
    return post(endpoint, null, null);
  }

  public VimeoResponse post(String endpoint, Object params, Map<String, String> headers) throws IOException {
    return apiRequest(endpoint, HttpPost.METHOD_NAME, params, headers);
  }

  public VimeoResponse put(String endpoint) throws IOException {
    return put(endpoint, null, null);
  }

  public VimeoResponse put(String endpoint, Object params, Map<String, String> headers) throws IOException {
    return apiRequest(endpoint, HttpPut.METHOD_NAME, params, headers);
  }

  public VimeoResponse delete(String endpoint) throws IOException {
    return delete(endpoint, null, null);
  }

  public VimeoResponse delete(String endpoint, Object params, Map<String, String> headers) throws IOException {
    return apiRequest(endpoint, HttpDelete.METHOD_NAME, params, headers);
  }

  public VimeoResponse patch(String endpoint) throws IOException {
    return patch(endpoint, null, null);
  }

  public VimeoResponse patch(String endpoint, Object params, Map<String, String> headers) throws IOException {
    return apiRequest(endpoint, HttpPatch.METHOD_NAME, params, headers);
  }

  public VimeoResponse head(String endpoint) throws IOException {
    return head(endpoint, null, null);
  }

  public VimeoResponse head(String endpoint, Object params, Map<String, String> headers) throws IOException {
    return apiRequest(endpoint, HttpHead.METHOD_NAME, params, headers);
  }

  public VimeoResponse updateVideoMetadata(String videoEndpoint, String name, String description, String license, String privacyView, String privacyEmbed, boolean reviewLink) throws IOException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("name", name);
    params.put("description", description);
    params.put("license", license);
    params.put("privacy.view", privacyView);
    params.put("privacy.embed", privacyEmbed);
    params.put("review_page.active", reviewLink ? "true" : "false");
    return patch(videoEndpoint, params, null);
  }

  public VimeoResponse addVideoPrivacyDomain(String videoEndpoint, String domain) throws ClientProtocolException, UnsupportedEncodingException, IOException {
    domain = URLEncoder.encode(domain, UTF_8);
    return put(new StringBuffer(videoEndpoint).append("/privacy/domains/").append(domain).toString());
  }

  public VimeoResponse getVideoPrivacyDomains(String videoEndpoint) throws IOException {
    return get(new StringBuffer(videoEndpoint).append("/privacy/domains").toString());
  }

  public VimeoResponse removeVideo(String videoEndpoint) throws IOException {
    return delete(videoEndpoint);
  }

  public VimeoResponse setVideoThumb(String videoEndpoint, float time, boolean active) throws IOException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("time", Float.toString(time));
    params.put("active", Boolean.toString(active));
    return post(new StringBuffer(videoEndpoint).append("/pictures").toString(), params, null);
  }

  public VimeoResponse getMe() throws IOException {
    return get("/me");
  }

  public VimeoResponse getVideos() throws IOException {
    return get("/me/videos");
  }

  public VimeoResponse searchVideos(String query) throws IOException {
    return searchVideos(query, null, null);
  }

  public VimeoResponse searchVideos(String query, String pageNumber, String itemsPerPage) throws IOException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("query", query);
    params.put("page", pageNumber);
    params.put("per_page", itemsPerPage);
    return searchVideos(params);
  }

  public VimeoResponse searchVideos(Map<String, String> params) throws IOException {
    return get("/videos", params, null);
  }

  public String addVideo(File file) throws IOException, VimeoException {
    return addVideo(new FileInputStream(file), file.length(), null, null);
  }

  public String addVideo(File file, String name, Map<String, String> privacy) throws IOException, VimeoException {
    return addVideo(new FileInputStream(file), file.length(), name, privacy);
  }

  public String addVideo(byte[] bytes, long fileSize) throws IOException, VimeoException {
    return addVideo(new ByteArrayInputStream(bytes), fileSize, null, null);
  }

  public String addVideo(byte[] bytes, long fileSize, String name, Map<String, String> privacy) throws IOException, VimeoException {
    return addVideo(new ByteArrayInputStream(bytes), fileSize, name, privacy);
  }

  public String addVideo(InputStream inputStream, long fileSize, String name, Map<String, String> privacy) throws IOException, VimeoException {
    VimeoResponse response = beginUploadVideo(fileSize, name, privacy);
    if (response.getStatusCode() == 200) {
      JSONObject upload = response.getJson().getJSONObject("upload");
      if ("tus".equalsIgnoreCase(upload.getString("approach"))) {
        uploadVideo(upload.getString("upload_link"), inputStream);
        return response.getJson().getString("uri");
      }
    }
    throw new VimeoException(new StringBuffer("HTTP Status Code: ").append(response.getStatusCode()).toString());
  }

  public VimeoResponse beginUploadVideo(long fileSize, String name, Map<String, String> privacy) throws IOException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("upload.approach", "tus");
    params.put("upload.size", fileSize + "");
    if (name != null) {
      params.put("name", name);
    }
    if (privacy != null) {
      for (String key : privacy.keySet()) {
        params.put("privacy." + key, privacy.get(key));
      }
    }
    return post("/me/videos", params, null);
  }

  public VimeoResponse uploadVideo(String uploadLink, byte[] bytes) throws IOException {
    return uploadVideo(uploadLink, new ByteArrayInputStream(bytes));
  }

  public VimeoResponse uploadVideo(String uploadLink, File file) throws IOException {
    return uploadVideo(uploadLink, new FileInputStream(file));
  }

  public VimeoResponse uploadVideo(String uploadLink, InputStream inputStream) throws IOException {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Tus-Resumable", "1.0.0");
    headers.put("Upload-Offset", "0");
    headers.put("Content-Type", " application/offset+octet-stream");
    return patch(uploadLink, inputStream, headers);
  }

  public VimeoResponse uploadVerify(String uploadLink) throws IOException {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("Tus-Resumable", "1.0.0");
    return head(uploadLink, null, headers);
  }

  public VimeoResponse likesVideo(String videoId) throws IOException {
    return get(new StringBuffer("/me/likes/").append(videoId).toString());
  }

  public VimeoResponse likeVideo(String videoId) throws IOException {
    return put(new StringBuffer("/me/likes/").append(videoId).toString());
  }

  public VimeoResponse unlikeVideo(String videoId) throws IOException {
    return delete(new StringBuffer("/me/likes/").append(videoId).toString());
  }

  public VimeoResponse checkEmbedPreset(String videoEndPoint, String presetId) throws IOException {
    return get(new StringBuffer(videoEndPoint).append("/presets/").append(presetId).toString());
  }

  public VimeoResponse addEmbedPreset(String videoEndPoint, String presetId) throws IOException {
    return put(new StringBuffer(videoEndPoint).append("/presets/").append(presetId).toString());
  }

  public VimeoResponse removeEmbedPreset(String videoEndPoint, String presetId) throws IOException {
    return delete(new StringBuffer(videoEndPoint).append("/presets/").append(presetId).toString());
  }

  public VimeoResponse getTextTracks(String videoEndPoint) throws IOException {
    return get(new StringBuffer(videoEndPoint).append("/texttracks").toString());
  }

  public VimeoResponse getTextTrack(String videoEndPoint, String textTrackId) throws IOException {
    return get(new StringBuffer(videoEndPoint).append("/texttracks/").append(textTrackId).toString());
  }

  public String addTextTrack(String videoEndPoint, File file, boolean active, String type, String language, String name) throws IOException, VimeoException {
    return addTextTrack(videoEndPoint, new FileInputStream(file), active, type, language, name);
  }

  public String addTextTrack(String videoEndPoint, byte[] bytes, boolean active, String type, String language, String name) throws IOException, VimeoException {
    return addTextTrack(videoEndPoint, new ByteArrayInputStream(bytes), active, type, language, name);
  }

  public String addTextTrack(String videoEndPoint, InputStream inputStream, boolean active, String type, String language, String name) throws IOException, VimeoException {
    Map<String, String> params = new HashMap<String, String>();
    params.put("active", active ? "true" : "false");
    params.put("type", type);
    params.put("language", language);
    params.put("name", name);

    VimeoResponse addVideoRespose = post(new StringBuffer(videoEndPoint).append("/texttracks").toString(), params, null);
    VimeoResponse response = null;
    if (addVideoRespose.getStatusCode() == 201) {
      String textTrackUploadLink = addVideoRespose.getJson().getString("link");
      response = apiRequest(textTrackUploadLink, HttpPut.METHOD_NAME, inputStream, null);
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
    return patch(new StringBuffer(videoEndPoint).append(textTrackUri).toString(), params, null);
  }

  public VimeoResponse removeTextTrack(String videoEndPoint, String textTrackId) throws IOException {
    return delete(new StringBuffer(videoEndPoint).append("/texttracks/").append(textTrackId).toString());
  }

  protected VimeoResponse apiRequest(String endpoint, String methodName, Object params, Map<String, String> headers) throws IOException {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpRequestBase request = null;
    String url = null;
    if (endpoint.startsWith("http")) {
      url = endpoint;
    } else {
      url = new StringBuffer(VIMEO_SERVER).append(endpoint).toString();
    }
    if (methodName.equals(HttpGet.METHOD_NAME)) {
      if (params instanceof Map) {
        StringBuffer urlWithParams = new StringBuffer(url).append("?");
        Map<String, String> map = (Map<String, String>) params;
        for (String key : map.keySet()) {
          String value = map.get(key);
          if (value != null) {
            urlWithParams.append(key).append("=").append(URLEncoder.encode(value, UTF_8)).append("&");
          }
        }
        url = urlWithParams.toString();
        if (url.endsWith("?") || url.endsWith("&")) {
          url = url.substring(0, url.length() - 1);
        }
      }
      request = new HttpGet(url);
    } else if (methodName.equals(HttpPost.METHOD_NAME)) {
      request = new HttpPost(url);
    } else if (methodName.equals(HttpPut.METHOD_NAME)) {
      request = new HttpPut(url);
    } else if (methodName.equals(HttpDelete.METHOD_NAME)) {
      request = new HttpDelete(url);
    } else if (methodName.equals(HttpPatch.METHOD_NAME)) {
      request = new HttpPatch(url);
    } else if (methodName.equals(HttpHead.METHOD_NAME)) {
      request = new HttpHead(url);
    }

    if (headers != null) {
      for (String key : headers.keySet()) {
        request.addHeader(key, headers.get(key));
      }
    }
    request.addHeader("Accept", "application/vnd.vimeo.*+json;version=" + VIMEO_VERSION);
    request.addHeader("Authorization", new StringBuffer(tokenType).append(' ').append(token).toString());

    HttpEntity entity = null;
    if (params != null) {
      if (params instanceof JSONObject) {
        JSONObject jsonParams = (JSONObject) params;
        entity = new ByteArrayEntity(jsonParams.toString().getBytes(UTF_8));
      } else if (params instanceof File) {
        File file = (File) params;
        entity = new FileEntity(file, ContentType.MULTIPART_FORM_DATA);
      } else if (params instanceof InputStream) {
        InputStream inputStream = (InputStream) params;
        entity = new InputStreamEntity(inputStream, ContentType.MULTIPART_FORM_DATA);
      } else {
        Map<String, String> map = (Map<String, String>) params;
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        for (String key : map.keySet()) {
          postParameters.add(new BasicNameValuePair(key, map.get(key)));
        }
        entity = new UrlEncodedFormEntity(postParameters);
      }
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

// Applying proxy to the request
    if (proxy != null) {
      HttpHost httpProxy = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getProtocol());
      RequestConfig config = RequestConfig.custom().setProxy(httpProxy).build();
      request.setConfig(config);
    }

    CloseableHttpResponse response = client.execute(request);
    String responseAsString = null;
    int statusCode = response.getStatusLine().getStatusCode();
    if (methodName.equals(HttpPut.METHOD_NAME) || methodName.equals(HttpDelete.METHOD_NAME) || methodName.equals(HttpHead.METHOD_NAME)) {
      JSONObject out = new JSONObject();
      for (Header header : response.getAllHeaders()) {
        out.put(header.getName(), header.getValue());
      }
      responseAsString = out.toString();
    } else if (statusCode != 204) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      response.getEntity().writeTo(out);
      responseAsString = out.toString(UTF_8);
      out.close();
    }
    JSONObject responseJson = null;
    JSONObject responseHeaders = null;
    try {
      responseJson = new JSONObject(responseAsString);
      responseHeaders = new JSONObject();
      for (Header header : response.getAllHeaders()) {
        responseHeaders.put(header.getName(), header.getValue());
      }
    } catch (Exception e) {
      responseJson = new JSONObject();
      responseHeaders = new JSONObject();
    }
    VimeoResponse vimeoResponse = new VimeoResponse(responseJson, responseHeaders, statusCode);
    response.close();
    client.close();
    return vimeoResponse;
  }
}