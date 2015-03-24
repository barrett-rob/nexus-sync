package org.nexus.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class GlobalState {

	protected final File ivyCacheDir;
	protected final File ivySettingsFile;
	protected final Set<Dependency> inputDependencies;
	protected final List<String> versions = new ArrayList<String>();
	protected final List<Pattern> excludes = new ArrayList<Pattern>();
	protected final List<Pattern> versionPatterns = new ArrayList<Pattern>();

	public GlobalState(Properties ps, Set<Dependency> ds) {
		this.ivyCacheDir = new File(ps.getProperty("ivy.cache"));
		if (!this.ivyCacheDir.exists() || !this.ivyCacheDir.isDirectory()) {
			throw new RuntimeException(this.ivyCacheDir
					+ " does not exist or is not a directory");
		}
		this.ivySettingsFile = new File(ps.getProperty("ivy.settings"));
		if (!this.ivySettingsFile.exists() || !this.ivySettingsFile.isFile()) {
			throw new RuntimeException(this.ivySettingsFile
					+ " does not exist or is not a file");
		}
		for (Object o : ps.keySet()) {
			String s = (String) o;
			if (s.startsWith("version")) {
				String version = ps.getProperty(s);
				Pattern p = Pattern.compile(getRegexFromIvyVersion(version));
				versions.add(version);
				versionPatterns.add(p);
			}
			if (s.startsWith("exclude")) {
				String exclude = ps.getProperty(s);
				Pattern p = Pattern.compile(exclude);
				excludes.add(p);
			}
		}
		this.inputDependencies = ds;
	}

	protected String getRegexFromIvyVersion(String s) {
		s = s.replaceAll("\\.", Matcher.quoteReplacement("\\."));
		s = s.replaceAll("\\+$", ".*");
		return s;
	}

	protected String getVersionString(String rev) {
		int i = 0;
		for (Pattern p : versionPatterns) {
			if (p.matcher(rev).matches()) {
				return versions.get(i);
			}
			i++;
		}
		return null;
	}

}
