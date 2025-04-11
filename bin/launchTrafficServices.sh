#!/bin/bash
#
# Launches a traffic services server
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

SPRING_ARGS=--spring.main.banner-mode=off

cd $OWNDIR/../traffic-services
checkrc cd
mvn spring-boot:run -Dspring-boot.run.arguments="$SPRING_ARGS"