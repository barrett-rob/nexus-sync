package org.nexus.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class Sync {

	public static void main(String[] args) throws FileNotFoundException,
			IOException {
		// load config
		Properties ps = new Properties();
		ps.load(new FileInputStream(new File("nexus-sync.properties")));
		// get base set of deps from filesystem
		Set<Dependency> dependencies = new FileSystemScanner(ps).scan();
		// filter based on versions
		dependencies = new FilteringScanner(ps, dependencies).scan();
		// reduce to latest versions
		dependencies = new ReducingScanner(ps, dependencies).scan();
		// resolve
		new Resolver(dependencies).resolve();
	}

}
