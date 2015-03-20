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
		// scan
		Set<Dependency> dependencies = new FileSystemScanner(ps).scan();
		dependencies = new HttpScanner(ps, dependencies).scan();
		// resolve
		new Resolver(dependencies).resolve();
	}

}
