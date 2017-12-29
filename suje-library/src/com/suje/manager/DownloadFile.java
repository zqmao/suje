package com.suje.manager;

import java.io.File;

public class DownloadFile {
	private String id;
	private String url;
	private File file;
	
	public DownloadFile(String id, String url, File file) {
		super();
		this.id = id;
		this.url = url;
		this.file = file;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
