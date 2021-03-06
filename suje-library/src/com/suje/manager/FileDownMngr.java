package com.suje.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.suje.http.RangeFileAsyncHttpResponseHandler;
import com.suje.http.RequestHandle;
import com.suje.http.RequestParams;
import com.suje.http.TextHttpResponseHandler;

/**
 * 文件下载管理器
 * @author mac
 *
 */
public class FileDownMngr {

	private static FileDownMngr INSTANCE;
	
	/**
	 * 任务列表
	 */
	private Map<String, RequestHandle> handles = new HashMap<String, RequestHandle>();
	
	/**
	 * 任务列表
	 */
	private Map<String, DownloadFile> files = new HashMap<String, DownloadFile>();
	
	/**
	 * 监听列表
	 */
	private List<ProgressChangeDown> listeners = new ArrayList<ProgressChangeDown>();
	
	public static FileDownMngr getInstance(Context context) {
		if(INSTANCE == null){
			INSTANCE = new FileDownMngr(context);
		}
		return INSTANCE;
	}
	
	private FileDownMngr(Context context){
	}

	/**
	 * 是否正在下载
	 * @param id
	 * @return
	 */
	public boolean isLoading(String id){
		return handles.containsKey(id);
	}
	
	/**
	 * 添加监听
	 * @param listener
	 */
	public void addListenner(ProgressChangeDown listener){
		if(listeners.contains(listener)){
			return;
		}
		listeners.add(listener);
	}
	
	/**
	 * 取消监听
	 * @param listener
	 */
	public void removeListenner(ProgressChangeDown listener){
		listeners.remove(listener);
	}

	public void preDownload(final String fileId, final String thirdExterpriseId, String url, Map<String, String> headers, final JSONObject h5){
		//如果本地数据库没有记录，就初始化；否则就继续下载
		//初始化获取下载地址
		FileHttpClient.getClient().addHeaders(headers);
		FileHttpClient.getClient().get(url, new RequestParams(), new TextHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				//初始化失败
				for(ProgressChangeDown listener : listeners){
					listener.onPreFailure(responseString, h5);
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				JSONObject object = JSON.parseObject(responseString);
				String downloadUrl = object.getString("downloadUrl");
				//初始化成功
				for(ProgressChangeDown listener : listeners){
					listener.onPreSuccess(fileId, thirdExterpriseId, downloadUrl, h5);
				}
			}
		});
	}
	/**
	 * 创建下载任务
	 * @param id
	 * @param url
	 */
	public void downLoadTask(final String id, final String url, final File file){
		Log.e("downloadZqmao", id + "downLoadTask listeners.size : " + listeners.size());
		if(handles.containsKey(id)){
			return;
		}
		if(handles.size() > 10){
			Log.e("downloadZqmao", " handles.size > 10 ");
			if(files.containsKey(id)){
				Log.e("downloadZqmao", " containsKey ");
				return;
			}
			DownloadFile downloadFile = new DownloadFile(id, url, file);
			files.put(id, downloadFile);
			return;
		}
		RangeFileAsyncHttpResponseHandler rangeHandler = new RangeFileAsyncHttpResponseHandler(file) {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, File file) {
				Log.e("downloadZqmao", "onSuccess listeners.size : " + listeners.size());
				for(ProgressChangeDown listener : listeners){
					listener.onSuccess(id);
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
				stopTask(id);
				Log.e("downloadZqmao", "statusCode:" + statusCode + " throwable:" + throwable.getMessage());
				String msg = throwable.getMessage();
				if(statusCode == 0){
					msg = "network";
				}
				for(ProgressChangeDown listener : listeners){
					listener.onFailure(id, msg);
				}
			}
			
			@Override
			public void onFinish() {
				Log.e("downloadZqmao", "onFinish:");
				handles.remove(id);
				files.remove(id);
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					
					@Override
					public void run() {
						for(DownloadFile downloadFile : files.values()){
							downLoadTask(downloadFile.getId(), downloadFile.getUrl(), downloadFile.getFile());
						}
					}
				});
			}
			
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				if(!isLoading(id)){
					return;
				}
				for(ProgressChangeDown listener : listeners){
					listener.onProgressChange(id, bytesWritten, totalSize);
				}
			}
		};
		
		RequestHandle request = FileHttpClient.getClient().get(url, new RequestParams(), rangeHandler);
		handles.put(id, request);
		
	}
	
	/**
	 * 停止任务
	 * @param id
	 * @return
	 */
	public boolean stopTask(String id){
		RequestHandle request = handles.get(id);
		if(request == null){
			return true;
		}
		boolean result = request.cancel(true);
		if(result){
			handles.remove(id);
			files.remove(id);
		}
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			
			@Override
			public void run() {
				for(DownloadFile downloadFile : files.values()){
					downLoadTask(downloadFile.getId(), downloadFile.getUrl(), downloadFile.getFile());
				}
			}
		});
		
		return result;
	}
}

