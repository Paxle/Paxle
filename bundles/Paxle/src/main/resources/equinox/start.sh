#!/bin/sh

test \! -z "$JAVA_HOME" && JAVA=$JAVA_HOME/bin/java
test -z "$JAVA" && JAVA="`which java`"

if [ "$1" = "-rdebug" ]; then
	nice -n 15 $JAVA -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -jar ${equinox.runtime.jar} -refresh -console
else
	nice -n 15 $JAVA -jar ${equinox.runtime.jar} -refresh -console
fi
