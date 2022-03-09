package de.yard.threed.engine.testutil;

import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.engine.Scene;

import de.yard.threed.engine.SceneAnimationController;
import de.yard.threed.engine.World;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import de.yard.threed.outofbrowser.SyncBundleLoader;

import java.util.HashMap;
import java.util.Properties;

/**
 * Fuer die Initilisierung der Platform zum Test.
 *
 * <p>
 * <p/>
 * Created by thomass on 10.04.15.
 */
public class TestFactory {

    public static Platform initPlatformForTest(String[] bundlelist, PlatformFactory platformFactory) {
        return initPlatformForTest(bundlelist, platformFactory, (InitMethod)null);
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
        SceneRunnerForTesting.init(properties, platformFactory, sceneIinitMethod, bundlelist);
        Platform pl = Platform.getInstance();

        return pl;
    }

    /**
     *
     */
    public static Platform initPlatformForTest(String[] bundlelist, PlatformFactory platformFactory, HashMap<String, String> properties) {

        resetInit();

        //7.7.21: Wie "im echten Leben" vor der Platform immer einen SceneRunner anlegen.
        SceneRunnerForTesting.init(properties, platformFactory, null, bundlelist);
        Platform pl = Platform.getInstance();

        return pl;
    }


    public static void loadBundleSync(String bundlename) {
        loadBundleSync(bundlename, null, false);
    }

    public static void loadBundleSync(String bundlename, String registername, boolean delayed) {

        // Doppelt laden vermeiden, es sei denn es soll unter anderem Namen registiriert werrden
        if (BundleRegistry.getBundle(bundlename) == null || registername != null) {
            /*BundleLoaderExceptGwt*/
            ResourcePath bundlebasedir = BundleResolver.resolveBundle(bundlename, Platform.getInstance().bundleResolver);
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
            },*/ new DefaultResourceReader(), bundlebasedir);
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
        // Try to remove old system properties. Might be difficult to find all, so rely on prefix
        Properties properties = System.getProperties();
        for (String p :properties.stringPropertyNames()) {
            if (p.startsWith(SimpleHeadlessPlatform.PROPERTY_PREFIX)) {
                System.clearProperty(p);
            }
        }
        if (Platform.getInstance() != null) {
            BundleRegistry.clear();
            SystemManager.reset();
            Observer.reset();
            VrInstance.reset();
        }
        if (AbstractSceneRunner.instance != null) {

            //30.9.19: Alte Bundle und Provider loswerden
            SceneAnimationController.instance = null;
            //4.5.20 das muss jetzt doch auch sein.
            AbstractSceneRunner.instance = null;
        }
    }
}
