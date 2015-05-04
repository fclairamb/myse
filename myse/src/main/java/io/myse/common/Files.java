package io.myse.common;

import static io.myse.common.LOG.LOG;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class Files {

	public static boolean copy(File src, File dst) {
		try {
			try (InputStream is = new FileInputStream(src)) {
				try (OutputStream os = new FileOutputStream(dst)) {
					IOUtils.copy(is, os);
				}
			}
			return true;
		} catch (Exception ex) {
			LOG.warn("copy", ex);
		}
		return false;
	}

	public static boolean renameToFailsafe(File src, File dst) {
		boolean ok = copy(src, dst);
		if (ok) {
			src.delete();
		}
		return ok;
	}
}
