package io.myse.updater;

import io.myse.common.BuildInfo;
import static io.myse.common.LOG.LOG;
import io.myse.common.Paths;
import io.myse.db.model.Config;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.compress.utils.IOUtils;

public class Updater implements Runnable {

	private static final String PRODUCT_NAME = "myse";
	private static final String URL_BASE = "http://update.myse.io/";

	private String getJarUrl(String version) {
		return URL_BASE + PRODUCT_NAME + "_" + version + ".jar";
	}

	private String getLastVersionFileUrl() {
		return URL_BASE + "version_" + Config.get(Config.PAR_UPDATE_CHANNEL, "stable", true) + "?version=" + BuildInfo.VERSION;
	}

	private String getHashUrl(String version) {
		return getJarUrl(version) + ".sha1sum";
	}

	public Updater() {
	}

	private String urlToString(String url) {
		try {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
				return reader.readLine();
			}
		} catch (Exception ex) {
			LOG.warn("Updater.urlToSTring( \"" + url + "\" )", ex);
			return null;
		}
	}

	private String getLastVersion() {
		return urlToString(getLastVersionFileUrl());
	}

	private String getHash(String version) {
		String v = urlToString(getHashUrl(version));
		if (v != null) {
			v = v.split(" ")[0];
		}
		return v;
	}

	public static boolean verifyChecksum(File file, String testChecksum) throws NoSuchAlgorithmException, IOException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] data = new byte[8192];
			int read = 0;
			while ((read = fis.read(data)) != -1) {
				sha1.update(data, 0, read);
			}
		}
		byte[] hashBytes = sha1.digest();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < hashBytes.length; i++) {
			sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		String fileHash = sb.toString();

		return fileHash.equals(testChecksum);
	}

	private boolean download(File target, String url, String hash) {
		File download = new File(target.getAbsolutePath() + ".download");
		try {
			try (InputStream is = new URL(url).openStream()) {
				LOG.info("Starting download !");
				try (OutputStream os = new FileOutputStream(download)) {
					IOUtils.copy(is, os);
				}
			}
			LOG.info("Finished download !");
			if (verifyChecksum(download, hash)) {
				LOG.info("Checksum OK !");
				boolean rename = download.renameTo(target);
				LOG.info("Renamed: " + rename);
				return true;
			} else {
				LOG.warn("Invalid checksum !");
			}
		} catch (Exception ex) {
			LOG.warn("download", ex);
		}
		return false;
	}

	@Override
	public void run() {
		LOG.debug("Checking current version...");
		
		if ( ! new File("/usr/share/myse").exists() ) {
			LOG.info("No update on Linux standard installation !");
			return;
		}

		boolean upgrade;

		String version = getLastVersion();

		LOG.debug("Current version  = " + BuildInfo.VERSION);
		LOG.debug("Required version = " + version);

		upgrade = new VersionComparator().compare(version, BuildInfo.VERSION) > 0;

		if (upgrade) {
			LOG.debug("Upgrade necessary !");
			File target = new File(Paths.getUpgradeDir().getAbsolutePath() + File.separator + version + ".jar");
			if (!target.exists()) {
				String hash = getHash(version);
				LOG.debug("hash = " + hash);
				if (download(target, getJarUrl(version), hash)) {
					LOG.debug("Download succeeded ! Saved to " + target);
				} else {
					LOG.debug("Problem during download !");
				}
			}

			if (target.exists()) {
				LOG.debug("Performing upgrade !");
				performUpgrade(target);
			}
		} else {
			LOG.debug("No upgrade necessary !");
			for (File f : Paths.getUpgradeDir().listFiles()) {
				LOG.info("Deleting old upgrade file \"{}\"", f.getName());
				f.delete();
			}
		}
	}

	private void performUpgrade(File target) {
		try {

			File me = Paths.myJarFile();

			// If we can't write this file,
			// we will do the installation in the alternative location
			if (!me.canWrite()) {
				me = Paths.alternativeJarLocation();
			}

			File javaExe = Paths.getJavaExe();

			ProcessBuilder pb = new ProcessBuilder(javaExe.getAbsolutePath(), "-jar", target.getAbsolutePath(), "--upgrade-to", me.getAbsolutePath());
			pb.directory(Paths.getUpgradeDir());
			LOG.info("Starting " + pb.command() + " ...");
			pb.start();
			System.exit(0);
		} catch (Exception ex) {
			LOG.warn("perfomUpgrade", ex);
		}
	}
}
