package de.yard.threed.core;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import de.yard.threed.core.testutil.TestFactory;
import de.yard.threed.core.testutil.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * In scenery gibt es auch noch Tests.
 */
public class OutlineBuilderTest {
    static Platform platform = TestFactory.initPlatformForTest(new PlatformFactoryTestingCore(), null);

    /**
     * Skizze 11c
     */
    @Test
    public void testSimpleOutline() {
        double offset = 3;
        double offset45 = Math.sqrt(offset * offset / 2);
        List<Vector2> vlist = new ArrayList<Vector2>();
        vlist.add(new Vector2(20, 30));
        vlist.add(new Vector2(25, 25));
        vlist.add(new Vector2(25, 30));
        vlist.add(new Vector2(25, 35));
        vlist.add(new Vector2(30, 40));
        vlist.add(new Vector2(35, 45));
        vlist.add(new Vector2(40, 40));
        vlist.add(new Vector2(45, 35));

        //rechts
        List<Vector2> outline = OutlineBuilder.getOutline(vlist, 3);
        Assertions.assertEquals(vlist.size(), outline.size(), "size");
        TestUtils.assertVector2(new Vector2(20 - offset45, 30 - offset45), outline.get(0));
        TestUtils.assertVector2(new Vector2(26.14805029709527, 22.228361402466142), outline.get(1));
        TestUtils.assertVector2(new Vector2(27.771638597533858, 33.85194970290473), outline.get(3));
        TestUtils.assertVector2(new Vector2(45 - offset45, 35 - +offset45), outline.get(7));

        //links
        outline = OutlineBuilder.getOutline(vlist, -3);
        Assertions.assertEquals(vlist.size(), outline.size(), "size");
        TestUtils.assertVector2(new Vector2(20 + offset45, 30 + offset45), outline.get(0));
        TestUtils.assertVector2(new Vector2(35, 45 + offset), outline.get(5));
        TestUtils.assertVector2(new Vector2(45 + offset45, 35 + offset45), outline.get(7));


    }
}
