#!/bin/sh
java -classpath "bin:lib/*" org.nexus.sync.Sync
exit $?
