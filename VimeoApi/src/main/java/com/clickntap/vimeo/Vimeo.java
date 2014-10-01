package com.clickntap.vimeo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

public class Vimeo {
	private static final String VIMEO_SERVER = "https://api.vimeo.com";
	private String bearerToken;

	public Vimeo(String bearerToken) {
		this.bearerToken = bearerToken;
	}

	public JSONObject getVideos() throws Exception {
		return apiRequest("/me/videos", HttpGet.METHOD_NAME);
	}

	private JSONObject apiRequest(String endpoint, String methodName) throws IOException, ClientProtocolException, UnsupportedEncodingException, JSONException {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpRequestBase request = null;
		if (methodName.equals(HttpGet.METHOD_NAME)) {
			request = new HttpGet(VIMEO_SERVER + endpoint);
		} else if (methodName.equals(HttpPost.METHOD_NAME)) {
			request = new HttpPost(VIMEO_SERVER + endpoint);
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

	public static void main(String[] args) throws Exception {
		Vimeo vimeo = new Vimeo("");
		System.out.println(vimeo.getVideos().toString(2));
	}
}
