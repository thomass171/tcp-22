#!/bin/bash
#
# Launches maze services server
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

SPRING_ARGS=--spring.main.banner-mode=off
SPRING_ARGS="${SPRING_ARGS} --spring.datasource.url=jdbc:postgresql://localhost:5432/mz"
SPRING_ARGS="${SPRING_ARGS} --spring.datasource.username=maze"
SPRING_ARGS="${SPRING_ARGS} --spring.datasource.password=maze2022"

cd $OWNDIR/../services
checkrc cd
mvn spring-boot:run -Dspring-boot.run.arguments="$SPRING_ARGS"