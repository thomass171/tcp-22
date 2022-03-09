package de.yard.threed.engine;

import de.yard.threed.core.Dimension;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import de.yard.threed.engine.platform.common.Settings;
import org.junit.jupiter.api.Test;

public class ObserverTest {

    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testFinetune() {

        Dimension d = new Dimension(400, 200);
        Camera camera = new PerspectiveCamera(Platform.getInstance().buildPerspectiveCamera(Settings.defaultfov, ((double) d.width) / (double) d.height, Settings.defaultnear, Settings.defaultfar)/* TODO MA36 new OpenGlPerspectiveCamera(*/);

        Observer observer = Observer.buildForCamera(camera);
        observer.fineTune(true);
        TestUtil.assertVector3(new Vector3(0, Observer.offsetstep, 0), observer.getEffectivePosition());
    }
}
