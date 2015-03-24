package org.nexus.sync;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSystemScanner extends GlobalState {

	static final Pattern CHARSET_PATTERN = Pattern
			.compile("^.*charset=(\\S*).*$");
	static final Pattern IVY_XML_PATTERN = Pattern
			.compile("^ivy-(\\S*)\\.xml$");

	final Deque<File> files = new ArrayDeque<File>();

	public FileSystemScanner(Properties ps) {
		super(ps, new LinkedHashSet<Dependency>());
	}

	public Set<Dependency> scan() {
		try {
			if (!inputDependencies.isEmpty()) {
				inputDependencies.clear();
				files.clear();
			}
			_scan();
			return inputDependencies;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void _scan() {
		files.add(this.ivyCacheDir);
		while (!files.isEmpty()) {
			_scan(files.removeFirst());
		}
		System.out.println("after scanning filesystem: "
				+ this.inputDependencies.size() + " dependencies");
	}

	private void _scan(File f) {
		for (Pattern p : this.excludes) {
			if (p.matcher(f.getName()).matches()) {
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

	private void _handleDir(File dir) {
		for (File f : dir.listFiles()) {
			files.add(f);
		}
	}

	private void _handleFullUrl(File f, Matcher ivyXmlMatcher) {
		Deque<String> segments = getSegments(f);
		// discard everything that's in root path
		for (int i = 0; i < getSegments(this.ivyCacheDir).size(); i++) {
			segments.removeFirst();
		}
		// discard file
		segments.removeLast();
		// get rev and name
		String rev = ivyXmlMatcher.group(1);
		String name = segments.removeLast();
		// everything else is the org
		String org = getOrg(segments);
		inputDependencies.add(new Dependency(org, name, rev));
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
