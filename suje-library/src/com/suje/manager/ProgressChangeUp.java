package com.suje.manager;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by zqmao on 2017/8/19.
 */

public interface ProgressChangeUp extends ProgressChangeDown{

    /**
     * 初始化完成
     * @param id
     * @param url
     * @param h5
     */
    void onEverySuccess(String id, String url, JSONObject h5);

    /**
     * 初始化失败
     * @param msg
     * @param h5
     */
    void onEveryFailure(String msg, JSONObject h5);
    
    /**
     * 初始化失败
     * @param msg
     * @param h5
     */
    void onUploadSuccess(String msg, JSONObject h5);
}
