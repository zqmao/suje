package com.suje.util;

import android.annotation.SuppressLint;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MdUtil {
	private static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	private static String MD5_SK = "zqmao";
	public static String MD5(String str) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

		char[] charArray = str.toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++) {
			byteArray[i] = (byte) charArray[i];
		}
		byte[] md5Bytes = md5.digest(byteArray);

		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	public static String MD5(byte[] byts)  {
		return MD5(byts, 0, byts.length);
	}
	public static String MD5(byte[] byts, int start, int length)  {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		md.update(byts, start, length);
		byte[] digest = md.digest();
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < digest.length; i++) {
			int val = ((int) digest[i]) & 0xff;
			if (val < 16) {
				hexValue.append("0");
			}
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	public static MD5Hander getMD5Hander(){
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return new MD5Hander(md);
	}

	static class MD5Hander{

		MessageDigest digest;


		public MD5Hander(MessageDigest digest) {
			super();
			this.digest = digest;
		}
		public void append(byte[] byts){
			digest.update(byts, 0, byts.length);
		}
		public void append(byte[] byts, int start, int length){
			digest.update(byts, 0, length);
		}
		public String getMD5String(){
			byte[] byts = digest.digest();
			StringBuffer hexValue = new StringBuffer();
			for (int i = 0; i < byts.length; i++) {
				int val = ((int) byts[i]) & 0xff;
				if (val < 16) {
					hexValue.append("0");
				}
				hexValue.append(Integer.toHexString(val));
			}
			return hexValue.toString();
		}
	}

	public static String hexify(byte bytes[]) {

		StringBuffer buf = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; ++i) {
			buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			buf.append(hexDigits[bytes[i] & 0x0f]);
		}
		return buf.toString();
	}
}
