#!/bin/sh

JAVA="`which java`"
test -z "$JAVA" && test \! -z "$JAVA_HOME" && JAVA=$JAVA_HOME/bin/java

if [ "$1" = "-rdebug" ]; then
	nice -n 15 $JAVA -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -jar org.eclipse.osgi_3.3.1.R33x_v20070828.jar -console
else
	nice -n 15 $JAVA -jar org.eclipse.osgi_3.3.1.R33x_v20070828.jar -console
fi
