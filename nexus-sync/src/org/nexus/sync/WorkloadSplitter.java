package org.nexus.sync;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class WorkloadSplitter extends VersionAware {

	public WorkloadSplitter(Properties ps, Set<Dependency> ds) {
		super(ps, ds);
	}

	public Map<Pattern, Set<Dependency>> split() {
		Map<Pattern, Set<Dependency>> m = new LinkedHashMap<Pattern, Set<Dependency>>();
		for (Dependency d : this.inputDependencies) {
			for (Pattern p : this.versions) {
				if (p.matcher(d.rev).matches()) {
					Set<Dependency> byVersion = m.get(p);
					if (byVersion == null) {
						m.put(p, byVersion = new LinkedHashSet<Dependency>());
					}
					byVersion.add(d);
				}
			}
		}
		return m;
	}

}
