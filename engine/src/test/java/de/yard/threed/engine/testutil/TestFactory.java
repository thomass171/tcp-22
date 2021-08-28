package de.yard.threed.engine.testutil;

import de.yard.threed.javacommon.Util;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.outofbrowser.AsyncBundleLoader;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.Scene;

import de.yard.threed.engine.SceneAnimationController;
import de.yard.threed.engine.World;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AsyncJobCallback;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.core.InitMethod;
import de.yard.threed.outofbrowser.SyncBundleLoader;
import org.junit.Assert;

import java.util.HashMap;

/**
 * Fuer die Initilisierung der Platform zum Test.
 *
 * <p>
 * <p/>
 * Created by thomass on 10.04.15.
 */
public class TestFactory {

    public static Platform initPlatformForTest(String[] bundlelist, PlatformFactory platformFactory) {
        return initPlatformForTest(bundlelist, platformFactory, null);
    }

    /**
     *
     */
    public static Platform initPlatformForTest(String[] bundlelist, PlatformFactory platformFactory, InitMethod sceneIinitMethod) {

        HashMap<String, String> properties = new HashMap<String, String>();

        //properties.put("BUNDLEDIR", Util.getHostdir() + "/bundles");

        //7.7.21 Reset aus EngineHelper (geht nicht beim ersten mal wegen fehlender Platform)
        resetInit();

        //7.7.21: Wie "im echten Leben" vor der Platform immer einen SceneRunner anlegen.
        SceneRunnerForTesting scenerunner = SceneRunnerForTesting.init(properties, platformFactory);
        Platform pl = Platform.getInstance();

        Scene scene = null;

        scene = new Scene() {
            @Override
            public void init() {
                if (sceneIinitMethod != null) {
                    sceneIinitMethod.init();
                }
            }
        };

        scenerunner.initAbstract(null/*new DummyScene()*/, scene);

        scene.setSceneAndCamera(pl.getScene()/*AbstractSceneRunner.getInstance().scene*/, new World());

        // 13.4.17: Die Bundle laden. Ausnahmesweise synchron wegen Test. Doof vor allem bei Einzeltests weil es so lange braucht.
        if (bundlelist != null) {
            for (String bundlename : bundlelist) {
                loadBundleSync(bundlename);
            }
        }

        //4.10.17: Auch eine Scene anlegen, um einen Node tree zu haben.
        //27.3.20: evtl. ein init aus dem Test.
        //MA36: Woher kommt denn der Tree? Und dann aber auch eine World. Das ganze mal nur mit EngineHelper.
        // MA36: TODO brauchts denn eine EngineHelper hier? Fuer world wohl schon? Aber die soll mal die Platform selber anlegen
        /*Scene scene =null;
        if (pl instanceof EngineHelper/*MA36 HomeBrew* /) {
            scene = new Scene() {
                @Override
                public void init() {
                    if (sceneIinitMethod != null) {
                        sceneIinitMethod.init();
                    }
                }
            };
            //24.3.18: Lieber bevorzugt immer neu anlegen, z.B. um alte Nodes loszuwerden.
            //if (RunnerHelper.getInstance() == null) {
            //MA36 OpenGlScene os = new OpenGlScene();
            //AbstractSceneRunner.init(os, JAResourceManager.getInstance(), scene);
            //MA36 new SceneRunnerForTesting().initAbstract(null/*MA36os* /,JAResourceManager.getInstance(), scene);

        //3.4.20 nach oben OpenGlContext.init(new GlImplDummyForTests());

        //Auch eine world, damit es den Tree ab root gibt.
        // Erst zum Schluss eine Node, weil die auch ein Event verschickt.
        //MA36 es gibt aber keinen Tree,oder? Und keine Camera.
            /*((Platform)pl).setWorld(new World());
            OpenGlPerspectiveCamera cam = new OpenGlPerspectiveCamera(new Dimension(20, 20));
            ((Platform) Platform.getInstance()).addCamera(cam);*  /
            scene.setSceneAndCamera(AbstractSceneRunner.getInstance().scene, ((EngineHelper)pl).getWorld());
        }*/

        //4.5.20: Bundle und anderes nur bei nicht trivialer Platform
        if (AbstractSceneRunner.getInstance() != null) {

            // 10.4.21: MannMannMann: das ist hier jetzt so reingefriemelt.
            TestHelper.cleanupAsync();

            // 13.4.17: Die Bundle laden. Ausnahmesweise synchron wegen Test. Doof vor allem bei Einzeltests weil es so lange braucht.
           /* if (bundlelist != null) {
                for (String bundlename : bundlelist) {
                    loadBundleSync(bundlename);
                }
            }*/

            //27.3.20 dann doch vorher auch den Sceneinit fuer ein paar Systems
            scene.init();
            // 9.1.17: Der Vollstaendigkeithalber auch der postinit
            AbstractSceneRunner.getInstance().postInit();
        }

        /*MA31 verschoben nach FgTestFactory
        if (withfg) {
            //30.9.19: Aber irgendeine Art init brauchts doch (z.B. wegen TileMgr, proptree). Und die beiden Modules sind ja nun mal da.
            //wird teilweise in einzelnen Tests gemacht. Das ist aber inkonsistent.
            //MA31 TODO unresolved dependencies
            Util.notyet();
            //FlightGearModuleBasic.init(null, null);
            //FlightGearModuleScenery.init(false);

        }*/

        return pl;
    }


    public static void loadBundleSync(String bundlename) {
        loadBundleSync(bundlename, null, false);
    }

    public static void loadBundleSync(String bundlename, String registername, boolean delayed) {

        // Doppelt laden vermeiden, es sei denn es soll unter anderem Namen registiriert werrden
        if (BundleRegistry.getBundle(bundlename) == null || registername != null) {
            /*BundleLoaderExceptGwt*/
            String e = SyncBundleLoader.loadBundleSyncInternal(bundlename, registername, delayed,/* new AsyncJobCallback() {
                @Override
                public void onSuccess() {
                    // was sync, nothing to do:
                }

                @Override
                public void onFailure(String e) {
                    // das mal als Fehler werten
                    Assert.fail("Bundle failed:" + bundlename);
                }
            },*/ new DefaultResourceReader());
            if (e != null) {
                // das mal als Fehler werten
                Assert.fail("Bundle failed:" + bundlename);
            }
        }
    }

    /**
     * Aus EngineHelper hier hin.
     * 4.5.20 Um jetzt mal so resetter unterzubringen(Um das mal zu buendeln.)
     * Geht aber erst, wenn instance gesetzt ist(nicht im constructor).
     * Bzw, wenn es schon mal eine Platform gab, also nicht beim ersten mal.
     * Darf nur aus Tests verwendet werden.
     */
    public static void resetInit() {
        if (AbstractSceneRunner.instance != null) {

            //30.9.19: Alte Bundle und Provider loswerden
            BundleRegistry.clear();
            SystemManager.reset();
            SceneAnimationController.instance = null;
            //4.5.20 das muss jetzt doch auch sein.
            AbstractSceneRunner.instance = null;
        }
    }
}
