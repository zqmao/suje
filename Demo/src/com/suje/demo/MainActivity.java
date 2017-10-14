package com.suje.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.suje.inject.InjectedChromeClient;

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
//        url="http://115.28.1.196/test//worker.html?token=Onebox/FBE5C4AD9F2D0477124E7DE2231CEFC4269667A8432280147AA2AFEF&ownerId=54&thirdExterpriseId=123123";
        url="file:///android_asset/upload/list.html?token=Onebox/BA84668F6CE69F6F9AFBA8BDD5DE41E30112DCA85A6897A0F2168696&ownerId=48&thirdExterpriseId=500310&phoneNum=13275898746&userId=35&alias=赵紫尧";
        wv.loadUrl(url);
    }
    
}
