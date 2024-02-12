#!/bin/sh
#
# Copies all files defined in filelist.txt from $CWD/$1 to the
# bundle destination directory BUNDLEDIR ($HOSTDIR/bundles/$1).
# Also does preprocess of pcm-files.
# Each copied file from filelist.txt plus those created are added to directory.txt in destination folder.
#
# 14.11.18: Needs config files filelist.
#
# Die Ausgabe am besten umleiten, da kommt viel.
# Muesste vielleicht mal pro Preprocess gemacht werden, sonst nutzt es nicht viel.
#
# 30.09.23: Also used by external projects, so strictly relying on HOSTDIR, cwd and parameter
#
# 26.10.23: Also model conversion here (eg. 'ac'->'gltf')
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

#set -x

validateHOSTDIR

usage() {
	echo "$0: [-S] [-s] <bundlename>"
	echo "$0: [-S] [-s] -m <modulename>"
	echo "option -S for skipping pcm files."
	exit 1
}

# 6.3.21: Now with multiple model per pcm file
#
processPcm() {
	pcmfile=$1
	destdir=$2
	dirfile=$3
	filename=$4
	subdir=$5
	echo "Processing PCM $pcmfile to $destdir. directory is $dirfile. filename=$filename. subdir=$subdir"
	#echo $CLASSPATH
	#set -x
	cat $pcmfile | while read basename classnameAndArgs
	do
		echo "ModelCreator for " $basename $classnameAndArgs
		java -Djava.awt.headless=true de.yard.threed.tools.ModelCreator -n $basename -o $destdir/$subdir -c $classnameAndArgs
		if [ $? != 0 ]
		then
			echo "ModelCreator failed"
			return 1
		fi
		echo $subdir/$basename.bin >> $dirfile
		echo $subdir/$basename.gltf >> $dirfile
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
  BASENAME=`basename $filename | cut -d'.' -f1`
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
    case $SUFFIX in
      "ac")
        # nasty dependency for FG. Will only be possible from tcp-flightgear.
        if [ ! -r $DESTDIR/$BASENAME.gltf -o "$FORCE" = "1" ]
        then
          sh $TCP22DIR/../tcp-flightgear/bin/convertModel.sh $FNAME $DESTDIR/$DIRNAME
          relax
        fi
        # keep suffix 'ac' in directory? 31.10.23: No, was never that way and BundleRegistry.exists() is aware of that and checks for gltf
        # 9.2.24: BTW: Thats not true. Two old aircraft bundles have ac listed
        echo $DIRNAME/$BASENAME.gltf >> $DIRECTORY
        echo $DIRNAME/$BASENAME.bin >> $DIRECTORY
        ;;
      "pcm")
        if [ "$STATIC" = "1" ]; then
          echo "Skipping pcm"
        else
          # destdir also needs DIRNAME subdir
          processPcm $FNAME $DESTDIR $DIRECTORY $filename $DIRNAME
          checkrc processPcm
        fi
        ;;
      *)
        cp -p $FNAME $DESTDIR/$filename
        checkrc cp
        echo $filename >> $DIRECTORY
    esac
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

