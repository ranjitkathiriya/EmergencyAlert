package com.example.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class JSONParser {
	static InputStream is = null;
	static String json = "";
	String str = "";

	public String getJSONFromUrl(String url) {

		// Making HTTP request
		try {
			URI website = null;
			try {
				website = new URI(url);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(website);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			str = EntityUtils.toString(entity);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return str;

	}
}
