package com.webingenia.myse.updater;

import com.webingenia.myse.Main;
import com.webingenia.myse.common.Files;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.common.Paths;
import com.webingenia.myse.db.model.Config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.elasticsearch.common.Strings;

public class Upgrader implements Runnable {

	private final File target;

	public Upgrader(File target) {
		this.target = target;
	}

	@Override
	public void run() {
		try {
			LOG.info("I will upgrade myself to " + target);
			File me = Paths.myJarFile();
			Thread.sleep(1000);
			Files.copy(me, target);
			LOG.info("Done! Starting the app !");

			File javaExe = Paths.getJavaExe();

			Main.stop();

			ProcessBuilder pb = new ProcessBuilder(javaExe.getAbsolutePath(), "-jar", target.getAbsolutePath(), "--upgraded");
			pb.directory(Paths.getUpgradeDir());
			LOG.info("Starting " + Strings.collectionToDelimitedString(pb.command(), " ", "\"", "\"") + " ...");
			pb.start();
			System.exit(0);
		} catch (Exception ex) {
			LOG.error("Upgrader.run", ex);
		}
	}

	public static void main(String[] args) throws IOException {
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			switch (arg) {
				case "--upgrade-to":
					new Upgrader(new File(args[++i])).run();
					break;
				case "--release":
					Config.set("release", args[++i]);
					break;
			}
		}

		{
			File f = new File(Paths.getAppDir(), "release.txt");
			if (f.exists()) {
				try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
					String release = reader.readLine().trim();
					if (release.length() >= 3) {
						Config.set(Config.PAR_UPDATE_CHANNEL, release);
					}
				}
			}
		}

		{
			File javaExe = Paths.getJavaExe();
			File myJarFile = Paths.myJarFile();
			File alternative = Paths.alternativeJarLocation();
			//LOG.debug("         My JAR: " + myJarFile);
			//LOG.debug("Alternative JAR: " + alternative);
			if (alternative.exists() && javaExe.exists() && !alternative.getAbsolutePath().equals(myJarFile.getAbsolutePath())) {
				LOG.info("We have an alternative app. Starting it right now!");
				ProcessBuilder pb = new ProcessBuilder(javaExe.getAbsolutePath(), "-jar", alternative.getAbsolutePath());
				//LOG.info("Starting process ", pb.command());
				pb.start();
				System.exit(0);
			}
		}
	}
}
