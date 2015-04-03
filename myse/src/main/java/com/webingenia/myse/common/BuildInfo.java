package com.webingenia.myse.common;

import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {

	public static final String BUILD_SVNREV, BUILD_ID, BUILD_TIMESTAMP, BUILD_RELEASE, BUILD_SIMPLIFIED_INFO;

	static {
		String buildSvnRev = "-1", buildTimestamp = "_", buildRelease = "dev", buildStable, buildId = "#";
		try {
			Properties properties = new Properties();
			try (InputStream ras = BuildInfo.class.getResourceAsStream("/version.properties")) {
				if (ras != null) {
					properties.load(ras);

					buildSvnRev = (String) properties.get("build_svnrev");
					buildId = (String) properties.get("build_id");
					buildTimestamp = (String) properties.get("build_timestamp");
					buildStable = (String) properties.getProperty("build_stable");
					if (buildStable != null && !buildStable.startsWith("$")) {
						if (buildStable.equals("true")) {
							buildRelease = "prod";
						} else {
							buildRelease = "test";
						}
					} else {
						buildRelease = (String) properties.getProperty("build_release");
					}
				}
			}
		} catch (Exception ex) {
		}

		if ("testing".equals(buildRelease)) {
			buildRelease = "test";
		}

		if (buildRelease.startsWith("${")) {
			buildRelease = "dev";
		}

		BUILD_SVNREV = buildSvnRev;
		BUILD_ID = buildId;
		BUILD_TIMESTAMP = buildTimestamp;
		BUILD_RELEASE = buildRelease;
		BUILD_SIMPLIFIED_INFO = BUILD_RELEASE + " v" + BUILD_SVNREV + " (" + BUILD_TIMESTAMP + ")";
	}

	public static String getBuildInfo() {
		return BUILD_SIMPLIFIED_INFO;
	}

	public static String getBuildRelease() {
		return BUILD_RELEASE;
	}
}
