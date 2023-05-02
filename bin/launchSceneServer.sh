#!/bin/sh
#
# Launches a scene server.
#
# Uses the regular class path by maven, so this is only intended for testing purposes. servermanager should
# be used to launch a scene server from install dir sceneserver/target/buildlib
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

#set -x

validateHOSTDIR


cd $OWNDIR/../sceneserver
checkrc cd

usage() {
	echo "usage: $0 <sceneclass>"
	exit 1
}

if [ $# != 1 ]
then
  usage
fi

# target frame rate 40
mvn exec:java -Dexec.args="--throttle=25 --scene=$1"

exit 0

