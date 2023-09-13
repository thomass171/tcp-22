package de.yard.threed.graph;


import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.geometry.Shape;
import de.yard.threed.engine.avatar.VehicleFactory;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.testutil.EngineTestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by thomass on 28.11.16.
 */
public class FactoryTest {
    //static Platform platform = TestFactory.initPlatformForTest(false,false,null,true);
    static Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine", "data"}, new SimpleHeadlessPlatformFactory());

    /**
     * gebogenes Gleisstück
     */
    @Test
    public void testArcedRail() {
        float width = 0.5f;
        float height = 0.2f;
        float depth = 0.2f;
        float radius = 0.06f;
        Shape railshape = RailingFactory.buildRailShape(VehicleFactory.wheelwidth);
        ShapeGeometry sg = RailingFactory.buildRailGeometry(new Degree(30), 0.36f, 4);
        int vertices = (4 + 1) * railshape.getPoints().size();
        Assertions.assertEquals(vertices, sg.getVertices().size(), "Anzahl Vertices");
        // Der Shape beginnt links unten und dann CW rum
        //TestUtil.assertVector3( new Vector3(-width/2+radius, -height/2, depth/2),sg.getVertices().get(0));
        // Nur eine Surface weil es eine glatte Flaeche ohne Front/Back ist
        Assertions.assertEquals(1, sg.getSurfacesCount(), "Anzahl Surfaces");
        Assertions.assertEquals(1, sg.getSurfaces(0).size(), "Anzahl Surfaces normal");

    }

}
