#!/bin/bash
#
# needs zsh or similar
#
# usage: zsh bin/java2cs.sh <module>
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

#set -x

CLASSPATH=$CLASSPATH:$MR/org/slf4j/slf4j-log4j12/1.5.6/slf4j-log4j12-1.5.6.jar
CLASSPATH=$CLASSPATH:$MR/org/slf4j/slf4j-api/1.5.6/slf4j-api-1.5.6.jar
CLASSPATH=$CLASSPATH:$MR/org/antlr/antlr4-runtime/4.5.2/antlr4-runtime-4.5.2.jar

CLASSPATH=$CLASSPATH:$MR/de/yard/tcp-22/module-java2cs/$VERSION/module-java2cs-$VERSION.jar

usage() {
	echo "usage: $0 <module>"
	exit 1
}

convertModule() {
	MODULE=$1

	SRCDIR=$MODULE/src/main/java
	if [ ! -r $SRCDIR ]
	then
		error $SRCDIR not found
	fi
	TARGETDIR=platform-unity/PlatformUnity/Assets/scripts-generated
	if [ ! -r $TARGETDIR ]
	then
		error $TARGETDIR not found
	fi

	#FILELIST=`echo $MODULE_FILES[$MODULE]` 
	#echo $FILELIST
	#echo java de.yard.threed.java2cs.J2Swift -src $SRCDIR -exclude $MODULE_EXCLUDE[$MODULE] -target $TARGETDIR `echo $MODULE_FILES[$MODULE]`
	java de.yard.threed.java2cs.J2CS -src $SRCDIR -exclude $MODULE_EXCLUDE[$MODULE] -target $TARGETDIR `echo $MODULE_FILES[$MODULE]`

	if [ $? != 0 ]
	then
		error java2cs failed
	fi
}

declare -A MODULE_FILES
declare -A MODULE_EXCLUDE
MODULE_FILES[core]="
	de/yard/threed/core/platform
	de/yard/threed/core/resource
	de/yard/threed/core/testutil
	de/yard/threed/core/buffer
	de/yard/threed/core"
MODULE_EXCLUDE[core]=de/yard/threed/core/JavaStringHelper.java

MODULE_FILES[outofbrowser-common]="
	de/yard/threed/outofbrowser"
MODULE_EXCLUDE[outofbrowser-common]=de/yard/threed/core/XXX.java

MODULE_FILES[engine]="
	de/yard/threed/engine
	de/yard/threed/engine/loader
	de/yard/threed/engine/util
	de/yard/threed/engine/platform
	de/yard/threed/engine/platform/common
	de/yard/threed/engine/osm
	de/yard/threed/engine/apps
	de/yard/threed/engine/apps/reference
	de/yard/threed/engine/apps/vr
	de/yard/threed/engine/ecs
	de/yard/threed/engine/graph
	de/yard/threed/engine/mp
	de/yard/threed/engine/avatar
	de/yard/threed/engine/gui
	de/yard/threed/engine/geometry
	de/yard/threed/engine/util
	de/yard/threed/engine/vr
	de/yard/threed/engine/imaging
	de/yard/threed/engine/test"
MODULE_EXCLUDE[engine]=de/yard/threed/platform/HomeBrewRenderer.java,de/yard/threed/platform/SimpleHeadlessPlatform.java

MODULE_FILES[maze]="
	de/yard/threed/maze"
MODULE_EXCLUDE[maze]=de/yard/threed/maze/GridPath.java,de/yard/threed/maze/PathFinder.java


if [ "$1" = "-q" ]
then
	QUIET=1
	shift
fi
if [ -z "$1" ]
then
	usage
fi

convertModule $1
