#!/bin/sh
#
# Launches a scene server.
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

mvn exec:java -Dexec.args="--throttle=100 --scene=$1"

exit 0

