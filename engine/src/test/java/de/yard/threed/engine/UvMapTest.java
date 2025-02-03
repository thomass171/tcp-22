package de.yard.threed.engine;

import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.geometry.ProportionalUvMap;
import de.yard.threed.core.geometry.Rectangle;
import de.yard.threed.core.geometry.UvMap1;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.gui.Icon;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.core.Dimension;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Also for ProportionalUvMap.
 * Created by thomass on 03.11.15.
 */
public class UvMapTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());

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
        Vector2 uv = uvmap.getUvFromNativeUv(new Vector2(0, 0));
        TestUtils.assertST(uv, new Vector2(0.8125, 0.9375));
        uv = uvmap.getUvFromNativeUv(new Vector2(1, 1));
        TestUtils.assertST(uv, new Vector2(0.875, 1));

    }

    @Test
    public void testBinIconSet() {
        double elUvSize = 1.0 / 16;
        ProportionalUvMap uvMap = ProportionalUvMap.buildForGridElement(16, 1, 11, false);
        Vector2 uv = uvMap.getUvFromNativeUv(new Vector2(0, 0));
        TestUtils.assertST(new Vector2(elUvSize, 11 * elUvSize), uv);
        uv = uvMap.getUvFromNativeUv(new Vector2(1, 1));
        TestUtils.assertST(new Vector2(elUvSize + elUvSize, 11 * elUvSize + elUvSize), uv);
    }

    @Test
    public void testBTranslationInIconSet() {
        Matrix3 textureMatrix = Texture.getTextureMatrixForGridElement(16, -9999, 1, 11);
        // the expected values are from real shader debug/test
        assertEquals(0.0625, textureMatrix.e13);
        assertEquals(0.0625 * 11, textureMatrix.e23);

    }

}
