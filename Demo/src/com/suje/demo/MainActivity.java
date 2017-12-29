package com.suje.demo;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSONObject;
import com.suje.inject.InjectedChromeClient;
import com.suje.manager.ProgressChangeDown;

/**
 * 本页面为webview页面，根据不同的地址，展示不同的测试页面
 * 调用suje的sdk，只需要给webview设置setWebChromeClient
 * 结合自己项目中的WebChromeClient的话，需要继承sdk中的InjectedChromeClient，然后重写
 * onProgressChanged onJsPrompt要是需要重写的话，需要执行super.onJsPrompt和super.onProgressChanged
 * 
 * 不能忽视的是主配置文件中的权限设置
 * 混淆配置：com.suje.*不混淆
 * @author zqmao
 *
 */
@SuppressLint("SetJavaScriptEnabled") 
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	this.requestWindowFeature(Window.FEATURE_NO_TITLE); 
        super.onCreate(savedInstanceState);
        WebView wv = new WebView(this);
        setContentView(wv);
        WebSettings ws = wv.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        //sdk引入开始
        wv.setWebChromeClient(new InjectedChromeClient());
        //sdk引入结束
        
        //兼容大部分HTTPS
        wv.setWebViewClient(new WebViewClient(){
        	@Override
        	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        		handler.proceed();
        	}
        });
        
        String url = getIntent().getStringExtra("url");
        url="https://weixin.armjs.com/demo/worker.html?token=Onebox/530108BDEA915309B1C5F295404629268F8EABB44B76293F608E9E9C&ownerId=48&thirdExterpriseId=500310&phoneNum=13815885817&userId=33&alias=13815885817&shareSwitch=1&linkSwitch=1";
//        url="file:///android_asset/upload/list.html?token=Onebox/BA84668F6CE69F6F9AFBA8BDD5DE41E30112DCA85A6897A0F2168696&ownerId=48&thirdExterpriseId=500310&phoneNum=13275898746&userId=35&alias=赵紫尧";
        wv.loadUrl(url);
        
//        TextView tv = new TextView(this);
//        tv.setPadding(100, 100, 100, 100);
//        tv.setText("hello");
//        setContentView(tv);
//        tv.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				for(int i = 0; i < 40; i++){
//					String path = Environment.getExternalStorageDirectory().getPath() + "/Isuje/";
//					File file = new File(path + i + ".apk");
//					FileDownMngr.getInstance(getApplicationContext()).downLoadTask("FileDownMngr" + i, "http://sqdd.myapp.com/myapp/qqteam/tim/down/tim.apk", file);
//				}
//			}
//		});
//        FileDownMngr.getInstance(this).addListenner(mDownloadProgressChange);
    }
    
    private static ProgressChangeDown mDownloadProgressChange = new ProgressChangeDown() {
        private Map<String, Boolean> mFirst = new HashMap<String, Boolean>();

        @Override
        public void onPreSuccess(String id, String thirdExterpriseId, String url, JSONObject h5) {
        }

        @Override
        public void onPreFailure(String msg, JSONObject h5) {
        }

        @Override
        public void onSuccess(String id) {
        }

        @Override
        public void onProgressChange(String id, int bytesWritten, int totalSize) {
        }

        @Override
        public void onFailure(String id, String msg) {
        	Log.e("downloadZqmao", "download   ?" + id + "--" + msg);
        }
    };
    
}
