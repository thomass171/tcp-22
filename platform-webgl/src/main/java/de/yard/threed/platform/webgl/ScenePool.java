package de.yard.threed.platform.webgl;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.apps.reference.ReferenceScene;
import de.yard.threed.engine.apps.vr.VrScene;
import de.yard.threed.engine.apps.LightedRotatingCubeScene;
import de.yard.threed.engine.Scene;

import de.yard.threed.core.platform.Log;
import de.yard.threed.maze.MazeScene;
import de.yard.threed.engine.test.MainTestScene;
import de.yard.threed.traffic.apps.BasicTravelScene;


/**
 * Created by thomass on 15.05.15.
 */
public class ScenePool {
    static Log logger = Platform.getInstance().getLog(ScenePool.class);

    public static String[] scenes = new String[]{"LightedRotatingCube", "MazeScene","ShowroomScene","ReferenceScene","ExtrudedSplines","ModelViewScene"};

    public static Scene/*Updater*/ buildSceneUpdater(String name){
        if (name.equals("LightedRotatingCubeScene"))
            return new LightedRotatingCubeScene();
        if (name.equals("MazeScene"))
            return new MazeScene();
        if (name.equals("ReferenceScene"))
            return new ReferenceScene();
        if (name.equals("MainTestScene"))
            return new MainTestScene();
        if (name.equals("VrScene"))
            return new VrScene();
        if (name.equals("BasicTravelScene"))
            return new BasicTravelScene();
        logger.error("Scene " + name + " not found");
        return null;
    }
}
