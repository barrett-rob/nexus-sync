package org.nexus.sync;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.ivy.MyIvyResolver;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Resolver extends GlobalState {

	final Pattern version;
	final Set<Dependency> dependencies;

	public Resolver(Properties ps, Pattern p, Set<Dependency> ds) {
		super(ps, null);
		this.version = p;
		this.dependencies = ds;
		System.out.println("resolving [" + ds.size()
				+ "] dependencies for version pattern [" + p + "]");
	}

	public void resolve() {
		File ivyXml = createIvyXml();
		executeIvyResolve(ivyXml);
	}

	private File createIvyXml() throws RuntimeException {
		String v = this.version.toString().replace("\\", "").replace(".*", "");
		Document d = new Document();
		Element root = new Element("ivy-module");
		root.setAttribute("version", "2.0");
		Element info = new Element("info");
		info.setAttribute("organisation", "Sync");
		info.setAttribute("module", "virtual-for-" + v.replace('_', '-') + "x");
		root.addContent(info);
		Element configurations = new Element("configurations");
		Element conf = new Element("conf");
		conf.setAttribute("name", "default");
		configurations.addContent(conf);
		root.addContent(configurations);
		Element publications = new Element("publications");
		publications.addContent(new Element("artifact"));
		root.addContent(publications);
		root.addContent(constructDependenciesElement());
		d.addContent(root);
		try {
			File f = File.createTempFile("ivy_" + v + "_", ".xml");
			OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
			new XMLOutputter(Format.getPrettyFormat()).output(d, os);
			os.flush();
			os.close();
			System.out.println("created " + f);
			return f;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Element constructDependenciesElement() {
		Element dependenciesElement = new Element("dependencies");
		for (Dependency d : this.dependencies) {
			Element e = new Element("dependency");
			e.setAttribute("org", d.org);
			e.setAttribute("name", d.name);
			e.setAttribute("rev", getVersionString(d.rev));
			e.setAttribute("conf", "default");
			dependenciesElement.addContent(e);
		}
		dependenciesElement.addContent(constructExcludesElements());
		return dependenciesElement;
	}

	private List<Element> constructExcludesElements() {
		ArrayList<Element> elements = new ArrayList<Element>();
		elements.add(createExcludesElement("org.osgi.core"));
		elements.add(createExcludesElement("velocity"));
		return elements;
	}

	private Element createExcludesElement(String module) {
		Element e = new Element("exclude");
		e.setAttribute("module", module);
		return e;
	}

	private void executeIvyResolve(File ivyXml) {
		long start = System.currentTimeMillis();
		new MyIvyResolver(ivyXml, ivySettingsFile, ivyCacheDir).resolve();
		System.out.println("elapsed: ["
				+ ((double) (System.currentTimeMillis() - start) / 60000)
				+ "] min");
	}

}
