#!/bin/sh

if [ "$1" = "-rdebug" ]; then
	nice -n 15 $JAVA_HOME/bin/java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -jar org.eclipse.osgi_3.3.1.R33x_v20070828.jar -console
else
	nice -n 15 $JAVA_HOME/bin/java -jar org.eclipse.osgi_3.3.1.R33x_v20070828.jar -console
fi