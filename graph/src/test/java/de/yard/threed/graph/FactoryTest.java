package de.yard.threed.graph;


import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Shape;
import de.yard.threed.engine.avatar.VehicleFactory;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.testutil.TestFactory;
import de.yard.threed.javacommon.SimpleHeadlessPlatformFactory;
import de.yard.threed.core.testutil.TestUtil;
import org.junit.Test;

/**
 * Created by thomass on 28.11.16.
 */
public class FactoryTest {
    //static Platform platform = TestFactory.initPlatformForTest(false,false,null,true);
    static Platform platform = TestFactory.initPlatformForTest( new String[] {"engine","data"}, new SimpleHeadlessPlatformFactory());

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
        ShapeGeometry sg = RailingFactory.buildRailGeometry(new Degree(30),0.36f,4);
        int vertices = (4 + 1)*railshape.getPoints().size();
        TestUtil.assertEquals("Anzahl Vertices", vertices, sg.getVertices().size());
        // Der Shape beginnt links unten und dann CW rum
        //TestUtil.assertVector3( new Vector3(-width/2+radius, -height/2, depth/2),sg.getVertices().get(0));
        // Nur eine Surface weil es eine glatte Flaeche ohne Front/Back ist
        TestUtil.assertEquals("Anzahl Surfaces", 1, sg.getSurfacesCount());
        TestUtil.assertEquals("Anzahl Surfaces normal", 1, sg.getSurfaces(0).size());
       
    }

}
