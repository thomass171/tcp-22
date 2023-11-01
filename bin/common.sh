#
# Also used by external projects like tcp-flightgear. So should contain generic parts only
# and not be too specific.
#
export VERBOSELOG=0

MR=$HOME/.m2/repository
VERSION=1.0.0-SNAPSHOT

for module in core java-common java-native engine maze outofbrowser-common tools traffic
do
  CLASSPATH=$CLASSPATH:$MR/de/yard/tcp-22/module-$module/$VERSION/module-$module-$VERSION.jar
done

CLASSPATH=$CLASSPATH:$MR/org/apache/logging/log4j/log4j-api/2.17.2/log4j-api-2.17.2.jar
CLASSPATH=$CLASSPATH:$MR/org/apache/logging/log4j/log4j-core/2.17.2/log4j-core-2.17.2.jar
CLASSPATH=$CLASSPATH:$MR/org/apache/logging/log4j/log4j-slf4j-impl/2.17.2/log4j-slf4j-impl-2.17.2.jar
CLASSPATH=$CLASSPATH:$MR/org/slf4j/slf4j-api/1.7.25/slf4j-api-1.7.25.jar
CLASSPATH=$CLASSPATH:$MR/com/google/code/gson/gson/2.8.2/gson-2.8.2.jar
CLASSPATH=$CLASSPATH:$MR/commons-cli/commons-cli/1.3.1/commons-cli-1.3.1.jar
CLASSPATH=$CLASSPATH:$MR/commons-logging/commons-logging/1.2/commons-logging-1.2.jar
CLASSPATH=$CLASSPATH:$MR/commons-lang/commons-lang/2.6/commons-lang-2.6.jar
CLASSPATH=$CLASSPATH:$MR/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar
CLASSPATH=$CLASSPATH:$MR/commons-io/commons-io/2.4/commons-io-2.4.jar
CLASSPATH=$CLASSPATH:$MR/org/apache/commons/commons-configuration2/2.2/commons-configuration2-2.2.jar
CLASSPATH=$CLASSPATH:$MR/org/apache/commons/commons-lang3/3.6/commons-lang3-3.6.jar
CLASSPATH=$CLASSPATH:$MR/org/apache/commons/commons-compress/1.8.1/commons-compress-1.8.1.jar

export CLASSPATH MR

# 1.1.23 Delete no longer existing files because GWT generated files are not unique
COPYCMD="rsync -rt --chmod=ugo+rx --delete"

error() {
        echo $*
        exit 1
}

verboselog() {
        if [ $VERBOSELOG = "1" ]
        then
                echo $*
        fi
}

relax() {
        sleep 1
}

checkrc() {
        RC=$?
        if [ $RC != 0 ]
        then
                error $* returned $RC
        fi
}

validateHOSTDIR() {
        if [ -z "$HOSTDIR" ]; then
                error "HOSTDIR not set"
        fi
        if [ ! -d "$HOSTDIR" ]; then
                error "HOSTDIR $HOSTDIR not found"
        fi
}