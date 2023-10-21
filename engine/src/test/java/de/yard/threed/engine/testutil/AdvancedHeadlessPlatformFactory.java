package de.yard.threed.engine.testutil;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.NativeEventBus;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.BundleResolverFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;

/**
 * 19.10.23 Intended for tests.
 */
public class AdvancedHeadlessPlatformFactory implements PlatformFactory {

    NativeEventBus optionalEventbus = null;
    BundleResolverFactory[] customBundleResolverfactories = null;

    public AdvancedHeadlessPlatformFactory() {
    }

    public AdvancedHeadlessPlatformFactory(NativeEventBus eventBus) {
        this.optionalEventbus = eventBus;
    }

    public AdvancedHeadlessPlatformFactory(NativeEventBus eventBus, BundleResolverFactory... customBundleResolverfactories) {
        this.optionalEventbus = eventBus;
        this.customBundleResolverfactories = customBundleResolverfactories;
    }

    public AdvancedHeadlessPlatformFactory(BundleResolverFactory... customBundleResolverfactories) {
        this.customBundleResolverfactories = customBundleResolverfactories;
    }

    @Override
    public PlatformInternals createPlatform(Configuration configuration) {
        PlatformInternals platformInternals = AdvancedHeadlessPlatform.init(configuration, optionalEventbus);
        if (customBundleResolverfactories != null) {
            for (BundleResolverFactory brf : customBundleResolverfactories) {
                Platform.getInstance().addBundleResolver(brf.build());
            }
        }
        return platformInternals;
    }
}
