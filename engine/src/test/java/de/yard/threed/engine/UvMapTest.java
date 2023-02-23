package de.yard.threed.engine;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.core.Dimension;

import org.junit.jupiter.api.Test;

/**
 * Created by thomass on 03.11.15.
 */
public class UvMapTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testProportionalUvMap() {
        ProportionalUvMap map = new ProportionalUvMap(new Dimension(100, 60), new Rectangle(15, 10, 30, 20));
        Vector2 uv = map.getUvFromNativeUv(new Vector2(0, 0));
        TestUtils.assertST("0,0", uv, new Vector2(15f / 100f, 10f / 60f));
        uv = map.getUvFromNativeUv(new Vector2(1, 1));
        TestUtils.assertST("1,1", uv, new Vector2(30f / 100f, 20f / 60f));

        map = new ProportionalUvMap(new Dimension(100, 60), new Rectangle(0, 0, 100, 60));
        uv = map.getUvFromNativeUv(new Vector2(0, 0));
        TestUtils.assertST("0,0", uv, new Vector2(0, 0));
        uv = map.getUvFromNativeUv(new Vector2(1, 1));
        TestUtils.assertST("1,1", uv, new Vector2(1, 1));

        map = new ProportionalUvMap();
        uv = map.getUvFromNativeUv(new Vector2(0, 0));
        TestUtils.assertST("0,0", uv, new Vector2(0, 0));
        uv = map.getUvFromNativeUv(new Vector2(1, 1));
        TestUtils.assertST("1,1", uv, new Vector2(1, 1));
    }

    /**
     * Skizze 29
     */
    @Test
    public void testIconPosition() {
        Icon iconPosition = Icon.ICON_POSITION;
        UvMap1 uvmap = iconPosition.getUvMap();
        Vector2 uv = uvmap.getUvFromNativeUv(new Vector2(0,0));
        TestUtils.assertST( uv, new Vector2(0.8125, 0.9375));
        uv = uvmap.getUvFromNativeUv(new Vector2(1,1));
        TestUtils.assertST( uv, new Vector2(0.875, 1));

    }
}
