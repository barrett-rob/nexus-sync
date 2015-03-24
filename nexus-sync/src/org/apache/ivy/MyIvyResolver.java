package org.apache.ivy;

import java.io.File;

import org.apache.ivy.util.cli.CommandLineParser;

public class MyIvyResolver {

	final File ivyXml, ivySettingsFile, ivyCacheDir;

	public MyIvyResolver(File ivyXml, File ivySettingsFile, File ivyCacheDir) {
		this.ivyXml = ivyXml;
		this.ivySettingsFile = ivySettingsFile;
		this.ivyCacheDir = ivyCacheDir;
	}

	public void resolve() {
		CommandLineParser parser = Main.getParser();
		try {
			System.setProperty("ivy.default.resolver", "all");
			Main.run(
					parser,
					new String[] { "-settings", ivySettingsFile.getPath(),
							"-cache", ivyCacheDir.getPath(), "-ivy",
							ivyXml.getPath() });
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			System.getProperties().remove("ivy.default.resolver");
		}
	}
}
