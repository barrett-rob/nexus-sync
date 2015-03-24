package org.nexus.sync;

public class Dependency implements Comparable<Dependency> {

	public final String org, name, rev;

	public Dependency(String org, String name, String rev) {
		this.org = org;
		this.name = name;
		this.rev = rev;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((org == null) ? 0 : org.hashCode());
		result = prime * result + ((rev == null) ? 0 : rev.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dependency other = (Dependency) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (org == null) {
			if (other.org != null)
				return false;
		} else if (!org.equals(other.org))
			return false;
		if (rev == null) {
			if (other.rev != null)
				return false;
		} else if (!rev.equals(other.rev))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Dependency [org=" + org + ",\tname=" + name + ",\trev=" + rev
				+ "]";
	}

	@Override
	public int compareTo(Dependency that) {
		int n = this.org.compareTo(that.org);
		if (n == 0) {
			n = this.name.compareTo(that.name);
			if (n == 0) {
				return this.rev.compareTo(that.rev);
			} else {
				return n;
			}
		} else {
			return n;
		}
	}

}
