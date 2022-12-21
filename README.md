# tcp-22

A cross platform 3D graphics meta engine for Java, but still WiP!

See https://thomass171.github.io/tcp-22/tcp-22.html

# Building And Deploying

This shows the build and installation to a web browser running locally serving from
directory $HOME/Sites. Set shell variable HOSTDIR, eg.:

```
export HOSTDIR=$HOME/Sites/tcp-22
```
and create that base directory. Deploy static content bundles needed for building to $HOSTDIR:

```
sh bin/deployBundle.sh data
sh bin/deployBundle.sh corrupted
sh bin/deployBundle.sh -m maze
sh bin/deployBundle.sh -S -m engine
```
 
The module platform-jme requires customized JMonkeyEngine build artifact files, which were built
from JMonkeyEngine 3.2.4-stable with all datatypes float replaced by double. These use the legacy version
2.9.3 of lwjgl (http://legacy.lwjgl.org).

These files reside in subfolder lib and should be installed in the local maven repository.
```
for l in jme3-core jme3-desktop jme3-effects jme3-lwjgl
do
  mvn install:install-file -Dfile=./platform-jme/lib/jme3-core-3.2.4-dbl.jar -DgroupId=org.jmonkeyengine -DartifactId=jme3-core -Dversion=3.2.4-dbl -Dpackaging=jar
done  
```

Maven is needed for building. Run

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
* An @Override annotation needs to be removed in GraphVisualizer.cs manually
* DefaultMenuProvider:menuBuilder.buildMenu
* EcsEntity:entityFilter.matches
* ReferenceScene:GeneralHandler.handle

Furthermore Unity needs to know your HOSTDIR for finding bundle data.
Go to file Main.js and adjust the SetEnvironmentVariable() call accordingly.

And it might be necessary to reassign the script file
Main.cs to game object "MyScriptContainer" to trigger it.

Now the scene "SampleScene" is ready to be started.

# JMonkeyEngine

Nothing special to do any more. All is prepared to launch a scene in JMonkeyEngine.

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
![](docs/UnityPreview.png)

## JMonkeyEngine

Start a scene by the wrapper script launchScene.sh, eg.:

```
sh bin/launchScene.sh de.yard.threed.engine.apps.reference.ReferenceScene
```
![](docs/JMonkeyEnginePreview.png)

## Settings

The following properties are evaluated from the environment or system properties.

ADDITIONALBUNDLE

This is a colon separated list of bundle locations, eg. filesystem paths or web URLs.

# Development
The most convenient way is to develop for a Java platform like JME initially and later test it on other platforms
like ThreeJs and Unity. Thats because the other platforms need converting which reduces
roundtrip time.

In your IDE you might create a launch configuration like the following.

![](docs/IDErunConfiguration.png)

## Build your own scene
The best starting point is to use class [ReferenceScene](engine/src/main/java/de/yard/threed/engine/apps/reference/ReferenceScene.java) and modify it for your needs.

## GWT
Platform platform-webgl uses GWT to compile Java code to JS. GWT Dev mode is started as usual by
```
cd platform-webgl
mvn gwt:run
```
This starts a local Jetty and makes the main entry point "available at http://127.0.0.1:8888/webgl.html". 
However, the page shown is empty, because parameter are missing. Open the
dev console of the browser for additional information.

For ReferenceScene use
```
ADDITIONALBUNDLE=engine,data@http://localhost:80/~thomas/tcp-22/bundles
```
which leads to URL
```
http://localhost:8888/webgl.html?scene=ReferenceScene&devmode=true&ADDITIONALBUNDLE=ZW5naW5lLGRhdGFAaHR0cDovL2xvY2FsaG9zdDo4MC9+dGhvbWFzL3RjcC0yMi9idW5kbGVz
```

and for maze with the default sokoban grid it will be
```
http://localhost:8888/webgl.html?scene=MazeScene&devmode=true&ADDITIONALBUNDLE=ZW5naW5lLGRhdGEsbWF6ZUBodHRwOi8vbG9jYWxob3N0OjgwL350aG9tYXMvdGNwLTIyL2J1bmRsZXM=
```

For avoiding URL de/encoding issues, ADDITIONALBUNDLE parts which are URLs need to be base64 encoded.

Don't forget to allow access in your web server by setting corresponding CORS header.

# Releases
This is the release list In terms of installations to the examples hosting
server used from https://thomass171.github.io/tcp-22/tcp-22.html. Every release contains lots of bug fixes.
Only major changes are listed here.

## 2021-06
Initial release
## 2022-04
Major changes:
  * Additional 'P' style maze games 
## 2022-12
Major changes:
  * VR moving in mazes by controller stick instead of using teleport location marker.
# Technical Details

## Architecture

## Modules

## Bundles

