package com.suje.db;

import java.io.Serializable;

/**
 * Created by zqmao on 2017/8/18.
 */

public class JsFile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String url;
    private String name;
    private long totalSize;
    private long size;
    private String dateTime;
    private String h5;
    private int status;//状态 1:上传下载中;2:上传下载失败;3:上传下载成功;4:已暂停;
    private String thirdExterpriseId;//个人所在的企业id

    public static final int STATUS_ING = 1;
    public static final int STATUS_FAIL = 2;
    public static final int STATUS_SUCC = 3;
    public static final int STATUS_PAUSE = 4;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public long getTotalSize() {
        return totalSize;
    }
    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public String getDateTime() {
        return dateTime;
    }
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
    public String getH5() {
        return h5;
    }
    public void setH5(String h5) {
        this.h5 = h5;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

	public String getThirdExterpriseId() {
		return thirdExterpriseId;
	}

	public void setThirdExterpriseId(String thirdExterpriseId) {
		this.thirdExterpriseId = thirdExterpriseId;
	}
}
