cd $(dirname $0)
MYDIR="$(pwd)"
FELIX=felix.jar
if [ "$1" != "" ];then FELIX=$1;fi
cd $(dirname $FELIX)
cd ..

(echo -e "paxle\n";sleep 2;echo -e "start http://download.eclipse.org/releases/europa/plugins/javax.servlet_2.4.0.v200706111738.jar\n
start http://gd.tuwien.ac.at/infosys/servers/http/apache/dist/felix/org.osgi.core-1.0.0.jar\n
start http://gd.tuwien.ac.at/infosys/servers/http/apache/dist/felix/org.osgi.compendium-1.0.0.jar\n
start http://download.eclipse.org/releases/europa/plugins/org.apache.commons.logging_1.0.4.v200706111724.jar\n
start file://$MYDIR/Core/target/Core-1.0-SNAPSHOT.jar\n
shutdown\n
") |java -jar bin/$(basename $FELIX)
