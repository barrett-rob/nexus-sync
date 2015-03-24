package org.nexus.sync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

public class FileSystemScanner implements Scanner {

	static final Set<String> EXCLUDE = new HashSet<String>(
			Arrays.asList(new String[] { ".ear", ".jar", ".war", ".zip",
					".md5", ".sha1", ".swc", ".exe", ".xml.original",
					".DS_Store", "archetype-catalog.xml" }));
	static final Pattern CHARSET_PATTERN = Pattern
			.compile("^.*charset=(\\S*).*$");
	static final Pattern IVY_XML_PATTERN = Pattern
			.compile("^ivy-(\\S*)\\.xml$");

	final File rootDir;
	final Deque<File> files = new ArrayDeque<File>();
	final Set<Dependency> dependencies = new LinkedHashSet<Dependency>();

	public FileSystemScanner(Properties ps) {
		this.rootDir = new File(ps.getProperty("ivy.cache"));
		if (!this.rootDir.exists() || !this.rootDir.isDirectory()) {
			throw new RuntimeException(this.rootDir
					+ " does not exist or is not a directory");
		}
		System.out.println("scanning from root: " + this.rootDir);
	}

	public Set<Dependency> scan() {
		try {
			if (!dependencies.isEmpty()) {
				dependencies.clear();
				files.clear();
			}
			_scan();
			return dependencies;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void _scan() throws ClientProtocolException, IOException {
		files.add(this.rootDir);
		while (!files.isEmpty()) {
			_scan(files.removeFirst());
		}
		System.out.println("after scanning filesystem: "
				+ this.dependencies.size());
	}

	private void _scan(File f) throws ClientProtocolException, IOException {
		for (String exclude : EXCLUDE) {
			if (f.getName().endsWith(exclude)) {
				// ignore
				return;
			}
		}
		if (f.isDirectory()) {
			_handleDir(f);
			return;
		}
		Matcher ivyXmlMatcher = IVY_XML_PATTERN.matcher(f.getName());
		if (ivyXmlMatcher.matches()) {
			_handleFullUrl(f, ivyXmlMatcher);
			return;
		}
	}

	private Deque<String> getSegments(File f) {
		Deque<String> d = new ArrayDeque<String>();
		f = f.getAbsoluteFile();
		while (f != null) {
			d.addFirst(f.getName());
			f = f.getParentFile();
		}
		return d;
	}

	private void _handleDir(File dir) throws IOException,
			ClientProtocolException, IllegalStateException {
		for (File f : dir.listFiles()) {
			files.add(f);
		}
	}

	private void _handleFullUrl(File f, Matcher ivyXmlMatcher) {
		Deque<String> segments = getSegments(f);
		// discard everything that's in root path
		for (int i = 0; i < getSegments(this.rootDir).size(); i++) {
			segments.removeFirst();
		}
		// discard file
		segments.removeLast();
		// get rev and name
		String rev = ivyXmlMatcher.group(1);
		String name = segments.removeLast();
		// everything else is the org
		String org = getOrg(segments);
		dependencies.add(new Dependency(org, name, rev));
	}

	private String getOrg(Deque<String> segments) {
		StringBuilder sb = new StringBuilder();
		for (String s : segments) {
			sb.append(s).append(".");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

}
