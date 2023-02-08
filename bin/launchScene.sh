#!/bin/sh
#
# Launches a scene via platform JMonkeyEngine or platform homebrew.
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

#set -x

validateHOSTDIR

PLATFORM=jme
if [ "$1" == "-p" ]
then
  PLATFORM=$2
  shift
  shift
fi
cd $OWNDIR/../platform-$PLATFORM
checkrc cd

usage() {
	echo "usage: $0 [-p <platform>] <sceneclass>"
	exit 1
}

if [ "$1" == "" ]
then
  usage
fi

mvn exec:java -Dexec.args="--scene=$1"

exit 0

