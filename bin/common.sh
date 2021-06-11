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