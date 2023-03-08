#!/bin/bash
#
# Converts a module to CS
# needs zsh or similar
#
# usage: zsh bin/java2cs.sh <module>
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

#set -x

CLASSPATH=$CLASSPATH:$MR/org/apache/logging/log4j/log4j-api/2.17.2/log4j-api-2.17.2.jar
CLASSPATH=$CLASSPATH:$MR/org/apache/logging/log4j/log4j-core/2.17.2/log4j-core-2.17.2.jar
CLASSPATH=$CLASSPATH:$MR/org/apache/logging/log4j/log4j-slf4j-impl/2.17.2/log4j-slf4j-impl-2.17.2.jar
CLASSPATH=$CLASSPATH:$MR/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar
CLASSPATH=$CLASSPATH:$MR/org/antlr/antlr4-runtime/4.5.2/antlr4-runtime-4.5.2.jar

CLASSPATH=$CLASSPATH:$MR/de/yard/tcp-22/module-java2cs/$VERSION/module-java2cs-$VERSION.jar

usage() {
	echo "usage: java2cs.sh [-q][-m <moduledef>][-t <basetargetdir>] <module>"
	exit 1
}

convertModule() {
	MODULE=$1

	SRCDIR=$MODULE/src/main/java
	if [ ! -r $SRCDIR ]
	then
		error $SRCDIR not found
	fi
	if [ ! -r $BASETARGETDIR ]
	then
		error $BASETARGETDIR not found
	fi
	TARGETDIR=$BASETARGETDIR/Assets/scripts-generated
	if [ ! -r $TARGETDIR ]
	then
		mkdir $TARGETDIR
		checkrc mkdir
	fi
  if [ -z "$MODULE_FILES[$MODULE]" ]
	then
		error no module files defined for module $MODULE
	fi
	if [ -z "$MODULE_EXCLUDE[$MODULE]" ]
	then
		error no module exclude files defined for module $MODULE
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

BASETARGETDIR=platform-unity/PlatformUnity

MODULEDEFINITION=java2cs/modules.sh
if [ "$1" = "-q" ]
then
	QUIET=1
	shift
fi
if [ "$1" = "-m" ]
then
	MODULEDEFINITION=$2
	shift
	shift
fi
if [ "$1" = "-t" ]
then
	BASETARGETDIR=$2
	shift
	shift
fi

if [ ! -r "$MODULEDEFINITION" ]
then
  error $MODULEDEFINITION not found
fi
source $MODULEDEFINITION || exit 1

if [ -z "$1" ]
then
	usage
fi

convertModule $1
