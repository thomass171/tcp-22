#!/bin/sh
#
# Launches a scene server.
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
cd $OWNDIR/../sceneserver
checkrc cd

usage() {
	echo "$0: -s <sceneclass>"
	exit 1
}

mvn exec:java -Dexec.args="-s $1"

exit 0

