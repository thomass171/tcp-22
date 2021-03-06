#!/bin/sh
#
# Kopiert alle Dateien anhand der Filelist in das bundle Verzeichnis
# in BUNDLEDIR.
#
# 14.11.18: Needs config files filelist.
#
# Und macht dann einen preprocess. Die Ausgabe am besten umleiten, da kommt viel.
# Muesste vielleicht mal pro Preprocess gemacht werden, sonst nutzt es nicht viel.
#
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

#set -x

validateHOSTDIR

usage() {
	echo "$0: [--nopp] <bundlename>"
	echo "$0: [--nopp] -m <modulename>"
	exit 1
}

# 6.3.21: Now with multiple model per pcm file
#
processPcm() {
	pcmfile=$1
	destdir=$2
	dirfile=$3
	filename=$4
	echo "Processing PCM $pcmfile to $destdir. directory is $dirfile"
	#echo $CLASSPATH
	#set -x
	cat $pcmfile | while read basename classnameAndArgs
	do
		echo "ModelCreator for " $basename $classnameAndArgs
		java -Djava.awt.headless=true de.yard.threed.tools.ModelCreator -n $basename -o $destdir -c $classnameAndArgs
		if [ $? != 0 ]
		then
			echo "ModelCreator failed"
			return 1
		fi
		echo $basename.bin >> $dirfile
		echo $basename.gltf >> $dirfile
	done
	return 0
}

export BUNDLEISMODULE=0
export STATIC=0
while getopts "Ssm" o; do
    case "${o}" in
        s)
            COPYCMD="scp -pr"
            ;;
        m)
            BUNDLEISMODULE=1
            ;;
        S)
            STATIC=1
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

NOPP=0

if [ -z "$1" ]
then
	usage
fi

BUNDLE=$1
shift

BUNDLEDIR=$HOSTDIR/bundles
mkdir -p $BUNDLEDIR
checkrc mkdir

if [ "$BUNDLE" == "corrupted" ]
then
	echo "special handling for manual built bundle"
	$COPYCMD corrupted $BUNDLEDIR
	exit 0
fi

if [ $BUNDLEISMODULE = "1" ]
then
	SOURCE=$BUNDLE/src/main/resources
else
	SOURCE=$BUNDLE
fi

if [ ! -d "$SOURCE" ]
then
	error "$SOURCE not found"
fi

if [ ! -r $SOURCE/filelist.txt ]
then
  error "$SOURCE/filelist.txt not found"
fi

DESTDIR=$BUNDLEDIR/$BUNDLE
if [ ! -d $DESTDIR ]
then
	mkdir $DESTDIR
	checkrc mkdir
fi
DIRECTORY=$DESTDIR/directory.txt
rm -f $DIRECTORY

echo Ready to deploy bundle $BUNDLE from $SOURCE to $DESTDIR. Hit CR
read

cat $SOURCE/filelist.txt | egrep -v "^#" | while read filename
do
	if [ -z "$filename" ]
	then
		error "empty file name"
	fi
	echo $filename

  FNAME=$SOURCE/$filename
  if [ ! -r "$FNAME" ]
  then
    error "$FNAME not found"
  fi

  # create destination target dir
  DIRNAME=`dirname $filename`
  if [ ! -z "$DIRNAME" -a ! -d "$DESTDIR/$DIRNAME" ]
  then
    mkdir -p $DESTDIR/$DIRNAME
    checkrc mkdir
  fi

  if [ -d $FNAME ]
  then
    error "only single files allowed"
  fi
  if [ -f $FNAME ]
  then
    #single file
    SUFFIX="${FNAME##*.}"
    #replace pcm file by creating models
    if [ "$SUFFIX" = "pcm" ]
    then
      if [ "$STATIC" = "1" ]; then
        echo "Skipping pcm"
      else
        processPcm $FNAME $DESTDIR $DIRECTORY $filename
        checkrc processPcm
      fi
    else
      cp -p $FNAME $DESTDIR/$filename
      checkrc cp
      echo $filename >> $DIRECTORY
    fi
  else
    error "unknown file type of file $FNAME"
  fi
done

if [ -s $DIRECTORY ]
then
	sort -u -o $DIRECTORY $DIRECTORY
else
	touch $DIRECTORY
fi
chmod 644 $DIRECTORY
exit 0

