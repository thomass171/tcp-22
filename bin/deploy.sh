#!/bin/bash
#
# Deploy the GWT/webgl build artifacts from $BUILDDIR to $HOSTDIR (which might be a remote directory)
# Also deploys the HTML landing page (tcp-22.html).
#
# Also used by external projects via different parameter and envs (eg. HOSTDIR).
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

validateHOSTDIR

# Default Values. Default host is localhost, a local installation just copying.

BUILDDIR=platform-webgl/target/module-platform-webgl-1.0.0-SNAPSHOT

usage() {
    echo "Usage: $0 [-b <builddir>] " 1>&2
    exit 1;
}

while getopts "b:s" o; do
    case "${o}" in
        b)
            BUILDDIR=${OPTARG}
            ;;
        s)
            COPYCMD="scp -pr"
            echo "Using scp. Be sure to delete deprecated files by hand."
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "${BUILDDIR}" ]; then
    usage
fi

if [ ! -d $BUILDDIR ]; then
    error "no directory " $BUILDDIR
fi

echo Ready to deploy from $BUILDDIR to $HOSTDIR. Hit CR
read

# rsync copy will delete deprecated files
$COPYCMD $BUILDDIR/js $HOSTDIR
$COPYCMD $BUILDDIR/webgl $HOSTDIR
$COPYCMD $BUILDDIR/threejs $HOSTDIR
$COPYCMD $BUILDDIR/webgl.html $HOSTDIR
$COPYCMD $BUILDDIR/.htaccess $HOSTDIR
# be prepared for other projects using similar files
$COPYCMD docs/tcp-*.html $HOSTDIR
$COPYCMD docs/tcp-*.js $HOSTDIR
$COPYCMD docs/util.js $HOSTDIR
$COPYCMD docs/sceneutil.js $HOSTDIR
$COPYCMD $BUILDDIR/version.html $HOSTDIR
$COPYCMD docs/*.png $HOSTDIR
$COPYCMD docs/favicon.ico $HOSTDIR