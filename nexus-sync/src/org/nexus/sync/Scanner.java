package org.nexus.sync;

import java.util.Set;

public interface Scanner {

	Set<Dependency> scan();
}
