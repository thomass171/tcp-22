package de.yard.threed.javacommon;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.NativeEventBus;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.core.resource.BundleResolver;
import de.yard.threed.core.resource.BundleResolverFactory;


import java.util.HashMap;

/**
 * 27.7.21 Primarily for tests, but also for some tools or desktop apps.
 */
public class SimpleHeadlessPlatformFactory implements PlatformFactory {

    NativeEventBus optionalEventbus = null;
    BundleResolverFactory[] customBundleResolverfactories = null;

    public SimpleHeadlessPlatformFactory() {
    }

    public SimpleHeadlessPlatformFactory(NativeEventBus eventBus) {
        this.optionalEventbus = eventBus;
    }

    public SimpleHeadlessPlatformFactory(NativeEventBus eventBus, BundleResolverFactory... customBundleResolverfactories) {
        this.optionalEventbus = eventBus;
        this.customBundleResolverfactories = customBundleResolverfactories;
    }

    public SimpleHeadlessPlatformFactory(BundleResolverFactory... customBundleResolverfactories) {
        this.customBundleResolverfactories = customBundleResolverfactories;
    }

    @Override
    public PlatformInternals createPlatform(Configuration configuration) {
        PlatformInternals platformInternals = SimpleHeadlessPlatform.init(configuration, optionalEventbus);
        if (customBundleResolverfactories != null) {
            for (BundleResolverFactory brf : customBundleResolverfactories) {
                Platform.getInstance().addBundleResolver(brf.build());
            }
        }
        return platformInternals;
    }
}
