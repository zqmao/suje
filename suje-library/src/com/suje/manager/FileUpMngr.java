package com.suje.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suje.db.JsFile;
import com.suje.db.JsFileUp;
import com.suje.db.helper.SujeDbHelper;
import com.suje.http.AsyncHttpResponseHandler;
import com.suje.http.RequestHandle;
import com.suje.http.RequestParams;
import com.suje.http.TextHttpResponseHandler;
import com.suje.util.FileMetaUtil;
import com.suje.util.FileUtil;

/**
 * 文件下载管理器
 * @author mac
 *
 */
public class FileUpMngr {

	private static Context mContext;
	private static FileUpMngr INSTANCE;

	/**
	 * 任务列表
	 */
	private Map<String, RequestHandle> handles = new HashMap<String, RequestHandle>();

	/**
	 * 监听列表
	 */
	private List<ProgressChangeUp> listeners = new ArrayList<ProgressChangeUp>();

	public static FileUpMngr getInstance(Context context) {
		if(INSTANCE == null){
			INSTANCE = new FileUpMngr(context);
		}
		return INSTANCE;
	}

	private FileUpMngr(Context context){
		mContext = context;
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
	public void addListenner(ProgressChangeUp listener){
		if(listeners.contains(listener)){
			return;
		}
		listeners.add(listener);
	}

	/**
	 * 取消监听
	 * @param listener
	 */
	public void removeListenner(ProgressChangeUp listener){
		listeners.remove(listener);
	}

	public void preUploadChunk(final String fileId, final String preUploadUrl, final String filePath, final Map<String, String> paramMap, final Map<String, String> headers, final JSONObject h5){
		FileUtil.chunkFile(fileId, filePath, new Runnable(){

			@Override
			public void run() {
				preUpload(fileId, preUploadUrl, filePath, paramMap, headers, h5);
			}
			
		});
	}
	public void preUpload(final String fileId, final String preUploadUrl, final String filePath, Map<String, String> paramMap, Map<String, String> headers, final JSONObject h5){
		final File file = new File(filePath);
		FileHttpClient.getClient().addHeaders(headers);
		RequestParams params = new RequestParams();
		params.put("name", file.getName());
		params.put("size", file.length());
        String md5 = FileMetaUtil.getFileMeta(file).getMd5();
        String blockMD5 = FileMetaUtil.getFileMeta(file).getHeader();
        params.put("md5", md5);
        params.put("blockMD5", blockMD5);
		for(String key : paramMap.keySet()){
			params.put(key, paramMap.get(key));
		}
		FileHttpClient.getClient().put(preUploadUrl, "application/json", params, new TextHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				for(ProgressChangeUp listener : listeners){
					listener.onPreFailure(responseString, h5);
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				for(ProgressChangeUp listener : listeners){
					listener.onUploadSuccess(fileId, h5);
				}
				JSONObject object = JSON.parseObject(responseString);
				String serverFileId = object.getString("fileId");
				String uploadUrl = object.getString("uploadUrl");
				if (TextUtils.isEmpty(serverFileId) || TextUtils.isEmpty(uploadUrl)){
					for(ProgressChangeUp listener : listeners){
						listener.onProgressChange(fileId, (int)file.length(), (int)file.length());
					}
					try {
						JsFileUp jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, fileId);
						jsFileUp.setStatus(JsFile.STATUS_SUCC);
						jsFileUp.setSize((int)file.length());
						SujeDbHelper.getInstance(mContext).update(jsFileUp);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
				//把serverFileId和uploadUrl保存到数据库，后面每次刷新都要使用到
				try {
					JsFileUp jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, fileId);
					jsFileUp.setServerFileId(serverFileId);
					jsFileUp.setUploadUrl(uploadUrl);
					jsFileUp.setPartId(0);
					SujeDbHelper.getInstance(mContext).update(jsFileUp);
				} catch (Exception e) {
					e.printStackTrace();
				}
				for(ProgressChangeUp listener : listeners){
					listener.onPreSuccess(fileId, "", uploadUrl, h5);
				}
			}
		});
	}

	public void refreshUpload(final String fileId, String refreshUrl, Map<String, String> headers, final JSONObject h5){
		String uploadUrl = "";
		try {
			JsFileUp jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, fileId);
			uploadUrl = jsFileUp.getUploadUrl();
		}catch (Exception e){
			e.printStackTrace();
		}

