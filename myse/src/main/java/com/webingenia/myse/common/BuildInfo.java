package com.webingenia.myse.common;

import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {

	public static final String BUILD_NUMBER, BUILD_DATE, BUILD_RELEASE, BUILD_SIMPLIFIED_INFO, BUILD_GIT_COMMIT, BUILD_GIT_NB;

	static {
		String buildGitCommit = null, buildGitNb = null, buildDate = "XXXX-XX-XX_XX-XX-XX", buildRelease = "dev", buildNumber = "#";
		try {
			Properties properties = new Properties();
			try (InputStream ras = BuildInfo.class.getResourceAsStream("/myse_build.properties")) {
				if (ras != null) {
					properties.load(ras);
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

		if (buildRelease == null) {
			buildRelease = "dev";
		}

		BUILD_NUMBER = buildNumber;
		BUILD_GIT_COMMIT = buildGitCommit;
		BUILD_GIT_NB = buildGitNb;
		BUILD_DATE = buildDate;
		BUILD_RELEASE = buildRelease;
		
		String info = BUILD_RELEASE;

		if (BUILD_NUMBER != null) {
			info += " b" + BUILD_NUMBER;
		}

		if (BUILD_GIT_NB != null) {
			info += " c" + BUILD_GIT_COMMIT;
		}

		if (BUILD_GIT_COMMIT != null) {
			info += " h" + BUILD_GIT_COMMIT.substring(0, 5);
		}

		info += " (" + BUILD_DATE + ")";
		BUILD_SIMPLIFIED_INFO = info;
	}

	public static String getBuildInfo() {
		return BUILD_SIMPLIFIED_INFO;
	}

	public static String getBuildRelease() {
		return BUILD_RELEASE;
	}
}
