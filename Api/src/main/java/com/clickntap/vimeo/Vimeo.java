package com.clickntap.vimeo;

public class Vimeo {
	private String bearerToken;

	public Vimeo(String bearerToken) {
		this.bearerToken = bearerToken;
	}

	public String getVideos() {
		HttpClient client = new DefaultHttpClient();
		HttpPost httpGet = new HttpPost("https://api.vimeo.com/me/videos");
		httpGet.addHeader("Authorization", "bearer 43f9dad9af69ca1d5e9a9158439f02d8");
		httpGet.addHeader("Accept", "application/vnd.vimeo.*+json; version=3.2");
		HttpResponse response;
		return "{}";
	}

	public static void main(String[] args) {
		System.out.println("Hello World!");
	}
}
