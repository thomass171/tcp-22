package de.yard.threed.platform.jme.sample;

import de.yard.threed.core.Color;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.DirectionalLight;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.apps.reference.ReferenceScene;
import de.yard.threed.engine.loader.DefaultMaterialFactory;
import de.yard.threed.engine.test.Base3DTest;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.platform.jme.JmePlatformFactory;
import de.yard.threed.platform.jme.JmeSceneRunner;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Small setup for, well "getting started" with JmeSceneRunner and PlatformJme.
 * Or testing with new LWJGL for example.
 */
public class GettingStartedScene extends Scene {

    public static void main(String[] args) {

        HashMap<String, String> properties = new HashMap<String, String>();
        Configuration configuration = ConfigurationByEnv.buildDefaultConfigurationWithArgsAndEnv(args, properties);
        PlatformFactory platformFactory = new JmePlatformFactory();
        JmeSceneRunner sceneRunner = JmeSceneRunner.init(platformFactory.createPlatform(configuration));

        Scene updater = new GettingStartedScene();
        sceneRunner.runScene(updater);

    }

    /**
     * Derived from ReferenceScene
     */
    @Override
    public void init(SceneMode sceneMode) {
        Camera camera = getDefaultCamera();
        camera.getCarrier().getTransform().setPosition(new Vector3(0, 5, 11));
        camera.lookAt(new Vector3(0, 0, 0));

        ArrayList<SceneNode> towerright = new ArrayList<SceneNode>();
        ReferenceScene.buildTower("right", towerright, 4, 3, 1, ReferenceScene.rightTowerColors, new DefaultMaterialFactory());
        towerright.get(0).getTransform().setPosition(new Vector3(4, 0, -3));
        addToWorld(towerright.get(0));

        addLightToWorld(new DirectionalLight(new Color(0xee, 0xee, 0xee), new Vector3(0, 1, 1)));
    }

    @Override
    public void update() {

    }
}
