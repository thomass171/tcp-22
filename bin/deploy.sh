#!/bin/bash
#
# Deploy build artifacts from $BUILDDIR to $HOSTDIR on $HOST
# Only native build artifacts for now.
#

OWNDIR=`dirname $0`
. $OWNDIR/common.sh

# Default Values. Default host is localhost, a local installation just copying.
# Remote host names must contain a suffixing colon.
BUILDDIR=target
HOST=
HOSTDIR=tcp-22
COPYCMD="rsync -rt --chmod=ugo+rx"

usage() {
    echo "Usage: $0 [-b <builddir>] [-h <host>]" 1>&2
    exit 1;
}

while getopts ":b:h:s" o; do
    case "${o}" in
        b)
            BUILDDIR=${OPTARG}
            ;;
        h)
            HOST=${OPTARG}
            ;;
        s)
            COPYCMD="scp -pr"
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "${BUILDDIR}" ] || [ -z "${HOSTDIR}" ]; then
    usage
fi

if [ ! -d $BUILDDIR ]; then
    error "no directory " $BUILDDIR
fi

echo Ready to deploy to $HOST$HOSTDIR. Hit CR
read

$COPYCMD $BUILDDIR/js $HOST$HOSTDIR
$COPYCMD $BUILDDIR/webgl $HOST$HOSTDIR
$COPYCMD $BUILDDIR/threejs $HOST$HOSTDIR
$COPYCMD $BUILDDIR/webgl.html $HOST$HOSTDIR
