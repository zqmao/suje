/**
 * Summary: 异步回调页面JS函数管理对象
 * Version 1.0
 * Date: 13-11-26
 * Time: 下午7:55
 * Copyright: Copyright (c) 2013
 */

package com.suje.inject;

import java.lang.ref.WeakReference;

import android.util.Log;
import android.webkit.WebView;

public class JsCallback {
    private static final String CALLBACK_JS_FORMAT = "javascript:%s.callback(%d, %d %s);";
    private int mIndex;
    private boolean mCouldGoOn;
    private WeakReference<WebView> mWebViewRef;
    private int mIsPermanent;
    private String mInjectedName;

    public JsCallback (WebView view, String injectedName, int index) {
        mCouldGoOn = true;
        mWebViewRef = new WeakReference<WebView>(view);
        mInjectedName = injectedName;
        mIndex = index;
    }
    public JsCallback (WebView view, String injectedName) {
        mCouldGoOn = true;
        mWebViewRef = new WeakReference<WebView>(view);
        mInjectedName = injectedName;
    }

    public void apply (Object... args) {
        if (mWebViewRef.get() == null) {
            Log.e("JsCallback", "the WebView related to the JsCallback has been recycled");
            return;
        }
        if (!mCouldGoOn) {
            Log.e("JsCallback", "the JsCallback isn't permanent,cannot be called more than once");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args){
            if(arg == null){
                continue;
            }
            sb.append(",");
            boolean isStrArg = arg instanceof String;
            if (isStrArg) {
                sb.append("\"");
            }
            String result = String.valueOf(arg);
            if(isStrArg){
                result = result.replaceAll("\"", "'");
            }
            sb.append(result);
            if (isStrArg) {
                sb.append("\"");
            }
        }
        String params = "";
        if(sb.toString().startsWith(",")){
            params = sb.substring(1);
        }
//        String execJs = String.format(CALLBACK_JS_FORMAT, mInjectedName, mIndex, mIsPermanent, sb.toString());
        String execJs = "javascript:" + mInjectedName + "(" + params + ");";
        Log.d("JsCallBack", execJs);
        mWebViewRef.get().loadUrl(execJs);
//        mCouldGoOn = mIsPermanent > 0;
    }

    public void setPermanent (boolean value) {
        mIsPermanent = value ? 1 : 0;
    }
}
