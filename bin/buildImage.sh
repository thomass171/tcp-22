#!/bin/sh
#
# Building of images for eg. textures
#
# example: bin/buildImage.sh Orange Iconset
#          bin/buildImage.sh LightBlue Labelset
#          sh bin/buildImage.sh darkgreen Face
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

usage() {
	echo "$0: <theme> <name>"
	exit 1
}

buildImage() {
	echo java  de.yard.threed.tools.ImageBuilder -t $1 -n $2
	java  de.yard.threed.tools.ImageBuilder -t $1 -n $2
	if [ $? != 0 ]
	then
		error ImageBuilder failed
	fi
}

if [ -z "$1" ]
then
	usage
fi
if [ -z "$2" ]
then
	usage
fi
buildImage $1 $2
