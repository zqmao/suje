package com.suje.manager;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by zqmao on 2017/8/19.
 */

public interface ProgressChangeDown{
    /**
     * 预处理完成
     * @param id
     * @param url
     * @param h5
     */
    void onPreSuccess(String id, String url, JSONObject h5);

    /**
     * 预处理失败
     * @param msg
     * @param h5
     */
    void onPreFailure(String msg, JSONObject h5);
    /**
     * 进度变化
     * @param id
     * @param bytesWritten
     * @param totalSize
     */
    void onProgressChange(String id, int bytesWritten, int totalSize);

    /**
     * 完成
     * @param id
     */
    void onSuccess(String id);

    /**
     * 失败
     * @param id
     */
    void onFailure(String id, String msg);
}
