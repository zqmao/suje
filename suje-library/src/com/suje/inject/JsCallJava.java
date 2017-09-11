package com.suje.inject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JsCallJava {
    private final static String TAG = "JsCallJava";
    private final static String RETURN_RESULT_FORMAT = "{\"code\": %d, \"result\": %s}";
    private static HashMap<String, Method> mMethodsMap;
    private static String mPreloadInterfaceJS;
    private static HashMap<String, Class> mClasssMap = new HashMap<String, Class>();
    private static JsCallJava mInstance;
    
    public static JsCallJava getInstance(){
    	if(mInstance == null){
    		mInstance = new JsCallJava();
    	}
    	return mInstance;
    }
    
    public JsCallJava(){
    	
    }
    
    public void putInstance(String injectedName, Class injectedCls){
    	if (!TextUtils.isEmpty(injectedName)) {
    		try {
	    		mClasssMap.put(injectedName, injectedCls);
	    		setPreloadInterfaceJS();
    		} catch(Exception e){
                Log.e(TAG, "init js error:" + e.getMessage());
            }
        }
    }

    private static String genJavaMethodSign (Method method) {
        String sign = method.getName();
        Class[] argsTypes = method.getParameterTypes();
        int len = argsTypes.length;
        if (len < 1 || argsTypes[0] != WebView.class) {
            Log.w(TAG, "method(" + sign + ") must use webview to be first parameter, will be pass");
            return null;
        }
        for (int k = 1; k < len; k++) {
            Class cls = argsTypes[k];
            if (cls == String.class) {
                sign += "_S";
            } else if (cls == int.class ||
                cls == long.class ||
                cls == float.class ||
                cls == double.class) {
                sign += "_N";
            } else if (cls == boolean.class) {
                sign += "_B";
            } else if (cls == JSONObject.class) {
                sign += "_O";
            } else if (cls == JsCallback.class) {
                sign += "_F";
            } else {
                sign += "_P";
            }
        }
        return sign;
    }

    public String getPreloadInterfaceJS () {
        return mPreloadInterfaceJS;
    }
    
    private static void setPreloadInterfaceJS () {
    	mMethodsMap = new HashMap<String, Method>();
    	Set<String> keySet = mClasssMap.keySet();
    	StringBuilder sb = new StringBuilder("javascript:(function(b){console.log(\"");
        sb.append("test");
        sb.append(" initialization begin\");");
    	for(String key : keySet){
    		Class<?> injectedCls = mClasssMap.get(key);
            //获取自身声明的所有方法（包括public private protected）， getMethods会获得所有继承与非继承的方法
            Method[] methods = injectedCls.getDeclaredMethods();
            
            sb.append("var "+key);
            sb.append("={queue:[],callback:function(){var d=Array.prototype.slice.call(arguments,0);var c=d.shift();var e=d.shift();this.queue[c].apply(this,d);if(!e){}}};");
            for (Method method : methods) {
                String sign;
                if (method.getModifiers() != (Modifier.PUBLIC | Modifier.STATIC) || (sign = genJavaMethodSign(method)) == null) {
                    continue;
                }
                mMethodsMap.put(sign, method);
                sb.append(String.format(key+".%s=", method.getName()));
            }

            sb.append("function(){var f=Array.prototype.slice.call(arguments,0);if(f.length<1){throw\"");
            sb.append(key);
            sb.append(" call error, message:miss method name\"}var e=[];for(var h=1;h<f.length;h++){var c=f[h];var j=typeof c;e[e.length]=j;if(j==\"function\"){var d="+key+".queue.length;"+key+".queue[d]=c;f[h]=d}}");
            sb.append("var g=JSON.parse(prompt(JSON.stringify({action:\""+key+"\",method:f.shift(),types:e,args:f})));if(g.code!=200){throw\"");
            sb.append(key);
            sb.append(" call error, code:\"+g.code+\", message:\"+g.result}return g.result};Object.getOwnPropertyNames("+key+").forEach(function(d){var c="+key+"[d];if(typeof c===\"function\"&&d!==\"callback\"){"+key+"[d]=function(){return c.apply("+key+",[d].concat(Array.prototype.slice.call(arguments,0)))}}});");
            sb.append("b."+key+"="+key+";");
    	}
    	sb.append("console.log(\" initialization end\")})(window);");
    	mPreloadInterfaceJS = sb.toString();
    }

    public String call(WebView webView, String jsonStr) {
        if (!TextUtils.isEmpty(jsonStr)) {
            try {
                JSONObject callJson = JSON.parseObject(jsonStr);//new JSONObject(jsonStr);
                String action = callJson.getString("action");
                String methodName = callJson.getString("method");
                JSONArray argsTypes = callJson.getJSONArray("types");
                JSONArray argsVals = callJson.getJSONArray("args");
                String sign = methodName;
                int len = argsTypes.size();
                Object[] values = new Object[len + 1];
                int numIndex = 0;
                String currType;

                values[0] = webView;

                for (int k = 0; k < len; k++) {
                    currType = argsTypes.getString(k);
                    if ("string".equals(currType)) {
                        sign += "_S";
                        values[k + 1] = argsVals.getString(k);
                    } else if ("number".equals(currType)) {
                        sign += "_N";
                        numIndex = numIndex * 10 + k + 1;
                    } else if ("boolean".equals(currType)) {
                        sign += "_B";
                        values[k + 1] = argsVals.getBoolean(k);
                    } else if ("object".equals(currType)) {
                        sign += "_O";
                        values[k + 1] = argsVals.getJSONObject(k);
                    } else if ("function".equals(currType)) {
                        sign += "_F";
                        values[k + 1] = new JsCallback(webView, action, argsVals.getIntValue(k));
                    } else {
                        sign += "_P";
                    }
                }

                Method currMethod = mMethodsMap.get(sign);

                // 方法匹配失败
                if (currMethod == null) {
                    return getReturn(jsonStr, 500, "not found method(" + sign + ") with valid parameters");
                }
                // 数字类型细分匹配
                if (numIndex > 0) {
                    Class[] methodTypes = currMethod.getParameterTypes();
                    int currIndex;
                    Class currCls;
                    while (numIndex > 0) {
                        currIndex = numIndex - numIndex / 10 * 10;
                        currCls = methodTypes[currIndex];
                        if (currCls == int.class) {
                            values[currIndex] = argsVals.getInteger(currIndex - 1);
                        } else if (currCls == long.class) {
                            //WARN: argsJson.getLong(k + defValue) will return a bigger incorrect number
                            values[currIndex] = Long.parseLong(argsVals.getString(currIndex - 1));
                        } else {
                            values[currIndex] = argsVals.getDouble(currIndex - 1);
                        }
                        numIndex /= 10;
                    }
                }

                return getReturn(jsonStr, 200, currMethod.invoke(null, values));
            } catch (Exception e) {
                //优先返回详细的错误信息
                if (e.getCause() != null) {
                    return getReturn(jsonStr, 500, "method execute error:" + e.getCause().getMessage());
                }
                return getReturn(jsonStr, 500, "method execute error:" + e.getMessage());
            }
        } else {
            return getReturn(jsonStr, 500, "call data empty");
        }
    }

    private String getReturn (String reqJson, int stateCode, Object result) {
        String insertRes;
        if (result == null) {
            insertRes = "null";
        } else if (result instanceof String) {
            result = ((String) result).replace("\"", "\\\"");
            insertRes = "\"" + result + "\"";
        } else if (!(result instanceof Integer)
                && !(result instanceof Long)
                && !(result instanceof Boolean)
                && !(result instanceof Float)
                && !(result instanceof Double)
                && !(result instanceof JSONObject)) {    // 非数字或者非字符串的构造对象类型都要序列化后再拼接
            insertRes = JSON.toJSONString(result);
        } else {  //数字直接转化
            insertRes = String.valueOf(result);
        }
        String resStr = String.format(RETURN_RESULT_FORMAT, stateCode, insertRes);
        Log.d(TAG, " call json: " + reqJson + " result:" + resStr);
        return resStr;
    }
}