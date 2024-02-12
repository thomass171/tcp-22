package de.yard.threed.engine.testutil;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleFactory;
import de.yard.threed.core.resource.BundleLoadDelegate;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.testutil.TestBundle;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.Observer;
import de.yard.threed.engine.platform.PlatformBundleLoader;
import de.yard.threed.engine.vr.VrInstance;
import de.yard.threed.javacommon.ConfigurationByEnv;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.BundleRegistry;

import de.yard.threed.engine.SceneAnimationController;
import de.yard.threed.engine.ecs.SystemManager;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.core.InitMethod;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.javacommon.JavaWebClient;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.outofbrowser.FileSystemBundleResourceLoader;

import java.util.HashMap;
import java.util.Properties;
import java.util.function.BooleanSupplier;

/**
 * Has access to ConfigurationByEnv, other than CoreTestFactory.
 *
 * <p>
 * <p/>
 * Created by thomass on 10.04.15.
 */
public class EngineTestFactory {

    public static Platform initPlatformForTest(String[] bundlelist, PlatformFactory platformFactory) {
        return initPlatformForTest(bundlelist, platformFactory, (InitMethod) null, ConfigurationByEnv.buildDefaultConfigurationWithEnv(new HashMap<>()));
    }

    /**
     *
     */
    public static Platform initPlatformForTest(String[] bundlelist, PlatformFactory platformFactory, InitMethod sceneIinitMethod, Configuration configuration) {

        HashMap<String, String> properties = new HashMap<String, String>();

        //properties.put("BUNDLEDIR", Util.getHostdir() + "/bundles");

        //7.7.21 Reset aus EngineHelper (geht nicht beim ersten mal wegen fehlender Platform)
        resetInit();

        //7.7.21: Wie "im echten Leben" vor der Platform immer einen SceneRunner anlegen.
        SceneRunnerForTesting.init(configuration, platformFactory, sceneIinitMethod, bundlelist);
        Platform pl = Platform.getInstance();

        return pl;
    }

    /**
     *
     */
    /*6.2.23 use above public static Platform initPlatformForTest(String[] bundlelist, PlatformFactory platformFactory, Configuration configuration) {

        resetInit();

        //7.7.21: Wie "im echten Leben" vor der Platform immer einen SceneRunner anlegen.
        SceneRunnerForTesting.init(configuration, platformFactory, null, bundlelist);
        Platform pl = Platform.getInstance();

        return pl;
    }*/

    /**
     * 22.12.23
     */
    public static void loadBundleAndWait(String bundlename) {
        AbstractSceneRunner.getInstance().loadBundle(bundlename, new BundleLoadDelegate() {
            @Override
            public void bundleLoad(Bundle bundle) {
                BundleRegistry.registerBundle(bundlename, bundle);
            }
        });
        try {
            TestUtils.waitUntil(new BooleanSupplier() {
                @Override
                public boolean getAsBoolean() {
                    return BundleRegistry.getBundle(bundlename) != null;
                }
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public static void loadBundleSync(String bundlename) {
        loadBundleAndWait(bundlename);
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
        // 6.2.23 deprecated since using configuration
        Properties properties = System.getProperties();
        for (String p : properties.stringPropertyNames()) {
            if (p.startsWith(SimpleHeadlessPlatform.PROPERTY_PREFIX)) {
                System.clearProperty(p);
            }
        }
        SystemManager.reset();
        if (Platform.getInstance() != null) {
            // the following need the logger
            BundleRegistry.clear();
            Observer.reset();
            VrInstance.reset();
        }
        if (AbstractSceneRunner.instance != null) {

            //30.9.19: Alte Bundle und Provider loswerden
            SceneAnimationController.instance = null;
            //4.5.20 das muss jetzt doch auch sein.
            AbstractSceneRunner.instance = null;
        }
        JavaWebClient.close();
    }

    /**
     * Loaded from local module working dir.
     */
    public static void addTestResourcesBundle() {
        if (BundleRegistry.getBundle("test-resources") == null) {
            ResourcePath bundlebasedir = new ResourcePath("src/test/resources");
            //15.12.23 SyncBundleLoader.loadBundleSyncInternal("test-resources",  /*13.12.23 null,*/ false, new DefaultResourceReader(), bundlebasedir);
            // bundleLoader in SceneRunner is hidden. So use a new one for this special case.
            PlatformBundleLoader bundleLoader = new PlatformBundleLoader();
            bundleLoader.setBundleFactory(new BundleFactory() {
                @Override
                public Bundle createBundle(String name, String[] directory, String basepath) {
                    return new TestBundle(name, directory, basepath);
                }
            });
            bundleLoader.loadBundle("test-resources", new BundleLoadDelegate() {
                @Override
                public void bundleLoad(Bundle bundle) {
                    BundleRegistry.registerBundle("test-resources", bundle);
                }
            }, new FileSystemBundleResourceLoader(bundlebasedir));
        }
    }


    /**
     * Loaded from local module working/project dir.
     * Also expects a directory.txt?
     */
    public static void addBundleFromProjectDirectory(String bundleName, String subdir) {
        if (BundleRegistry.getBundle(bundleName) == null) {
            ResourcePath bundlebasedir = new ResourcePath("../" + subdir);
            /*String e = SyncBundleLoader.loadBundleSyncInternal(bundleName, null,
                    false, new DefaultResourceReader(), bundlebasedir);*/
            // bundleLoader in SceneRunner is hidden. So use a new one for this special case.
            PlatformBundleLoader bundleLoader = new PlatformBundleLoader();
            bundleLoader.loadBundle(bundleName, new BundleLoadDelegate() {
                @Override
                public void bundleLoad(Bundle bundle) {
                    BundleRegistry.registerBundle(bundleName, bundle);
                }
            }, new FileSystemBundleResourceLoader(bundlebasedir));
        }
    }
}
