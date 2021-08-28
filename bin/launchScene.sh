#!/bin/sh
#
# Launches a scene via JMonkeyEngine
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

#set -x

validateHOSTDIR

usage() {
	echo "$0: [properties] <scenename>"
	exit 1
}

java $SCENECLASS

exit 0

