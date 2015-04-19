package com.webingenia.myse.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.webingenia.myse.common.LOG.LOG;

public class Hashing {

	private static String byteArrayToHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	public static String toSHA1(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			return byteArrayToHexString(md.digest(str.getBytes()));
		} catch (NoSuchAlgorithmException ex) {
			LOG.error("sha1", ex);
			return null;
		}
	}
}
