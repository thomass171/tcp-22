# tcp-22

A cross platform 3D graphics meta engine for Java, but still WiP!

See https://thomass171.github.io/tcp-22/tcp-22.html

# Building And Deploying

This shows the installation to a web browser running locally serving from
directory $HOME/Sites. Set shell variable HOSTDIR, eg.:

```
export HOSTDIR=$HOME/Sites/tcp-22
```
and create that base directory.
Deploy static content bundles needed for building:

```
sh bin/deployBundle.sh data
sh bin/deployBundle.sh corrupted
sh bin/deployBundle.sh -m maze
sh bin/deployBundle.sh -S -m engine
```
maven is needed for building. Run

```
mvn clean install
```

for building.
Deploy the software:

```
sh bin/deploy.sh
```

Deploy the remaining bundles:

```
sh bin/deployBundle.sh -m engine
```

## Unity
Convert the needed files to CS (be sure to use a zsh compatible shell like bash):
```
for m in core engine maze outofbrowser-common
do
        zsh bin/java2cs.sh $m
done
```
The converted files are copied to _platform-unity/PlatformUnity/Assets/scripts-generated_

Open the project platform-unity/PlatformUnity in Unity. It should compile all the
files with two errors related to using delegates. Unfortunately the converter does not 
remove the FunctionalInterface (which become delegates in CS) method names, so you need to remove these manually.
In detail these are:

* ImageHelper:handler.handlePixel
* AbstractSceneRunner:handler.handle

Furthermore Unity needs to know your HOSTDIR for finding bundle data.
Go to file Main.js and adjust the SetEnvironmentVariable() call accordingly.

Now the scene "SampleScene" is ready to be started.

# Running
## Browser
Enter the URL
```
http://localhost/<youruserdir>/tcp-22/tcp-22.html?host=http://localhost/<youruserdir>/tcp-22
```
in your browser. Be sure to add the "host" parameter for really accessing your local installation.
Check the developer console for possible errors. 
You should see the [ReferenceScene](engine/src/main/java/de/yard/threed/engine/apps/reference/ReferenceScene.java)

![](docs/ReferenceScene-webgl.png)

## Unity
Just start the scene.
# Build your own scene
The best starting point is to use class [ReferenceScene](engine/src/main/java/de/yard/threed/engine/apps/reference/ReferenceScene.java) and modify it for your needs.

# Technical Details

## Architecture

## Modules

