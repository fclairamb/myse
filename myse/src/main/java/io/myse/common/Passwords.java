package io.myse.common;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import jcifs.Config;

public class Passwords {

	private static String hash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
		int iterations = 2000;
		char[] chars = password.toCharArray();
		byte[] bsalt = getSalt().getBytes();

		PBEKeySpec spec = new PBEKeySpec(chars, bsalt, iterations, 64 * 8);
		SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		byte[] hash = skf.generateSecret(spec).getEncoded();
		return "pbdkf2:" + iterations + ":" + toHex(bsalt) + ":" + toHex(hash);
	}

	private static final String CONFIG_SALT = "passwords_salt";

	private static final String SYMBOLS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-.,0123456789";

	private static String salt;

	private static String getSalt() throws NoSuchAlgorithmException {
		if (salt == null) {
			salt = Config.getProperty(CONFIG_SALT, salt);
			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
			StringBuilder tmp = new StringBuilder();
			for (int i = 0; i < 32; i++) {
				tmp.append(SYMBOLS.charAt(sr.nextInt() % SYMBOLS.length()));
			}
			salt = tmp.toString();
			Config.setProperty(CONFIG_SALT, salt);
		}
		return salt;
	}

	private static String toHex(byte[] array) throws NoSuchAlgorithmException {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		} else {
			return hex;
		}
	}
}
