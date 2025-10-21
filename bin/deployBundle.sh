#!/bin/sh
#
# Copies all files defined in $FILELIST from $CWD/$1 to the
# bundle destination directory BUNDLEDIR (defaults to $HOSTDIR/bundles/$1).
# Also does preprocess of pcm-files.
# Each copied file from $FILELIST plus those created are added to directory.txt in destination folder.
#
# 14.11.18: Needs config file filelist.
# 30.09.23: Also used by external projects, so strictly relying on HOSTDIR, cwd and parameter
# 26.10.23: Also model conversion here (eg. 'ac'->'gltf')
# 28.08.25: Filelist file might be a parameter to be used for FG aircraft conversion
#

OWNDIR=`dirname $0`
source $OWNDIR/common.sh || exit 1

#set -x

validateHOSTDIR

usage() {
	echo "$0: [-S] [-s] <bundlename>"
	echo "$0: [-S] [-s] -m <modulename>"
	echo "option -S for skipping pcm files. 'filelist' defaults to 'filelist.txt' in source sub directory."
	echo "'bundlename' is the pure name, no path. The source sub dir is expected in the current directory."
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
		if [ $subdir == "." ]
		then
		    echo $basename.bin >> $dirfile
        echo $basename.gltf >> $dirfile
		else
		    echo $subdir/$basename.bin >> $dirfile
		    echo $subdir/$basename.gltf >> $dirfile
	  fi
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

if [ -z "$BUNDLEDIR" ]
then
	BUNDLEDIR=$HOSTDIR/bundles
fi
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
	FILELIST=$SOURCE/filelist.txt
else
  # check whether we have a bundle definition (relative to current path), otherwise just assume its a sub directory
  if [ -r bundledefs/$BUNDLE.bundledef ]
  then
    BUNDLEDEF=bundledefs/$BUNDLE.bundledef
    FILELIST=bundledefs/$BUNDLE.filelist

    if [ ! -s $BUNDLEDEF ]
    then
    	error "$BUNDLEDEF not found"
    fi
    source $BUNDLEDEF
  else
  	SOURCE=$BUNDLE
  	FILELIST=$SOURCE/filelist.txt
  fi
fi

if [ ! -d "$SOURCE" ]
then
	error "$SOURCE not found"
fi

if [ ! -s $FILELIST ]
then
  error "filelist $FILELIST not found"
fi

export FILELIST
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

TMPFILELIST=$HOME/tmp/tmpfilelist.txt
rm -f $TMPFILELIST
cat $FILELIST | egrep -v "^#" | while read filename
do
	if [ -z "$filename" ]
	then
		error "empty file name"
	fi
	#echo $filename

  FNAME=$SOURCE/$filename
  if [ ! -r "$FNAME" ]
  then
    error "$FNAME not found"
  fi

  if [ -d $FNAME ]
  then
    echo "Expanding directory $FNAME"
    cd $SOURCE
    checkrc cd
    find $filename -type f | grep -v .dirhash | grep -v .dirindex >> $TMPFILELIST
    checkrc find
    cd - > /dev/null
    checkrc cd
  else
    echo $filename >> $TMPFILELIST
  fi
done
checkrc "Building $TMPFILELIST"

# Now really process the file list
cat $TMPFILELIST | while read filename
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
    error "$FNAME is a directory. Only single files allowed. "
  fi
  if [ -f $FNAME ]
  then
    #single file
    SUFFIX="${FNAME##*.}"
    case $SUFFIX in
      "ac")
        # nasty dependency for FG. Will only be possible from tcp-flightgear.
        if [ ! -r $DESTDIR/$DIRNAME/$BASENAME.gltf -o "$FORCE" = "1" ]
        then
          sh $TCP22DIR/bin/convertSingleModelToGltf.sh $FNAME $DESTDIR/$DIRNAME
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
      # 13.7.25 convert deprecated 'rgb' to 'png'. BTW: LoaderAC renames references to 'rgb' in 'ac' files to 'png', so GLTF files will always use 'png'.
      # '$FORCE' only for tcp-flightgear?
      "rgb")
        if [ ! -r $DESTDIR/$DIRNAME/$BASENAME.png -o "$FORCE" = "1" ]
        then
          # CmdLine tool of https://imagemagick.org. BTW, gimp knows how to display rgb files
          #MAGICK_SIZE_CLAUSE=
          #if [ $BASENAME = "clock-transparent" ]
          #then
          #  MAGICK_SIZE_CLAUSE="-size 128x128"
          #fi
          magick $MAGICK_SIZE_CLAUSE $FNAME $DESTDIR/$DIRNAME/$BASENAME.png
          checkrc magick
        fi
        ;;
      # 24.9.25: Checking suffix and aborting is difficult as there are many many file types and some even without suffix (like 'AUTHORS','LICENCE')
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

