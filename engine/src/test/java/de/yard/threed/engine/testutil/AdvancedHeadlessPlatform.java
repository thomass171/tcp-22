package de.yard.threed.engine.testutil;

import de.yard.threed.core.ModelBuildDelegate;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeEventBus;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.platform.common.ModelLoader;
import de.yard.threed.javacommon.DefaultResourceReader;
import de.yard.threed.javacommon.JALog;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;
import de.yard.threed.outofbrowser.SimpleBundleResolver;


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
        instance.bundleResolver.addAll(SimpleBundleResolver.buildFromPath(configuration.getString("ADDITIONALBUNDLE"), resourceReader));

        logger.info("AdvancedHeadlessPlatform created");
        return platformInternals;
    }

    @Override
    public void buildNativeModelPlain(ResourceLoader resourceLoader, ResourcePath opttexturepath, ModelBuildDelegate delegate, int options) {

        //logger.debug("buildNativeModel "+filename+", delegateid="+delegateid);
        ModelLoader.buildModelFromBundle(resourceLoader, opttexturepath, options, delegate);
    }
}
