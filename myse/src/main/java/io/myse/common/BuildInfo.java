package io.myse.common;

import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {

	public static final String NUMBER, DATE, RELEASE, SIMPLIFIED_INFO, GIT_COMMIT, GIT_NB, VERSION;

	static {
		String buildGitCommit = null, buildGitNb = null, buildDate = "XXXX-XX-XX_XX-XX-XX", buildRelease = "dev", buildNumber = "#", buildVersion = "dev";
		try {
			Properties properties = new Properties();
			try (InputStream ras = BuildInfo.class.getResourceAsStream("/myse_build.properties")) {
				if (ras != null) {
					properties.load(ras);
					buildVersion = (String) properties.getProperty("version");
					buildNumber = (String) properties.get("number");
					buildGitCommit = (String) properties.getProperty("git_commit");
					buildGitNb = (String) properties.getProperty("git_commit_count");

					buildDate = (String) properties.get("date");
					buildRelease = (String) properties.getProperty("release");
				}
			}
		} catch (Exception ex) {
		}

		if ("testing".equals(buildRelease)) {
			buildRelease = "test";
		}

		if (buildRelease == null || buildRelease.startsWith("$")) {
			buildRelease = "dev";
		}

		NUMBER = buildNumber;
		GIT_COMMIT = buildGitCommit;
		GIT_NB = buildGitNb;
		DATE = buildDate;
		RELEASE = buildRelease;
		VERSION = buildVersion;

		String info = RELEASE;

		if (NUMBER != null) {
			info += ", b:" + NUMBER;
		}

		if (GIT_NB != null) {
			info += ", c:" + GIT_NB;
		}

		if (GIT_COMMIT != null) {
			info += ", h:" + GIT_COMMIT.substring(0, 5);
		}

		info += ", d:" + DATE;
		SIMPLIFIED_INFO = info;
	}

	public static String getBuildRelease() {
		return RELEASE;
	}
}
