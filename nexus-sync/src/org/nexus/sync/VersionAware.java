package org.nexus.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class VersionAware {

	protected final Set<Dependency> inputDependencies;
	protected final List<Pattern> versions = new ArrayList<Pattern>();

	public VersionAware(Properties ps, Set<Dependency> ds) {
		for (Object o : ps.keySet()) {
			String s = (String) o;
			if (s.startsWith("version")) {
				Pattern p = Pattern.compile(getRegexFromIvyVersion(ps
						.getProperty(s)));
				versions.add(p);
			}
		}
		this.inputDependencies = ds;
	}

	protected String getRegexFromIvyVersion(String s) {
		s = s.replaceAll("\\.", Matcher.quoteReplacement("\\."));
		s = s.replaceAll("\\+$", ".*");
		return s;
	}

}
