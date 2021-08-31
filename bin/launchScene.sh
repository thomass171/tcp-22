#!/bin/sh
#
# Launches a scene via JMonkeyEngine
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

#set -x

validateHOSTDIR

cd $OWNDIR/../platform-jme
checkrc cd

usage() {
	echo "$0: <sceneclass>"
	exit 1
}

mvn exec:java -Dscene=$1

exit 0

