package de.yard.threed.engine;

import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.testutil.TestUtils;
import de.yard.threed.engine.testutil.PlatformFactoryHeadless;
import de.yard.threed.engine.testutil.EngineTestFactory;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 *
 */
public class ShapeFactoryTest {
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new PlatformFactoryHeadless());

    /**
     * von rechts 90 Grad nach oben
     */
    @Test
    public void testbuildArc() {
        Shape shape = ShapeFactory.buildArc(new Degree(0), new Degree(90), 1, 2);

        Assertions.assertEquals(3, shape.getPoints().size(), "Anzahl Punkte");
        TestUtils.assertVector2(new Vector2(1, 0), shape.getPoints().get(0), "ersterKreispunkt");
        TestUtils.assertVector2(new Vector2(0.70710677f, 0.70710677f), shape.getPoints().get(1), "zweiterKreispunkt");
        TestUtils.assertVector2(new Vector2(0, 1.0f), shape.getPoints().get(2), "dritterKreispunkt");
    }

    /**
     * von unten 90 Grad nach rechts
     */
    @Test
    public void testbuildArcUntenRechts() {
        Shape shape = ShapeFactory.buildArc(new Degree(-90), new Degree(90), 1, 2);

        Assertions.assertEquals(3, shape.getPoints().size(), "Anzahl Punkte");
        TestUtils.assertVector2(new Vector2(0, -1f), shape.getPoints().get(0), "ersterKreispunkt");
        TestUtils.assertVector2(new Vector2(0.70710677f, -0.70710677f), shape.getPoints().get(1), "zweiterKreispunkt");
        TestUtils.assertVector2(new Vector2(1, 0f), shape.getPoints().get(2), "dritterKreispunkt");
    }

    /**
     * von oben 180 Grad rechts nach unten. Wie für den Halbkreis einer Kugel
     */
    @Test
    public void testbuildArcObenUnten() {
        Shape shape = ShapeFactory.buildArc(new Degree(90), new Degree(-180), 1, 2);

        Assertions.assertEquals(3, shape.getPoints().size(), "Anzahl Punkte");
        TestUtils.assertVector2(new Vector2(0, 1f), shape.getPoints().get(0), "ersterKreispunkt");
        TestUtils.assertVector2(new Vector2(1, 0), shape.getPoints().get(1), "zweiterKreispunkt");
        TestUtils.assertVector2(new Vector2(0, -1f), shape.getPoints().get(2), "dritterKreispunkt");
    }

    /**
     * von links 180 Grad oben nach rechtsunten. Wie für den Halbkreis einer Kugel
     */
    @Test
    public void testbuildArcLinksOben() {
        Shape shape = ShapeFactory.buildArc(new Degree(180), new Degree(-180), 1, 2);

        Assertions.assertEquals(3, shape.getPoints().size(), "Anzahl Punkte");
        TestUtils.assertVector2(new Vector2(-1, 0f), shape.getPoints().get(0), "ersterKreispunkt");
        TestUtils.assertVector2(new Vector2(0, 1), shape.getPoints().get(1), "zweiterKreispunkt");
        TestUtils.assertVector2(new Vector2(1, 0f), shape.getPoints().get(2), "dritterKreispunkt");
    }
}
