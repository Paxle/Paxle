#!/bin/sh

# java runtime lookup
test \! -z "$JAVA_HOME" && JAVA=$JAVA_HOME/bin/java
test -z "$JAVA" && JAVA="`which java`"

# change directory
cd "`dirname $0`"

# read java runtime options
JAVA_ARGS=""
if [ -f start.ini ]
then
	JAVA_ARGS_FILE=`cat start.ini`
	for JAVA_ARG in $JAVA_ARGS_FILE
	do		
		JAVA_ARG=`echo $JAVA_ARG | sed 's/\r//g'`
		if [ -n $JAVA_ARG ]; then JAVA_ARGS="$JAVA_ARG $JAVA_ARGS"; fi;
	done
	echo "Using Java Args: $JAVA_ARGS"
fi

# startup java
if [ "$1" = "-rdebug" ]; then
	nice -n 15 $JAVA $JAVA_ARGS -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -jar ${equinox.runtime.jar} $@
else
	nice -n 15 $JAVA $JAVA_ARGS -jar ${equinox.runtime.jar} $@
fi
echo "Shutdown finished"
