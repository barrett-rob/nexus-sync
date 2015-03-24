package org.nexus.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class FilteringScanner extends GlobalState {

	public FilteringScanner(Properties ps, Set<Dependency> ds) {
		super(ps, ds);
	}

	public Set<Dependency> scan() {
		List<Dependency> out = new ArrayList<Dependency>();
		for (Dependency d : this.inputDependencies) {
			for (Pattern p : this.versionPatterns) {
				if (p.matcher(d.rev).matches()) {
					out.add(d);
					continue;
				}
			}
		}
		Collections.sort(out);
		System.out.println("after filtering for specific versions: "
				+ out.size() + " dependencies");
		return new LinkedHashSet<Dependency>(out);
	}
}
