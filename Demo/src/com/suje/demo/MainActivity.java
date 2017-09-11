package com.suje.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.suje.inject.InjectedChromeClient;
import com.suje.util.Contants;

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
        url="http://115.28.1.196/test//worker.html?token=Onebox/FBE5C4AD9F2D0477124E7DE2231CEFC4269667A8432280147AA2AFEF&ownerId=54&thirdExterpriseId=123123";
        url="file:///android_asset/download/download.html";
//        wv.loadUrl(url);
        String path = Contants.FILE_DOWNLOAD_PATH + "zq.exe";
        String downUrl = "http://sw.bos.baidu.com/sw-search-sp/software/1f23f7071f7c1/QQ_8.9.4.21603_setup.exe";
//        FileDownMngr.getInstance(this).downLoadTask("hello", downUrl, new File(path));
//        openUrl(this, url);
    }
    
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    	String url="http://115.28.1.196/test//worker.html?token=Onebox/FBE5C4AD9F2D0477124E7DE2231CEFC4269667A8432280147AA2AFEF&ownerId=54&thirdExterpriseId=123123";
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    
    public void openUrl(Activity kActivity, String url){  
        String[] browser = { "com.tencent.mtt", "com.UCMobile", "com.uc.browser", "com.oupeng.browser", 
        		"com.oupeng.mini.android", "com.android.browser", "com.sec.android.app.sbrowser" };  
  
        Intent intent = null;  
        for (String br : browser) {  
            if (isInstall(kActivity, br)) {  
            	
                String clsName = null;  
                try {  
                    PackageManager pm = kActivity.getApplicationContext().getPackageManager();  
                    Intent intent1 = pm.getLaunchIntentForPackage(br);  
                    ComponentName act = intent1.resolveActivity(pm);  
                    clsName = act.getClassName();  
                } catch (Exception e) {  
                }  
                if (clsName == null) {  
                    break;  
                }  
                Log.e("zqmao", br + " is install ：" + clsName);
//                intent = new Intent();  
//                intent.setAction("android.intent.action.VIEW");  
//                Uri content_url = Uri.parse(url);  
//                intent.setData(content_url);  
//                intent.setClassName(br, clsName);  
//                break;  
            }else{
            	Log.e("zqmao", br + " is not install");
            }
        }  
        if (intent == null) {  
            intent = new Intent();  
            intent.setAction("android.intent.action.VIEW");  
            Uri content_url = Uri.parse(url);  
            intent.setData(content_url);  
        }  
//        kActivity.startActivity(intent);  
    } 
    
    private static boolean isInstall(Activity activity, String url){
    	PackageInfo packageInfo;
		try {
			packageInfo = activity.getPackageManager().getPackageInfo(url, 0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		if (packageInfo == null) {
			return false;
		}else {
			return true;
		}
    }
}
