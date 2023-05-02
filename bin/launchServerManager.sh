#!/bin/sh
#
# Launches a server manager.
#
# Uses the fat jar also used for deployment and sceneserver/target/buildlib
# for launching a scene server
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

#set -x

# a scene server started by server manager will need a HOSTDIR
validateHOSTDIR

cd $OWNDIR/../servermanager
checkrc cd

usage() {
	echo "usage: $0 "
	exit 1
}

java -jar target/module-servermanager-1.0.0-SNAPSHOT.jar

exit 0

