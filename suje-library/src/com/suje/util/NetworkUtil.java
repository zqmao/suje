package com.suje.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

	public static String getNetwork(Context context) {
		String type = "";
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null) {
			type = "null";
		} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			type = "wifi";
		} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			type = "gprs";
		}
		return type;
	}

	
}
