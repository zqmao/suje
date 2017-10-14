package com.suje.core;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suje.db.JsFile;
import com.suje.db.JsFileDown;
import com.suje.db.JsFileUp;
import com.suje.db.helper.SujeDbHelper;
import com.suje.inject.JsCallback;
import com.suje.manager.FileDownMngr;
import com.suje.manager.FileUpMngr;
import com.suje.manager.ProgressChangeDown;
import com.suje.manager.ProgressChangeUp;
import com.suje.util.Contants;
import com.suje.util.FileUtil;
import com.suje.util.ImageUtil;
import com.suje.util.NetworkUtil;

/**
 * Created by zqmao on 2017/8/15.
 */

@SuppressLint("SimpleDateFormat") public class JsFileInject {


    private static Context mContext;
    private static JsCallback mProgressUpSuccessCallback;
    private static JsCallback mProgressUpFailureCallback;
    private static JsCallback mProgressDownSuccessCallback;
    private static JsCallback mProgressDownFailureCallback;
    
    /** upload start **/
    /**
     *
     * @param webView
     * @param success
     * @param error
     */
    private static JsCallback mUpListSuccessCallback;
    private static JsCallback mUpListFailureCallback;
    public static void getUploadFiles(WebView webView, String thirdExterpriseId, String pageNum, String pageSize, final String success, final String error) {
        mContext = webView.getContext();
        mUpListSuccessCallback = new JsCallback(webView, success);
        mUpListFailureCallback = new JsCallback(webView, error);
        try {
            List<JsFileUp> files = SujeDbHelper.getInstance(mContext).list(JsFileUp.class, thirdExterpriseId, pageNum, pageSize);
            String temp = JSON.toJSONString(files);
            JSONArray result = JSON.parseArray(temp);
            if(mUpListSuccessCallback != null){
            	mUpListSuccessCallback.apply(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(mUpListFailureCallback != null){
            	mUpListFailureCallback.apply(e.getMessage());
            }
        }
    }

    public static void uploadInject(WebView webView, final String success, final String error){
        mContext = webView.getContext();
        mProgressUpSuccessCallback = new JsCallback(webView, success);
        mProgressUpFailureCallback = new JsCallback(webView, error);
    }

    private static JsCallback mUpSuccessCallback;
    private static JsCallback mUpFailureCallback;
    public static void upload(WebView webView, String thirdExterpriseId, JSONObject config, final String success, final String error){
        mContext = webView.getContext();
        FileUpMngr.getInstance(mContext).addListenner(mUploadProgressChange);
        mUpSuccessCallback = new JsCallback(webView, success);
        mUpFailureCallback = new JsCallback(webView, error);
        String fileId = config.getString("fileId");
        String ownerId = config.getString("ownerId");
        String filePath = config.getString("fileUrl");
        String refreshUrl = config.getString("refreshUrl");
        String url = config.getString("serverUrl");
        JSONObject data = config.getJSONObject("data");
        Set<String> keys = data.keySet();
        final Map<String, String> paramsMap = new HashMap<String, String>();
        for(String key : keys){
            String value = data.getString(key);
            paramsMap.put(key, value);
        }
        final JSONObject h5 = config.getJSONObject("h5");
        JSONObject headers = config.getJSONObject("headers");
        Set<String> headKeys = headers.keySet();
        final Map<String, String> headerMap = new HashMap<String, String>();
        for(String key : headKeys){
            String value = headers.getString(key);
            headerMap.put(key, value);
        }
        File file = new File(filePath);
        if(!file.exists()){
            if(mUpFailureCallback != null){
            	mUpFailureCallback.apply("file " + filePath + " is not exists ", h5);
            }
            return;
        }
        JsFileUp jsFileUp = null;
        if(TextUtils.isEmpty(fileId)){
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            fileId = sdf.format(new Date());
            jsFileUp = new JsFileUp();
            jsFileUp.setId(fileId);
            jsFileUp.setUrl(filePath);
            jsFileUp.setDateTime(fileId);
            jsFileUp.setH5(h5.toJSONString());
            jsFileUp.setStatus(JsFile.STATUS_ING);
            jsFileUp.setThirdExterpriseId(thirdExterpriseId);
            jsFileUp.setName(file.getName());
            jsFileUp.setTotalSize(file.length());
            jsFileUp.setSize(0);
            jsFileUp.setRefreshUrl(refreshUrl);
            jsFileUp.setOwnerId(ownerId);
            try {
            	SujeDbHelper.getInstance(mContext).create(jsFileUp);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //如果fileId为空，就直接预上传
            //如果fileId不为空，不用预上传，直接刷新上传地址
            FileUpMngr.getInstance(mContext).preUploadChunk(fileId, url, filePath, paramsMap, headerMap, h5);
        }else{
            //1,先获取刷新地址
            //2,刷新上传地址接口
            try {
                jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, fileId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(jsFileUp == null){
                return;
            }
            String result = getRefreshUploadUrl(ownerId, jsFileUp.getServerFileId(), refreshUrl);
            if(TextUtils.isEmpty(result)){
            	if(mUpFailureCallback != null){
            		mUpFailureCallback.apply("refreshUrl is empty", h5);
                }
            	return;
            }
            FileUpMngr.getInstance(mContext).refreshUpload(fileId, result, headerMap, h5);
        }
    }

    private static JsCallback mUpPauseSuccessCallback;
    private static JsCallback mUpPauseFailureCallback;
    public static void uploadPause(WebView webView, final String fileId, JSONObject h5, final String success, final String error){
        mContext = webView.getContext();
        mUpPauseSuccessCallback = new JsCallback(webView, success);
        mUpPauseFailureCallback = new JsCallback(webView, error);
        if(TextUtils.isEmpty(fileId)){
            if(mUpPauseFailureCallback != null){
            	mUpPauseFailureCallback.apply("fileId is empty", h5);
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUpMngr.getInstance(mContext).stopTask(fileId);
            }
        }).start();
        Log.e("upload", "pause  fileId : " + fileId);
        try {
            JsFileUp jsFileUp = SujeDbHelper.getInstance(webView.getContext()).load(JsFileUp.class, fileId);
            if(jsFileUp != null){
                jsFileUp.setStatus(JsFile.STATUS_PAUSE);
                SujeDbHelper.getInstance(webView.getContext()).update(jsFileUp);
            }
            if(mUpPauseSuccessCallback != null){
            	mUpPauseSuccessCallback.apply(h5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsCallback mUpDeleteSuccessCallback;
    private static JsCallback mUpDeleteFailureCallback;
    public static void uploadDelete(WebView webView, final String fileId, JSONObject h5, final String success, final String error){
        mContext = webView.getContext();
        mUpDeleteSuccessCallback = new JsCallback(webView, success);
        mUpDeleteFailureCallback = new JsCallback(webView, error);
        if(TextUtils.isEmpty(fileId)){
            if(mUpDeleteFailureCallback != null){
            	mUpDeleteFailureCallback.apply("fileId is empty", h5);
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUpMngr.getInstance(mContext).stopTask(fileId);
            }
        }).start();
        Log.e("upload", "uploadDelete  fileId : " + fileId);
        try {
            JsFileUp jsFileUp = SujeDbHelper.getInstance(webView.getContext()).load(JsFileUp.class, fileId);
            if(jsFileUp != null){
                SujeDbHelper.getInstance(webView.getContext()).delete(jsFileUp);
            }
            String filePath = Contants.FILE_DOWNLOAD_PATH + jsFileUp.getName();
            File file = new File(filePath);
            if(file.exists()){
                file.delete();
            }
            if(mUpDeleteSuccessCallback != null){
            	mUpDeleteSuccessCallback.apply(h5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getRefreshUploadUrl(String ownerId, String serverFileId, String refreshUrl){
    	if(TextUtils.isEmpty(refreshUrl)){
    		return "";
    	}
        return refreshUrl.replaceAll(":ownerId", ownerId).replaceAll(":fileId", serverFileId);
    }
    /** upload end **/

    /** download start **/
    /**
     *
     * @param webView
     * @param success
     * @param error
     */
    private static JsCallback mDownListSuccessCallback;
    private static JsCallback mDownListFailureCallback;
    public static void getDownLoadFiles(WebView webView, String thirdExterpriseId, String pageNum, String pageSize, final String success, final String error) {
        mContext = webView.getContext();
        mDownListSuccessCallback = new JsCallback(webView, success);
        mDownListFailureCallback = new JsCallback(webView, error);
        //下载文件存放在sdcard中
        try {
            List<JsFileDown> files = SujeDbHelper.getInstance(mContext).list(JsFileDown.class, thirdExterpriseId, pageNum, pageSize);
            for(JsFileDown jsFileDown : files){
                String filePath = Contants.FILE_DOWNLOAD_PATH + jsFileDown.getName();
                File file = new File(filePath);
                if(file.exists()){
                    jsFileDown.setSize(file.length());
                }else{
                    jsFileDown.setSize(0);
                }
                SujeDbHelper.getInstance(mContext).update(jsFileDown);
            }
            String temp = JSON.toJSONString(files);
            JSONArray result = JSON.parseArray(temp);
            if(mDownListSuccessCallback != null){
            	mDownListSuccessCallback.apply(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(mDownListFailureCallback != null){
            	mDownListFailureCallback.apply(e.getMessage());
            }
        }
    }

    public static void downLoadInject(WebView webView, final String success, final String error){
        mContext = webView.getContext();
        mProgressDownSuccessCallback = new JsCallback(webView, success);
        mProgressDownFailureCallback = new JsCallback(webView, error);
    }

    private static JsCallback mDownSuccessCallback;
    private static JsCallback mDownFailureCallback;
    public static void download(WebView webView, String thirdExterpriseId, JSONObject config, final String success, final String error){
        mContext = webView.getContext();
        FileDownMngr.getInstance(mContext).addListenner(mDownloadProgressChange);
        mDownSuccessCallback = new JsCallback(webView, success);
        mDownFailureCallback = new JsCallback(webView, error);
        String fileId = config.getString("fileId");
        String url = config.getString("serverUrl");
        if(url.indexOf("?") != -1){
            url += "&";
        }else{
            url += "?";
        }
        JSONObject data = config.getJSONObject("data");
        Set<String> keys = data.keySet();
        for(String key : keys){
            String value = data.getString(key);
            url += key + "=" + value + "&";
        }
        if(url != null && (url.endsWith("&") || url.endsWith("?"))){
            url = url.substring(0, url.length() - 1);
        }
        JSONObject h5 = config.getJSONObject("h5");
        JSONObject headers = config.getJSONObject("headers");
        Set<String> headKeys = headers.keySet();
        Map<String, String> map = new HashMap<String, String>();
        for(String key : headKeys){
            String value = headers.getString(key);
            map.put(key, value);
        }
        FileDownMngr.getInstance(mContext).preDownload(fileId, thirdExterpriseId, url, map, h5);
    }

    private static JsCallback mDownPauseSuccessCallback;
    private static JsCallback mDownPauseFailureCallback;
    public static void downLoadPause(WebView webView, final String fileId, JSONObject h5, final String success, final String error){
        mContext = webView.getContext();
        mDownPauseSuccessCallback = new JsCallback(webView, success);
        mDownPauseFailureCallback = new JsCallback(webView, error);
        if(TextUtils.isEmpty(fileId)){
            if(mDownPauseFailureCallback != null){
            	mDownPauseFailureCallback.apply("fileId is empty", h5);
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileDownMngr.getInstance(mContext).stopTask(fileId);
            }
        }).start();
        Log.e("download", "pause  fileId : " + fileId);
        try {
            JsFileDown jsFileDown = SujeDbHelper.getInstance(webView.getContext()).load(JsFileDown.class, fileId);
            if(jsFileDown != null){
                jsFileDown.setStatus(JsFile.STATUS_PAUSE);
                SujeDbHelper.getInstance(webView.getContext()).update(jsFileDown);
            }
            if(mDownPauseSuccessCallback != null){
            	mDownPauseSuccessCallback.apply(h5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsCallback mDownDeleteSuccessCallback;
    private static JsCallback mDownDeleteFailureCallback;
    public static void downLoadDelete(WebView webView, final String fileId, JSONObject h5, final String success, final String error){
        mContext = webView.getContext();
        mDownDeleteSuccessCallback = new JsCallback(webView, success);
        mDownDeleteFailureCallback = new JsCallback(webView, error);
        if(TextUtils.isEmpty(fileId)){
            if(mDownDeleteFailureCallback != null){
            	mDownDeleteFailureCallback.apply("fileId is empty", h5);
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileDownMngr.getInstance(mContext).stopTask(fileId);
            }
        }).start();
        Log.e("downLoad", "downLoadDelete  fileId : " + fileId);
        try {
            JsFileDown jsFileDown = SujeDbHelper.getInstance(webView.getContext()).load(JsFileDown.class, fileId);
            if(jsFileDown != null){
                SujeDbHelper.getInstance(webView.getContext()).delete(jsFileDown);
            }
            String filePath = Contants.FILE_DOWNLOAD_PATH + jsFileDown.getName();
            File file = new File(filePath);
            if(file.exists()){
                file.delete();
            }
            if(mDownDeleteSuccessCallback != null){
            	mDownDeleteSuccessCallback.apply(h5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /** download end **/

    private static ProgressChangeDown mDownloadProgressChange = new ProgressChangeDown() {
        private Map<String, Boolean> mFirst = new HashMap<String, Boolean>();

        @Override
        public void onPreSuccess(String id, String thirdExterpriseId, String url, JSONObject h5) {
            //预加载成功后，开始下载
            File file = null;
            if(TextUtils.isEmpty(id)){
            	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                id = sdf.format(new Date());
                file = FileUtil.getFile(mContext, id, url);
                JsFileDown jsFileDown = new JsFileDown();
                jsFileDown.setId(id);
                jsFileDown.setUrl(file.getAbsolutePath());
                jsFileDown.setDateTime(id);
                jsFileDown.setStatus(JsFile.STATUS_ING);
                jsFileDown.setName(file.getName());
                jsFileDown.setH5(h5.toJSONString());
                try {
                    SujeDbHelper.getInstance(mContext).create(jsFileDown);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    JsFileDown jsFileDown = SujeDbHelper.getInstance(mContext).load(JsFileDown.class, id);
                    if(jsFileDown == null){
                        if(mDownFailureCallback != null){
                        	mDownFailureCallback.apply("there are not item in db ", h5);
                        }
                        return;
                    }else{
                        String filePath = Contants.FILE_DOWNLOAD_PATH + jsFileDown.getName();
                        try {
                        	filePath = java.net.URLDecoder.decode(filePath,"UTF-8");
                		} catch (UnsupportedEncodingException e) {
                			e.printStackTrace();
                		}
                        file = new File(filePath);
                        jsFileDown.setStatus(JsFile.STATUS_ING);
                        SujeDbHelper.getInstance(mContext).update(jsFileDown);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            Log.e("download", "download   " + file.length() + "||" + id + file.getAbsolutePath());
            if(!FileDownMngr.getInstance(mContext).isLoading(id)){
                FileDownMngr.getInstance(mContext).downLoadTask(id, url, file);
            }
            if(mDownSuccessCallback != null){
            	mDownSuccessCallback.apply(id, h5);
            }
        }

        @Override
        public void onPreFailure(String msg, JSONObject h5) {
            if(mDownFailureCallback != null){
            	mDownFailureCallback.apply(msg, h5);
            }
        }

        @Override
        public void onSuccess(String id) {
            try {
                JsFileDown jsFileDown = SujeDbHelper.getInstance(mContext).load(JsFileDown.class, id);
                if(jsFileDown != null){
                    jsFileDown.setStatus(JsFile.STATUS_SUCC);
                    SujeDbHelper.getInstance(mContext).update(jsFileDown);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProgressChange(String id, int bytesWritten, int totalSize) {
            Log.e("onProgressChange", id + "" + bytesWritten + "--" + totalSize + "--" + mFirst.containsKey(id));
            if(mProgressDownSuccessCallback != null){
            	mProgressDownSuccessCallback.apply(id, bytesWritten, totalSize);
            }
            if(!mFirst.containsKey(id)){
                mFirst.put(id, true);
                try {
                    JsFileDown jsFileDown = SujeDbHelper.getInstance(mContext).load(JsFileDown.class, id);
                    if(jsFileDown != null){
                        jsFileDown.setTotalSize(totalSize);
                        SujeDbHelper.getInstance(mContext).update(jsFileDown);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFailure(String id, String msg) {
            try {
            	JsFileDown jsFileDown = SujeDbHelper.getInstance(mContext).load(JsFileDown.class, id);
            	if(!msg.equals("network")){
            		if(jsFileDown != null){
                        jsFileDown.setStatus(JsFile.STATUS_FAIL);
                        SujeDbHelper.getInstance(mContext).update(jsFileDown);
                    }
            	}
                if(jsFileDown != null){
                    if(mDownFailureCallback != null){
                    	mDownFailureCallback.apply(msg, jsFileDown.getH5());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static ProgressChangeUp mUploadProgressChange = new ProgressChangeUp() {

        @Override
        public void onPreSuccess(String id, String thirdExterpriseId, String url, JSONObject h5) {
            //预上传成功后，开始上传
            //刷新上传地址后，开始上传
            JsFileUp jsFileUp = null;
            try {
                jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(jsFileUp == null){
                if(mUpFailureCallback != null){
                	mUpFailureCallback.apply("preUpload success but query form db is fail ", h5);
                }
                return;
            }
            Log.e("upload", "upload   " + jsFileUp.getTotalSize() + "||" + id + jsFileUp.getUrl());
            if(!FileUpMngr.getInstance(mContext).isLoading(id)){
                FileUpMngr.getInstance(mContext).upload(id, url, jsFileUp.getUrl(), h5);
                try {
                    if(jsFileUp != null){
                        jsFileUp.setStatus(JsFile.STATUS_ING);
                        SujeDbHelper.getInstance(mContext).update(jsFileUp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onPreFailure(String msg, JSONObject h5) {
            if(mUpFailureCallback != null){
            	mUpFailureCallback.apply(msg, h5);
            }
        }

        @Override
        public void onEverySuccess(final String id, String url, final JSONObject h5) {
            //1,先获取刷新地址
            //2,刷新上传地址接口
            JsFileUp jsFileUp = null;
            try {
                jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(jsFileUp == null){
                return;
            }

            String result = getRefreshUploadUrl(jsFileUp.getOwnerId(), jsFileUp.getServerFileId(), jsFileUp.getRefreshUrl());
            if(TextUtils.isEmpty(result)){
            	if(mUpFailureCallback != null){
            		mUpFailureCallback.apply("refreshUrl is empty", h5);
                }
            	return;
            }
            FileUpMngr.getInstance(mContext).stopTask(id);
            FileUpMngr.getInstance(mContext).refreshUpload(id, result, null, h5);
        }

        @Override
        public void onEveryFailure(String msg, JSONObject h5) {
            if(mUpFailureCallback != null){
            	mUpFailureCallback.apply(msg, h5);
            }
        }

        @Override
        public void onSuccess(String id) {
        	try {
                JsFileUp jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, id);
                if(jsFileUp != null){
                    jsFileUp.setStatus(JsFile.STATUS_SUCC);
                    SujeDbHelper.getInstance(mContext).update(jsFileUp);
                }
                File file = new File(jsFileUp.getUrl());
                if(mProgressUpSuccessCallback != null){
                	mProgressUpSuccessCallback.apply(id, file.length(), file.length());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onProgressChange(String id, int bytesWritten, int totalSize) {
            Log.e("onProgressChange", id + "" + bytesWritten + "--" + totalSize);
            if(mProgressUpSuccessCallback != null){
            	mProgressUpSuccessCallback.apply(id, bytesWritten - 1, totalSize);
            }
        }

        @Override
        public void onFailure(String id, String msg) {
            if(mUpFailureCallback != null){
            	mUpFailureCallback.apply(msg);
            }
            try {
                JsFileUp jsFileUp = SujeDbHelper.getInstance(mContext).load(JsFileUp.class, id);
                if(jsFileUp != null){
                    jsFileUp.setStatus(JsFile.STATUS_FAIL);
                    SujeDbHelper.getInstance(mContext).update(jsFileUp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

		@Override
		public void onUploadSuccess(String id, JSONObject h5) {
			if(mUpSuccessCallback != null){
            	mUpSuccessCallback.apply(id, h5);
            }
		}
    };

    private static JsCallback mGetNetworkSuccessCallback;
    public static void getNetwork(WebView webView, String success){
    	mContext = webView.getContext();
    	mGetNetworkSuccessCallback = new JsCallback(webView, success);
    	String network = NetworkUtil.getNetwork(mContext);
    	if(mGetNetworkSuccessCallback != null){
    		mGetNetworkSuccessCallback.apply(network);
    	}
    }
    
    private static JsCallback mUpdateStatusSuccess;
    public static void updateStatus(WebView webView, String fileId, String status, String type, String success){
    	mContext = webView.getContext();
    	mUpdateStatusSuccess = new JsCallback(webView, success);
    	String h5 = "";
    	try {
    		if(type.equals("1")){
    			JsFileDown jsFileDown = SujeDbHelper.getInstance(webView.getContext()).load(JsFileDown.class, fileId);
                if(jsFileDown != null){
                    jsFileDown.setStatus(Integer.parseInt(status));
                    SujeDbHelper.getInstance(webView.getContext()).update(jsFileDown);
                    h5 = jsFileDown.getH5();
                }
    		}else{
    			JsFileUp jsFileUp = SujeDbHelper.getInstance(webView.getContext()).load(JsFileUp.class, fileId);
                if(jsFileUp != null){
                	jsFileUp.setStatus(Integer.parseInt(status));
                    SujeDbHelper.getInstance(webView.getContext()).update(jsFileUp);
                    h5 = jsFileUp.getH5();
                }
    		}
            
            if(mUpdateStatusSuccess != null){
            	mUpdateStatusSuccess.apply(h5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static JsCallback mGetImageBase64SuccessCallback;
    private static JsCallback mGetImageBase64FailureCallback;
    public static void getImageBase64(WebView webView, String url, String id, String success, String error){
    	mContext = webView.getContext();
    	mGetImageBase64SuccessCallback = new JsCallback(webView, success);
    	mGetImageBase64FailureCallback = new JsCallback(webView, error);
    	Bitmap bitmap = null;
		try {
			bitmap = ImageUtil.compress(url);
			String base64 = "data:image/gif;base64," + ImageUtil.imgToBase64(bitmap);
			if(mGetImageBase64SuccessCallback != null){
				mGetImageBase64SuccessCallback.apply(base64, id);
	    	}
		} catch (Exception e) {
			e.printStackTrace();
			if(mGetImageBase64FailureCallback != null){
				mGetImageBase64FailureCallback.apply(e.getMessage(), id);
	    	}
		} finally {
			if (bitmap != null) {
				bitmap.recycle();
			}
		}
    }

}
