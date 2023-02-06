package de.yard.threed.platform.homebrew;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.engine.SceneMode;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.engine.World;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.Scene;
import de.yard.threed.core.platform.NativeSceneRunner;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;
import de.yard.threed.outofbrowser.SyncBundleLoader;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import java.util.HashMap;

/**
 * In eigenem Thread, um nicht den awtEvnet Thread zu blockieren (wirklich)?
 * 30.1.15: Wenn es nicht als eigener Thread läuft, kann man z.B. nicht parallel ein JFrame (ModelViewer aufhaben).
 * Darum doch als Thread.
 * 10.4.15: Der Thread ist jetzt im Renderloop Start
 * 25.1.23: Also used in headless mode (without custom renderer) in scene server.
 * <p/>
 * Date: 14.02.14
 * Time: 16:09
 */
public class HomeBrewSceneRunner extends AbstractSceneRunner implements NativeSceneRunner {
    Log logger = Platform.getInstance().getLog(HomeBrewSceneRunner.class);
    Scene scene;
    private static HomeBrewSceneRunner instance = null;
    // 8.4.21: Optional frame limit for testing.
    public int frameLimit = 0;
    private HomeBrewCamera cameraToRender = null;
    private SceneMode sceneMode;
    public int renderbrake = 0;

    /**
     * Private, weil es im Grunde ein Singleton ist.
     */
    private HomeBrewSceneRunner(PlatformInternals platformInternals, SceneMode sceneMode) {
        super(platformInternals);
        logger.info("Building HomeBrewSceneRunner");
        this.sceneMode = sceneMode;
        if (Platform.getInstance().getConfiguration().getInt("throttle") != null) {
            renderbrake = Platform.getInstance().getConfiguration().getInt("throttle");
            logger.debug("setting renderbrake to " + renderbrake);
        }
    }

    public static HomeBrewSceneRunner init(Configuration configuration, HomeBrewRenderer renderer, SceneMode sceneMode) {
        if (instance != null) {
            throw new RuntimeException("already inited");
        }
        // order is important. First platform, then runner.
        PlatformInternals platformInternals = PlatformHomeBrew.init(configuration/*properties*/);
        instance = new HomeBrewSceneRunner(platformInternals, sceneMode);
        ((PlatformHomeBrew) PlatformHomeBrew.getInstance()).renderer = renderer;
        return instance;
    }

    public static HomeBrewSceneRunner getInstance() {
        if (instance == null) {
            throw new RuntimeException("not inited");
        }
        return instance;
    }

    /**
     * 25.2.21: For testing. Isn't this nonsense? Why isn't it part of TestFactory.resetInit()?
     */
    public static void dropInstance() {
        instance = null;
    }

    @Override
    public void runScene(Scene scene) {
        logger.info("runScene");
        logger.debug("java.library.path=" + System.getProperty("java.library.path"));

        this.scene = scene;
        Settings scsettings = new Settings();
        scene.initSettings(scsettings);
        dimension = scene.getPreferredDimension();
        //10.10.18: Wenn es keine Vorgbe gibt, lege ich das fest.
        //
        if (dimension == null) {
            dimension = new Dimension(800, 600);
        }
        float aspect = (float) dimension.width / dimension.height;
        float fov = (scsettings.fov == null) ? Settings.defaultfov : scsettings.fov;
        float near = (scsettings.near == null) ? Settings.defaultnear : scsettings.near;
        float far = (scsettings.far == null) ? Settings.defaultfar : scsettings.far;

        ((PlatformHomeBrew) PlatformHomeBrew.getInstance()).renderer.init(dimension);
        Scene.world = new World();
        //HomeBrewScene openglscene = new HomeBrewScene();
        initAbstract(null, scene);

        if (sceneMode.isClient()) {
            cameraToRender = new HomeBrewPerspectiveCamera(fov, aspect, near, far);
            cameraToRender.setName("Main Camera");
            cameraToRender.getCarrier().setName("Main Camera Carrier");
        }

        scene.setSceneAndCamera(((Platform) PlatformHomeBrew.getInstance()).nativeScene, Scene.world/* ((EngineHelper) Platform.getInstance()).getWorld()*/);
        /*BundleLoaderExceptGwt*/
        SyncBundleLoader.preLoad(scene.getPreInitBundle(), new DefaultResourceReader(), Platform.getInstance().bundleResolver);

        scene.init(sceneMode);

        postInit();

        startRenderloop();

        // Only reached with framelimit
        logger.info("runScene completed");
        ((PlatformHomeBrew) PlatformHomeBrew.getInstance()).renderer.close();
    }

    /**
     * Endless loop. For testing a framelimit can be used and called multiple times.
     */
    public void startRenderloop() {

        HomeBrewRenderer renderer = ((PlatformHomeBrew) PlatformHomeBrew.getInstance()).renderer;
        long firstFrame = getFrameCount();

        // keep looping till the display window isType closed the ESC key isType down
        while (!renderer.userRequestsTerminate()) {
            long starttime = System.currentTimeMillis();


            renderer.renderFrame(this, ((HomeBrewScene) (PlatformHomeBrew.getInstance()).nativeScene).getLights(), cameraToRender);

            //logger.debug("renderFrame took "+(System.currentTimeMillis()-starttime)+" ms.");

            //einbremsen TODO schaltbar. 29.8.16: bischen weniger bremsen
            try {
                //logger.info("sleeping 500");
                Thread.sleep(renderbrake/*200/*2000*/);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //showStatistic();
            if (frameLimit > 0 && getFrameCount() - firstFrame >= frameLimit) {
                break;
            }
        }
        logger.debug("render loop completed " + frameLimit + " frames");
    }
}


