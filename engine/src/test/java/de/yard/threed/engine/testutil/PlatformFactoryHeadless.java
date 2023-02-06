package de.yard.threed.engine.testutil;

import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.javacommon.SimpleHeadlessPlatform;

import java.util.HashMap;

/**
 * General purpose platform for unit tests. 6.7.21: Uses PlatformFactoryHeadless instead of HomeBrew.
 * 26.7.21: Wird das noch gebraucht? Es gibt doch SimpleHeadlessPlatformFactory.
 * 1.3.22 set to deprecated
 */
@Deprecated
public class PlatformFactoryHeadless implements PlatformFactory {
    @Override
    public /*Engine*/PlatformInternals createPlatform(Configuration configuration) {
        // resetInit() wird im init() gemacht.
        /*Engine*/PlatformInternals pl ; /*MA36 = (PlatformHomeBrew) PlatformHomeBrew.init(properties);
        pl.setEventBus(new SimpleEventBusForTesting());
        ((PlatformHomeBrew) pl).logfactory = new TestLogFactory();
        // 12.6.17: In Tests aync laden
        ((PlatformHomeBrew) pl).resourcemanager = new TestResourceManager();
        OpenGlContext.init(new GlImplDummyForTests());
        pl.getEventBus().clear();

        //25.4.20 ohne renderer no material. Obwohl, was wird damit eigentlich getestet?
        ((PlatformHomeBrew) pl).setRenderer(OpenGlContext.getGlContext());
*/
         pl = SimpleHeadlessPlatform.init(configuration);
        // 12.6.17: In Tests aync laden
        //5.7.21 Hmm ((PlatformHomeBrew) pl).resourcemanager = new TestResourceManager();
        Platform.getInstance().getEventBus().clear();


        return pl;
    }
}
