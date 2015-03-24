package org.nexus.sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class ReducingScanner extends VersionAware {

	public ReducingScanner(Properties ps, Set<Dependency> ds) {
		super(ps, ds);
	}

	public Set<Dependency> scan() {
		List<Dependency> out = new ArrayList<Dependency>();
		Map<StringPair, Map<Pattern, List<Dependency>>> m = new LinkedHashMap<StringPair, Map<Pattern, List<Dependency>>>();
		for (Dependency d : this.inputDependencies) {
			StringPair sp = new StringPair(d.org, d.name);
			Map<Pattern, List<Dependency>> byVersion = m.get(sp);
			if (byVersion == null) {
				m.put(sp,
						byVersion = new LinkedHashMap<Pattern, List<Dependency>>());
			}
			// store by version
			for (Pattern p : this.versions) {
				if (p.matcher(d.rev).matches()) {
					List<Dependency> ds = byVersion.get(p);
					if (ds == null) {
						byVersion.put(p, ds = new ArrayList<Dependency>());
					}
					ds.add(d);
				}
			}
		}
		// get the most recent from each version
		for (Map<Pattern, List<Dependency>> byVersion : m.values()) {
			for (List<Dependency> ds : byVersion.values()) {
				out.add(getLatest(ds));
			}
		}
		Collections.sort(out);
		System.out.println("after reducing to latest versions: " + out.size());
		return new LinkedHashSet<Dependency>(out);
	}

	private Dependency getLatest(List<Dependency> ds) {
		Dependency latest = null;
		for (Dependency d : ds) {
			// TODO - replace this with proper ivy version comparison
			if (latest == null || latest.rev.compareTo(d.rev) < 0) {
				latest = d;
			}
		}
		return latest;
	}

}
