package io.myse.common;

import java.io.File;
import java.net.URLDecoder;

import static io.myse.common.LOG.LOG;

public class Paths {

	public static File getDesktopDir() {
		File file = new File(System.getProperty("user.home"));

		String pathAttempts[] = {"Bureau", "Desktop"};

		for (String n : pathAttempts) {
			File f = new File(file.getAbsolutePath() + "/" + n);
			if (f.exists()) {
				return f;
			}
		}

		return file;
	}

	public static File getAppDir() {
		File homeDir = new File(System.getProperty("user.home"));
		File appDir = new File(homeDir, ".myse");

		//File appDir = new File("data");
		if (!appDir.exists()) {
			appDir.mkdir();
		}

		return appDir;
	}

	public static File getScreenshotsDir() {
		File file = new File(getAppDir(), "screenshots");
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}

	public static File getLogsDir() {
		File file = new File(getAppDir(), "logs");
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}

	public static File getESDir() {
		File file = new File(getAppDir(), "es");
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}

	public static File getH2Dir() {
		File file = new File(getAppDir(), "h2");
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}

	public static File getCacheDir() {
		File file = new File(getAppDir(), "cache");
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}

	public static File getUpgradeDir() {
		File upgradeDir = new File(getAppDir(), "upgrade");
		if (!upgradeDir.exists()) {
			upgradeDir.mkdir();
		}
		return upgradeDir;
	}

	public static File getCacheHashFile(String hash, boolean createDir) {
		File dir = new File(getCacheDir().getAbsolutePath() + File.separator + hash.substring(0, 1) + File.separator + hash.substring(1, 3));

		if (createDir && !dir.exists()) {
			dir.mkdirs();
		}

		File file = new File(dir, hash);

		return file;
	}

	public static File getJavaExe() {
		File javaHome = new File(System.getProperty("java.home"));
		File javaBin = new File(javaHome, "bin");
		File javaExe = new File(javaBin, "java.exe");
		if (!javaExe.exists()) {
			javaExe = new File(javaBin, "java");
		}
		if (!javaExe.exists()) {
			LOG.warn("Could not find java executable !");
			return null;
		}
		return javaExe;
	}

	public static File myJarFile() {
		try {
			return new File(URLDecoder.decode(Paths.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "utf8"));
		} catch (Exception ex) {
			LOG.error("myJarFile", ex);
			return null;
		}
	}

	public static File alternativeJarLocation() {
		return new File(Paths.getAppDir(), "myse.jar");
	}
}
