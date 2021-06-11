#!/bin/bash
#
# Deploy build artifacts from $BUILDDIR to $HOSTDIR on $HOST
#

OWNDIR=`dirname $0`
. $OWNDIR/common.sh

# Default Values. Default host is localhost, a local installation just copying.
# Remote host names must contain a suffixing colon.
BUILDDIR=target
HOST=
HOSTDIR=tcp-22

usage() {
    echo "Usage: $0 [-b <builddir>] [-h <host>]" 1>&2
    exit 1;
}

while getopts ":b:h:" o; do
    case "${o}" in
        b)
            BUILDDIR=${OPTARG}
            ;;
        h)
            HOST=${OPTARG}
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

rsync -rt --chmod=ugo+rx $BUILDDIR/js $HOST$HOSTDIR
rsync -rt --chmod=ugo+rx $BUILDDIR/webgl $HOST$HOSTDIR
rsync -rt --chmod=ugo+rx $BUILDDIR/threejs $HOST$HOSTDIR
rsync -rt --chmod=ugo+rx $BUILDDIR/webgl.html $HOST$HOSTDIR

for bundle in core data maze engine railing
do
        rsync -rt --chmod=ugo+rx $BUILDDIR/../../../bundles/$bundle $HOST$HOSTDIR/bundles
done