package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 *
 */
public class ShapeTest {
    static Platform platform = TestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testSimpleArcLinksNachUnten() {
        Shape shape = new Shape();
        shape.addPoint(new Vector2(0f, 1.0f));
        shape.addArc(new Vector2(0, 0), new Degree(90), 2);

        Assertions.assertEquals(3, shape.getPoints().size(), "Anzahl Punkte");
        TestUtils.assertVector2(new Vector2(-1, 0), shape.getPoints().get(1), "ersterKreispunkt");
        TestUtils.assertVector2(new Vector2(0, -1.0f), shape.getPoints().get(2), "zweiterKreispunkt");
    }

    @Test
    public void testSimpleArcRechtsNachOben() {
        Shape shape = new Shape();
        shape.addPoint(new Vector2(0f, -1.0f));
        shape.addArc(new Vector2(0, 0), new Degree(90), 2);

        Assertions.assertEquals(3, shape.getPoints().size(), "Anzahl Punkte");
        TestUtils.assertVector2(new Vector2(1, 0), shape.getPoints().get(1), "ersterKreispunkt");
        TestUtils.assertVector2(new Vector2(0, 1.0f), shape.getPoints().get(2), "zweiterKreispunkt");
    }


}
