#!/bin/bash
#
# build and deploy everything like described in README.md
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

validateHOSTDIR

sh bin/deployBundle.sh data
checkrc deployBundle
sh bin/deployBundle.sh corrupted
checkrc deployBundle
sh bin/deployBundle.sh -m maze
checkrc deployBundle
sh bin/deployBundle.sh -S -m engine
checkrc deployBundle
sh bin/deployBundle.sh -m traffic
checkrc deployBundle

for l in jme3-core jme3-desktop jme3-effects jme3-lwjgl
do
        mvn install:install-file -Dfile=./platform-jme/lib/$l-3.2.4-dbl.jar -DgroupId=org.jmonkeyengine -DartifactId=$l -Dversion=3.2.4-dbl -Dpackaging=jar
        checkrc mvn
done

# 3.8.24 Some tests in engine need pcm from bundle
mvn install -DskipTests=true
checkrc mvn

sh bin/deployBundle.sh  -m engine
checkrc deployBundle

mvn install
checkrc mvn

sh bin/deploy.sh
checkrc deploy

for m in core engine maze outofbrowser-common graph traffic-core traffic
do
        zsh bin/java2cs.sh $m
        checkrc java2cs $m
done
