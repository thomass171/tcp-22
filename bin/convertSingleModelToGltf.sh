#!/bin/bash
#
# Convert a single 'ac' or 'btg' file to GLTF
# $1=inputfile
# $2=destination directory
#
# Not intended to run standalone but only by other scripts. So doesn't source common.sh. Instead
# the environment (classpath) and current directory should be prepared by the caller.
#
# Honors env variable ADDITIONAL_LOADER (needed for btg conversion)
#
# Does nothing bundle specific (eg. directory.txt handling).
# Derived from tcp-flightgear's convertModel.sh.
#

# needs HOSTDIR for building a platform and loading other bundle (eg. sgmaterial)

#
#??export BUILDDIR=platform-webgl-ext/target/module-platform-webgl-ext-1.0.0-SNAPSHOT

echo "Converting $1 in" `pwd` "to GLTF in" $2
#?? export ADDITIONALBUNDLE=$HOSTDIRFG/bundles
#java -Djava.awt.headless=true de.yard.threed.tools.GltfProcessor -gltf -o $2 "$1" -l de.yard.threed.toolsfg.LoaderBTGBuilder
if [ ! -z "$ADDITIONAL_LOADER" ]
then
  # add option to make java call simpler
  ADDITIONAL_LOADER="-l $ADDITIONAL_LOADER"
fi
java -Djava.awt.headless=true de.yard.threed.tools.GltfProcessor -gltf -o $2 "$1" $ADDITIONAL_LOADER
exit $?

