package com.suje.manager;

import java.util.Map;
import java.util.Set;

import org.apache.http.client.HttpClient;

import android.content.Context;

import com.suje.http.AsyncHttpClient;
import com.suje.http.AsyncHttpResponseHandler;
import com.suje.http.RequestHandle;
import com.suje.http.RequestParams;

public class FileHttpClient {
	public static final String CHECH_LOGIN_FIRST = "CHECH_LOGIN_FIRST__";
	public static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
	private static FileHttpClient seuHttpClient;
	private AsyncHttpClient client;
	public static  FileHttpClient getClient() {
		if(seuHttpClient == null){
			AsyncHttpClient client = new AsyncHttpClient(true, 80, 443);
			client.setMaxRetriesAndTimeout(3, 3000);
			client.setTimeout(150000);
			client.setMaxConnections(50);
			client.setUserAgent(USER_AGENT);
			seuHttpClient = new FileHttpClient();
			seuHttpClient.client = client;
		}else{
			seuHttpClient.client.setTimeout(150000);
			seuHttpClient.client.setMaxConnections(50);
			seuHttpClient.client.setMaxRetriesAndTimeout(3, 3000);
		}
		return seuHttpClient;
	}

	
	private FileHttpClient() {
		
	}
	
	public HttpClient getHttpClient(){
		return client.getHttpClient();
	}
	
	
	public RequestHandle post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		return post(null, url, params, responseHandler);
	}
	
	public RequestHandle post(final Context ctx, final String url, final RequestParams params, final AsyncHttpResponseHandler responseHandler){
		RequestHandle handle;
		handle = client.post(ctx, url, params, responseHandler);
		return handle;
	}


	public RequestHandle get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		return get(null, url, params, responseHandler);
	}
	public RequestHandle get(final Context ctx, final String url, final RequestParams params, final AsyncHttpResponseHandler responseHandler){
		RequestHandle handle;
		handle = client.get(ctx, url, params, responseHandler);
		return handle;
	}

	public RequestHandle put(String url, String contentType, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		return put(null, url, contentType, params, responseHandler);
	}
	public RequestHandle put(final Context ctx, final String url, String contentType, final RequestParams params, final AsyncHttpResponseHandler responseHandler){
		RequestHandle handle;
		handle = client.put(ctx, url, contentType, params, responseHandler);
		return handle;
	}

	public void addHeaders(Map<String, String> headers){
		if(client != null){
			if(headers != null && !headers.isEmpty()){
				Set<String> keySet = headers.keySet();
				for(String key : keySet){
					client.addHeader(key, headers.get(key));
				}
			}
		}
	}
}
