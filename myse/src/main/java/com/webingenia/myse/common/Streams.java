/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webingenia.myse.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author florent
 */
public class Streams {

	public static long copy(InputStream in, OutputStream out, long length) throws IOException {
		byte[] b = new byte[8192];

		int read;
		while ((read = in.read(b)) != -1 && length > 0L) {
			if ((long) read > length) {
				read = (int) length;
			}

			length -= (long) read;
			out.write(b, 0, read);
		}

		return length;
	}
}
