package org.nexus.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class Sync {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		// load config
		Properties ps = new Properties();
		ps.load(new FileInputStream(new File("nexus-sync.properties")));
		// scan nexus
		List<String> versions = new Scanner(ps).scan();
		// resolve
		new Resolver(versions).resolve();
	}

}
