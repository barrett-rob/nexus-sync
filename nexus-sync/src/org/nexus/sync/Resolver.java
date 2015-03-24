package org.nexus.sync;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Resolver {

	final Pattern version;
	final Set<Dependency> dependencies;

	public Resolver(Pattern p, Set<Dependency> ds) {
		this.version = p;
		this.dependencies = ds;
		System.out.println("resolving [" + ds.size()
				+ "] dependencies for version pattern [" + p + "]");
	}

	public void resolve() {
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
			e.setAttribute("rev", d.rev);
			dependenciesElement.addContent(e);
		}
		return dependenciesElement;
	}
}
