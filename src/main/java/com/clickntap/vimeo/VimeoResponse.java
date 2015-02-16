package com.clickntap.vimeo;

import org.json.JSONObject;

public class VimeoResponse {
    private JSONObject json;
    private int statusCode;

    public VimeoResponse(JSONObject json, int statusCode) {
        this.json = json;
        this.statusCode = statusCode;
    }

    public JSONObject getJson() {
        return json;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String toString() {
        return new StringBuffer("HTTP Status Code: \n").append(getStatusCode()).append("\nJson: \n").append(getJson().toString(2)).toString();
    }
}
