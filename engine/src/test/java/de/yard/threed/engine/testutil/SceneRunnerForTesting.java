package de.yard.threed.engine.testutil;

//import de.yard.threed.platform.HomeBrewRenderer;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.World;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;

import java.util.HashMap;

//23.2.21: brauch ich nicht mehr. Doch, ach das ist krude.
//Obwohl, fuer Tests ohne vollstaendigen SceneRunner, Hmm vielleicht doch.
//MA36: Den HomeBrewRenderer gibts hier jetzt nicht mehr.
/*@Deprecated*/
public class SceneRunnerForTesting extends AbstractSceneRunner {
    //private HomeBrewRenderer renderer;

    /**
     * 2.8.21: Jetzt mit den PlatformInternals
     */
    private SceneRunnerForTesting(PlatformInternals platformInternals, InitMethod sceneIinitMethod, String[] bundlelist, Scene scene) {
        super(platformInternals);

        if (scene == null) {
            scene = new Scene() {
                @Override
                public void init(boolean forServer) {
                    if (sceneIinitMethod != null) {
                        sceneIinitMethod.init();
                    }
                }

                @Override
                public void update() {

                }
            };
        } else {
            if (sceneIinitMethod != null) {
                throw new RuntimeException("sceneIinitMethod cannot be used on real scene");
            }
        }

        initAbstract(null/*JmeScene.getInstance(), rm*/, scene);

        World world = new World();
        //((EngineHelper) PlatformJme.getInstance()).setWorld(new World());

        //10.7.21: Camera geht erst, wenn world in der Scene ist
        Scene.world = world;

        // 20.11.21 even though testing is quite headless, a (dummy) camera is helpful for testing, eg. Observer.
        NativeCamera camera = Platform.getInstance().buildPerspectiveCamera(45, 800 / 600, 0.1, 1000);
        // SimpleHeadlessPlatform doesn't/cannot add camera, other might/will.
        if (getCameras().size() == 0) {
            addCamera(camera);
        }

        Platform pl = Platform.getInstance();
        scene.setSceneAndCamera(pl.getScene()/*AbstractSceneRunner.getInstance().scene*/, world);

        // 13.4.17: Die Bundle laden. Ausnahmesweise synchron wegen Test. Doof vor allem bei Einzeltests weil es so lange braucht.
        if (bundlelist != null) {
            for (String bundlename : bundlelist) {
                TestFactory.loadBundleSync(bundlename);
            }
        }

        // 10.4.21: MannMannMann: das ist hier jetzt so reingefriemelt.
        TestHelper.cleanupAsync();

        //27.3.20 dann doch vorher auch den Sceneinit fuer ein paar Systems
        scene.init(false);
        // 9.1.17: Der Vollstaendigkeithalber auch der postinit
        postInit();
    }

    /**
     * Ein Init wie in anderen SceneRunnern auch.
     * 7.7.21
     *
     * @return
     */
    public static SceneRunnerForTesting init(HashMap<String, String> properties, PlatformFactory platformFactory, InitMethod sceneIinitMethod, String[] bundlelist) {
        if (instance != null) {
            throw new RuntimeException("already inited");
        }
        //5.12.18: Mal ohne OpenGL sondern spezieller TestPlatform. Geht aber nicht bei Tests die z.B. nodes brauchen (z.B. world)
        //MA36 jetzt muesste/soll aber Platform gehen.
        /*Engine*/
        PlatformInternals pl = /*(EngineHelper)*/ platformFactory.createPlatform(properties);
        Scene scene = null;
        // better to use "argv.scene"?? Hmm, unclear.
        if (properties.containsKey("scene")) {
            try {
                scene = (Scene) Class.forName(properties.get("scene")).newInstance();
            } catch (Exception e) {
                //TODO log
            }
        }
        instance = new SceneRunnerForTesting(pl, sceneIinitMethod, bundlelist, scene);

        return (SceneRunnerForTesting) instance;
    }

    /**
     * @param frameCount
     */
    public void runLimitedFrames(int frameCount, double tpf) {
        for (int i = 0; i < frameCount; i++) {
            singleUpdate(tpf);
        }
    }

    public void runLimitedFrames(int frameCount) {
        // tpf 0 ist unguenstig, dann bewegt sich nichts.
        runLimitedFrames(frameCount,0.1);
    }

    /**
     * A single "update".
     *
     * @param tpf
     */
    public void singleUpdate(double tpf) {
        //TODO scene.deltaTime = tpf;
        prepareFrame(tpf);
        renderScene();
    }

    private void renderScene() {
        //TODO tja, ist hier was zu tun?
       /*MA36 if (renderer != null) {
            // no camera->no matrix
            renderer.render(null, null, null);
        }
        */
    }

    /*MA36public void setRenderer(HomeBrewRenderer renderer) {
        this.renderer = renderer;
    }*/

    /**
     * Setup scene like it is done in main and render "initialFrames" frames.
     * Scene name is taken from properties.
     *
     */
    public static SceneRunnerForTesting setupForScene(int initialFrames, HashMap<String, String> properties, String[] bundles)  {

        TestFactory.initPlatformForTest( bundles, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()),properties);

        SceneRunnerForTesting sceneRunner =  (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(initialFrames);
        return sceneRunner;
    }

}
