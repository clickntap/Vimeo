package com.clickntap.vimeo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
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
	private String bearerToken;

	public Vimeo(String bearerToken) {
		this.bearerToken = bearerToken;
	}

	public JSONObject getVideoInfo(String endpoint) throws Exception {
		return apiRequest(endpoint, HttpGet.METHOD_NAME, null, null);
	}

	public JSONObject getVideos() throws Exception {
		return apiRequest("/me/videos", HttpGet.METHOD_NAME, null, null);
	}

	public JSONObject beginUploadVideo(Map<String, String> params) throws Exception {
		return apiRequest("/me/videos", HttpPost.METHOD_NAME, params, null);
	}

	public JSONObject uploadVideo(File file, String uploadLinkSecure) throws Exception {
		return apiRequest(uploadLinkSecure, HttpPut.METHOD_NAME, null, file);
	}

	public JSONObject endUploadVideo(String completeUri) throws Exception {
		return apiRequest(completeUri, HttpDelete.METHOD_NAME, null, null);
	}

	public String addVideo(File file, boolean upgradeTo1080) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("type", "streaming");
		params.put("redirect_url", "");
		params.put("upgrade_to_1080", upgradeTo1080 ? "true" : "false");
		JSONObject info = beginUploadVideo(params);
		uploadVideo(file, info.getString("upload_link_secure"));
		info = endUploadVideo(info.getString("complete_uri"));
		return info.getString("Location");
	}

	private JSONObject apiRequest(String endpoint, String methodName, Map<String, String> params, File file) throws Exception {
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
		}
		request.addHeader("Accept", "application/vnd.vimeo.*+json; version=3.2");
		request.addHeader("Authorization", new StringBuffer("bearer ").append(bearerToken).toString());
		if (params != null && request instanceof HttpPost) {
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			for (String key : params.keySet()) {
				postParameters.add(new BasicNameValuePair(key, params.get(key)));
			}
			((HttpPost) request).setEntity(new UrlEncodedFormEntity(postParameters));
		} else if (file != null && request instanceof HttpPut) {
			FileEntity fileEntity = new FileEntity(file, ContentType.MULTIPART_FORM_DATA);
			((HttpPut) request).setEntity(fileEntity);
		}
		CloseableHttpResponse response = client.execute(request);
		String json = null;
		if (methodName.equals(HttpDelete.METHOD_NAME)) {
			JSONObject out = new JSONObject();
			for (Header header : response.getAllHeaders()) {
				out.put(header.getName(), header.getValue());
			}
			json = out.toString();
		} else {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			response.getEntity().writeTo(out);
			json = out.toString("UTF-8");
			out.close();
		}
		response.close();
		client.close();
		try {
			return new JSONObject(json);
		} catch (Exception e) {
			return new JSONObject();
		}
	}
}
