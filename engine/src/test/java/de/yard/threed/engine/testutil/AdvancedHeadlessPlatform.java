package de.yard.threed.engine.testutil;

import de.yard.threed.core.ModelBuildDelegate;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeEventBus;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.AsyncHelper;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.javacommon.JALog;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.outofbrowser.AsyncBundleLoader;
import de.yard.threed.outofbrowser.SimpleBundleResolver;
import de.yard.threed.outofbrowser.SyncBundleLoader;

/**
 * 19.10.23
 * SimpleHeadlessPlatform has no access to 'engine', so no AsyncHelper etc. This is an extension
 * that closes that gap. Intended for tests.
 */
public class AdvancedHeadlessPlatform extends SimpleHeadlessPlatform {

    static Log logger = new JALog(AdvancedHeadlessPlatform.class);

    public AdvancedHeadlessPlatform(NativeEventBus optionalEventbus, Configuration configuration) {
        super(optionalEventbus,configuration);
    }

    public static PlatformInternals init(Configuration configuration, NativeEventBus eventbus) {

        instance = new AdvancedHeadlessPlatform(eventbus, configuration);
        AdvancedHeadlessPlatform shpInstance = (AdvancedHeadlessPlatform) instance;
        PlatformInternals platformInternals = new PlatformInternals();
        DefaultResourceReader resourceReader = new DefaultResourceReader();
        instance.bundleResolver.add(new SimpleBundleResolver(shpInstance.hostdir + "/bundles", resourceReader));
        instance.bundleResolver.addAll(SyncBundleLoader.buildFromPath(configuration.getString("ADDITIONALBUNDLE"), resourceReader));
        instance.bundleLoader = new AsyncBundleLoader(resourceReader);

        logger.info("AdvancedHeadlessPlatform created");
        return platformInternals;
    }

    @Override
    public void buildNativeModelPlain(BundleResource filename, ResourcePath opttexturepath, ModelBuildDelegate delegate, int options) {
        int delegateid = AbstractSceneRunner.getInstance().invokeLater(delegate);

        //logger.debug("buildNativeModel "+filename+", delegateid="+delegateid);
        AsyncHelper.asyncModelBuild(filename, opttexturepath, options, delegateid);
    }
}
