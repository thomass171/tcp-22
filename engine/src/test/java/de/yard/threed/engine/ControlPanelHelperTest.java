package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.gui.ControlPanelHelper;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ControlPanelHelperTest {

    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new SimpleHeadlessPlatformFactory());

    @Test
    void testCalcYoffsetForRow(){
        assertEquals(-5.0, ControlPanelHelper.calcYoffsetForRow(0,2, 10));
        assertEquals(5.0, ControlPanelHelper.calcYoffsetForRow(1,2, 10));

        assertEquals(-10.0, ControlPanelHelper.calcYoffsetForRow(0,3, 10));
        assertEquals(0.0, ControlPanelHelper.calcYoffsetForRow(1,3, 10));
        assertEquals(10.0, ControlPanelHelper.calcYoffsetForRow(2,3, 10));
    }
}
