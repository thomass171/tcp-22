package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Vector2;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.TestFactory;

import de.yard.threed.core.testutil.TestUtil;
import org.junit.Test;



/**
 *
 */
public class ShapeTest {
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine"}, new PlatformFactoryHeadless());

    @Test
    public void testSimpleArcLinksNachUnten() {
        Shape shape = new Shape();
        shape.addPoint(new Vector2(0f, 1.0f));
        shape.addArc(new Vector2(0, 0), new Degree(90), 2);

        TestUtil.assertEquals("Anzahl Punkte", 3, shape.getPoints().size());
        TestUtil.assertVector2("ersterKreispunkt", new Vector2(-1, 0), shape.getPoints().get(1));
        TestUtil.assertVector2("zweiterKreispunkt", new Vector2(0, -1.0f), shape.getPoints().get(2));
    }

    @Test
    public void testSimpleArcRechtsNachOben() {
        Shape shape = new Shape();
        shape.addPoint(new Vector2(0f, -1.0f));
        shape.addArc(new Vector2(0, 0), new Degree(90), 2);

        TestUtil.assertEquals("Anzahl Punkte", 3, shape.getPoints().size());
        TestUtil.assertVector2("ersterKreispunkt", new Vector2(1, 0), shape.getPoints().get(1));
        TestUtil.assertVector2("zweiterKreispunkt", new Vector2(0, 1.0f), shape.getPoints().get(2));
    }


}
