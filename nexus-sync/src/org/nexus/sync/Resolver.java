package org.nexus.sync;

import java.util.Set;

public class Resolver {

	final Set<Dependency> dependencies;

	public Resolver(Set<Dependency> ds) {
		this.dependencies = ds;
		System.out.println("resolving [" + ds.size() + "] dependencies");
	}

	public void resolve() {
	}

}
