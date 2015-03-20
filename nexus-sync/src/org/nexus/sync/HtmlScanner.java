package org.nexus.sync;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlScanner implements Scanner {

	static final Set<String> EXCLUDE = new HashSet<String>(
			Arrays.asList(new String[] { ".ear", ".jar", ".war", ".zip",
					".md5", ".sha1", ".swc", ".exe", "maven-metadata.xml",
					"archetype-catalog.xml" }));
	static final Pattern CHARSET_PATTERN = Pattern
			.compile("^.*charset=(\\S*).*$");
	static final Pattern IVY_XML_PATTERN = Pattern
			.compile("^.*/(ivy)-(\\S*)\\.xml$");
	static final Pattern POM_PATTERN = Pattern
			.compile("^.*/(\\S*)-(\\S*)\\.pom$");

	final CloseableHttpClient chc = HttpClientBuilder.create().build();
	final String rootUrl;
	final Deque<String> deque = new ArrayDeque<String>();
	final Set<Dependency> list = new LinkedHashSet<Dependency>();
	final List<Pattern> repositories = new ArrayList<Pattern>();

	public HtmlScanner(Properties ps) {
		this.rootUrl = ps.getProperty("url");
		System.out.println("scanning from root url: " + this.rootUrl);
		for (Object o : ps.keySet()) {
			String s = (String) o;
			if (s.startsWith("repository")) {
				Pattern p = Pattern.compile(ps.getProperty(s));
				repositories.add(p);
				System.out.println("scanning repositories matching: " + p);
			}
		}
	}

	public Set<Dependency> scan() {
		long start = System.currentTimeMillis();
		try {
			if (!list.isEmpty()) {
				list.clear();
				deque.clear();
			}
			_scan();
			return list;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				chc.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			long elapsed = System.currentTimeMillis() - start;
			System.out.println("elapsed: [" + ((double) elapsed / (60 * 1000))
					+ "] min");
		}
	}

	private void _scan() throws ClientProtocolException, IOException {
		deque.add(rootUrl);
		while (!deque.isEmpty()) {
			_scan(deque.removeLast(), 0);
		}
	}

	private void _scan(String url, int depth) throws ClientProtocolException,
			IOException {
		for (String exclude : EXCLUDE) {
			if (url.endsWith(exclude)) {
				// ignore
				return;
			}
		}
		if (depth == 1 && !isValidRepository(url)) {
			return;
		}
		if (url.endsWith("/")) {
			_handlePath(url, depth);
			return;
		}
		Matcher ivyXmlMatcher = IVY_XML_PATTERN.matcher(url);
		if (ivyXmlMatcher.matches()) {
			_handleFullUrl(url);
			return;
		}
		Matcher pomMatcher = POM_PATTERN.matcher(url);
		if (pomMatcher.matches()) {
			_handleFullUrl(url);
			return;
		}
	}

	private boolean isValidRepository(String url) {
		String[] words = getWords(url);
		String repository = words[words.length - 1];
		for (Pattern p : repositories) {
			if (p.matcher(repository).matches()) {
				return true;
			}
		}
		return false;
	}

	private String[] getWords(String url) {
		return url.split("/");
	}

	private void _handlePath(String url, int depth) throws IOException,
			ClientProtocolException, IllegalStateException {
		CloseableHttpResponse chr = chc.execute(new HttpGet(url));
		int sc = chr.getStatusLine().getStatusCode();
		if (sc >= 200 && sc < 400) {
			HttpEntity he = chr.getEntity();
			Document d = Jsoup.parse(he.getContent(), getContentType(he), url);
			_handleDocument(url, d, depth + 1);
		}
	}

	private String getContentType(HttpEntity he) {
		Header h = he.getContentType();
		String contentType = h == null ? null : h.getValue();
		if (contentType != null) {
			Matcher m = CHARSET_PATTERN.matcher(contentType);
			if (m.matches()) {
				contentType = m.group(1);
			} else {
				contentType = null;
			}
		}
		if (contentType == null) {
			contentType = "UTF-8";
		}
		return contentType;
	}

	private void _handleDocument(String url, Document d, int depth)
			throws ClientProtocolException, IOException {
		Elements es = d.select("a[href]");
		for (Element e : es) {
			String href = e.attr("href");
			if (href.startsWith(url)) {
				_scan(href, depth);
			}
		}
	}

	private void _handleFullUrl(String url) {
		String rev = getRev(url);
		String name = getName(url);
		String org = getOrg(url);
		Dependency d = new Dependency(org, name, rev);
		System.out.println(d);
		list.add(d);
	}

	private String getOrg(String url) {
		// all words between repository and 3rd last
		String s = url.replace(this.rootUrl, "");
		String[] words = getWords(s);
		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < words.length - 3; i++) {
			sb.append(words[i]).append(".");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	private String getName(String url) {
		// 3rd last word is the name
		String[] words = getWords(url);
		return words[words.length - 3];
	}

	private String getRev(String url) {
		// 2nd last word is the rev
		String[] words = getWords(url);
		return words[words.length - 2];
	}

}
