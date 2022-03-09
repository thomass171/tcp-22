package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.gui.PanelGrid;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.jupiter.api.Test;

public class PanelGridTest {

    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testSimple() {

        PanelGrid panelGrid = new PanelGrid(0.6, 0.1, 3, new double[]{0.4, 0.2});

        TestUtil.assertFloat("midx[0]", -0.1, panelGrid.midx[0]);
        TestUtil.assertFloat("midy[0]", -0.15+0.1/2, panelGrid.midy[0]);

        TestUtil.assertFloat("midx[1]", 0.2, panelGrid.midx[1]);
        TestUtil.assertFloat("midy[1]", 0, panelGrid.midy[1]);

        TestUtil.assertFloat("midy[2]", 0.15-0.1/2, panelGrid.midy[2]);

    }
}
