package com.suje.db;

import com.alibaba.fastjson.annotation.JSONType;

/**
 * Created by zqmao on 2017/8/6.
 */

@JSONType(ignores = {"partId", "serverFileId", "uploadUrl", "ownerId", "refreshUrl"})
public class JsFileUp extends JsFile {
    private int partId;//上传文件的片数;
    private String serverFileId;//服务器fileId;
    private String uploadUrl;//预上传返回的uploadUrl;
    private String ownerId;//refreshUrl中的部分;
    private String refreshUrl;//refreshUrl包含需要替换的部分，临时;

    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    public String getServerFileId() {
        return serverFileId;
    }

    public void setServerFileId(String serverFileId) {
        this.serverFileId = serverFileId;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getRefreshUrl() {
        return refreshUrl;
    }

    public void setRefreshUrl(String refreshUrl) {
        this.refreshUrl = refreshUrl;
    }
}
