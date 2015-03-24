package org.nexus.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilteringScanner implements Scanner {

	protected final Set<Dependency> inputDependencies;
	protected final List<Pattern> versions = new ArrayList<Pattern>();

	public FilteringScanner(Properties ps, Set<Dependency> ds) {
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

	public Set<Dependency> scan() {
		List<Dependency> out = new ArrayList<Dependency>();
		for (Dependency d : this.inputDependencies) {
			for (Pattern p : this.versions) {
				if (p.matcher(d.rev).matches()) {
					out.add(d);
					continue;
				}
			}
		}
		Collections.sort(out);
		System.out.println("after filtering for specific versions: "
				+ out.size());
		return new LinkedHashSet<Dependency>(out);
	}
}
