package de.yard.threed.engine.testutil;

//import de.yard.threed.platform.HomeBrewRenderer;

import de.yard.threed.core.InitMethod;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.testutil.SimpleEventBusForTesting;
import de.yard.threed.engine.Scene;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.engine.World;
import de.yard.threed.engine.ecs.LoggingSystemTracker;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;

import static de.yard.threed.javanative.JavaUtil.sleepMs;

/**
 * A scene runner for tests. Needed because tests are not related to any platform and the homebrew scene runner
 * is not available here. And SimpleHeadless has no scene runner.
 * Anyway, its somehow weird because logically its a copy of runner like JME and webgl.
 */
public class SceneRunnerForTesting extends AbstractSceneRunner {
    //private HomeBrewRenderer renderer;

    private LoggingSystemTracker systemTracker = new LoggingSystemTracker();

    /**
     * 2.8.21: Jetzt mit den PlatformInternals
     */
    private SceneRunnerForTesting(PlatformInternals platformInternals, InitMethod sceneIinitMethod, String[] bundlelist, Scene scene) {
        super(platformInternals);

        if (scene == null) {
            scene = new Scene() {
                @Override
                public void init(SceneMode sceneMode) {
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
            if (bundlelist != null) {
                throw new RuntimeException("bundlelist cannot be used on real scene. Should have/use getPreInitBundle()");
            }
            bundlelist = scene.getPreInitBundle();
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
                EngineTestFactory.loadBundleSync(bundlename);
            }
        }

        // 10.4.21: MannMannMann: das ist hier jetzt so reingefriemelt.
        TestHelper.cleanupAsync();

        // For better analyzing use a more verbose system tracker for now
        SystemManager.setSystemTracker(systemTracker);

        //27.3.20 dann doch vorher auch den Sceneinit fuer ein paar Systems
        initScene();
        // 9.1.17: Der Vollstaendigkeithalber auch der postinit
        postInit();
    }

    /**
     * A init like in other scene runner.
     * 7.7.21
     */
    public static SceneRunnerForTesting init(Configuration configuration, PlatformFactory platformFactory, InitMethod sceneIinitMethod, String[] bundlelist) {
        if (instance != null) {
            throw new RuntimeException("already inited");
        }
        //5.12.18: Mal ohne OpenGL sondern spezieller TestPlatform. Geht aber nicht bei Tests die z.B. nodes brauchen (z.B. world)
        //MA36 jetzt muesste/soll aber Platform gehen.
        /*Engine*/
        PlatformInternals pl = /*(EngineHelper)*/ platformFactory.createPlatform(configuration);
        Scene scene = null;
        //6.2.23 still used? Yes.
        if (configuration.getString("scene") != null) {
            try {
                scene = (Scene) Class.forName(configuration.getString("scene")).newInstance();
            } catch (Exception e) {
                //TODO log
            }
        }
        instance = new SceneRunnerForTesting(pl, sceneIinitMethod, bundlelist, scene);
        Platform.getInstance().sceneRunner = instance;
        return (SceneRunnerForTesting) instance;
    }

    /**
     * @param frameCount
     */
    public void runLimitedFrames(int frameCount, double tpf, int delay) {
        for (int i = 0; i < frameCount; i++) {
            singleUpdate(tpf);
            sleepMs(delay);
        }
    }

    public void runLimitedFrames(int frameCount, double tpf) {
        runLimitedFrames(frameCount, tpf, 0);
    }

    public void runLimitedFrames(int frameCount) {
        // tpf 0 ist unguenstig, dann bewegt sich nichts.
        runLimitedFrames(frameCount, 0.1, 0);
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
     */
    public static SceneRunnerForTesting setupForScene(int initialFrames, Configuration configuration, String[] bundles) {

        // Also calls SceneRunnerForTesting.init()
        EngineTestFactory.initPlatformForTest(bundles, new SimpleHeadlessPlatformFactory(new SimpleEventBusForTesting()), null, configuration);

        SceneRunnerForTesting sceneRunner = (SceneRunnerForTesting) SceneRunnerForTesting.getInstance();
        sceneRunner.runLimitedFrames(initialFrames);
        return sceneRunner;
    }

    public LoggingSystemTracker getSystemTracker() {
        return systemTracker;
    }
}
