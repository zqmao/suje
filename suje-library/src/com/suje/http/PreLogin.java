package com.suje.http;


public interface PreLogin {
	public boolean isLoginUrl(String url);
	public void tryLoginWithSerial();
}
