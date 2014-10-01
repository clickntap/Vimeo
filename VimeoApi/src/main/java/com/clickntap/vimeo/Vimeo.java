package com.clickntap.vimeo;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
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

	public JSONObject getVideos() throws Exception {
		return apiRequest("/me/videos", HttpGet.METHOD_NAME, null);
	}

	public JSONObject uploadVideo(Map<String, String> params) throws Exception {
		return apiRequest("/me/videos", HttpPost.METHOD_NAME, params);
	}

	private JSONObject apiRequest(String endpoint, String methodName, Map<String, String> params) throws Exception {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpRequestBase request = null;
		if (methodName.equals(HttpGet.METHOD_NAME)) {
			request = new HttpGet(new StringBuffer(VIMEO_SERVER).append(endpoint).toString());
		} else if (methodName.equals(HttpPost.METHOD_NAME)) {
			request = new HttpPost(new StringBuffer(VIMEO_SERVER).append(endpoint).toString());
		}
		if (params != null && request instanceof HttpPost) {
			ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
			for (String key : params.keySet()) {
				postParameters.add(new BasicNameValuePair(key, params.get(key)));
			}
			((HttpPost) request).setEntity(new UrlEncodedFormEntity(postParameters));
		}
		request.addHeader("Authorization", new StringBuffer("bearer ").append(bearerToken).toString());
		request.addHeader("Accept", "application/vnd.vimeo.*+json; version=3.2");
		CloseableHttpResponse response = client.execute(request);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		response.getEntity().writeTo(out);
		String json = out.toString("UTF-8");
		out.close();
		response.close();
		client.close();
		return new JSONObject(json);
	}
}