		FileHttpClient.getClient().addHeaders(headers);
		RequestParams params = new RequestParams();
		params.put("uploadUrl", uploadUrl);
		FileHttpClient.getClient().put(refreshUrl, "application/json", params, new TextHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Log.e("FIleUpMngr", "responseString:" + responseString);
				for(ProgressChangeUp listener : listeners){
					listener.onPreFailure(responseString, h5);
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				JSONObject object = JSON.parseObject(responseString);
				String uploadUrl = object.getString("uploadUrl");
				//refresh获得的uploadUrl，不用保存，上传后，就失效了
				for(ProgressChangeUp listener : listeners){
					listener.onPreSuccess(fileId, "", uploadUrl, h5);
				}
			}
		});
	}

	public void upload(final String id, String uploadUrl, final String filePath,final JSONObject h5){
        final File file = new File(filePath);
		JsFileUp jsFileUp = null;
		try {
			jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, id);
		}catch (Exception e){
			e.printStackTrace();
		}
		if(jsFileUp == null){
			for(ProgressChangeUp listener : listeners){
				listener.onFailure(id, "there are not item in db ");
			}
			return;
		}
        final int total = FileUtil.getChunkCount(filePath);
		//数据库存储的partId是从1开始的，所以这里不需要加1
		final int index = jsFileUp.getPartId();
		if(index >= total){
			//上传完毕
			FileUpMngr.getInstance(mContext).uploadCommit(id);
			return;
		}
		RequestParams params = new RequestParams();
		try {
			File chunkFile = FileUtil.getChunkFile(id, index + 1);
			if(!chunkFile.exists()){
				for(ProgressChangeUp listener : listeners){
					listener.onFailure(id, "file not exist");
				}
				return;
			}
			params.put("upload", chunkFile, "application/octet-stream");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		String tempUrl = uploadUrl + "?partId=" + (index + 1);
		RequestHandle requestHandle = FileHttpClient.getClient().put(tempUrl, "application/octet-stream", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				Log.e("FIleUpMngr", "responseBody:" + new String(responseBody));
				//每上传一个分片，就保存下数据库，然后开始下一个分片的上传
				try {
                    long size = FileUtil.getChunkSize(filePath, index + 1);
					JsFileUp jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, id);
					jsFileUp.setPartId(index + 1);
					jsFileUp.setSize(size);
					SujeDbHelper.getInstance(mContext).update(jsFileUp);
                    FileUtil.deleteChunkFile(id, index + 1);
				}catch (Exception e){
					e.printStackTrace();
				}
				if(index + 1 >= total){
					FileUpMngr.getInstance(mContext).uploadCommit(id);
					return;
				}
				for(ProgressChangeUp listener : listeners){
					listener.onEverySuccess(id, "", h5);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				stopTask(id);
				String msg = new String(responseBody);
				if(statusCode == 0){
					msg = "network";
				}
				for(ProgressChangeUp listener : listeners){
					listener.onEveryFailure(msg, h5);
				}
			}

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				Log.e("FileUpMngr", "write:" + bytesWritten + " totalSize:"+totalSize+"   total:" + file.length());
				super.onProgress(bytesWritten, totalSize);
				if(!isLoading(id)){
					return;
				}
				long size = FileUtil.getChunkSize(filePath, index);
				for(ProgressChangeUp listener : listeners){
					listener.onProgressChange(id, (int)size + bytesWritten, (int)file.length());
				}
			}
		});
		handles.put(id, requestHandle);
	}

	public void uploadCommit(final String fileId){
		String filePath = "";
		String commitUploadUrl = "";
		try {
			JsFileUp jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, fileId);
			if(jsFileUp == null){
				return;
			}
			filePath = jsFileUp.getUrl();
			commitUploadUrl = jsFileUp.getUploadUrl() + "?commit";
		}catch (Exception e){
			e.printStackTrace();
		}
		int chunkCount = FileUtil.getChunkCount(filePath);
		JSONArray parts = new JSONArray();
		for(int i = 1; i <= chunkCount; i++){
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("partId", i);
			parts.add(jsonObject);
		}
		RequestParams params = new RequestParams();
		params.put("parts", parts);
		FileHttpClient.getClient().put(commitUploadUrl, "application/json", params, new TextHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Log.e("FileUpMngr", "commit upload fail");
				for(ProgressChangeUp listener : listeners){
					listener.onFailure(fileId, responseString);
				}
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseString) {
				Log.e("FileUpMngr", "commit upload success");
				//上传完毕
				for(ProgressChangeUp listener : listeners){
					listener.onSuccess(fileId);
				}
			}
		});
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
		}
		return result;
	}
}

