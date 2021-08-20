package de.yard.threed.engine.test.testutil;


import de.yard.threed.core.Degree;
import de.yard.threed.engine.Shape;
import de.yard.threed.engine.geometry.ShapeGeometry;

/**
 * Date: 31.05.14
 */
public class ShapedGeometryTestHelper {
    /**
     * ist nicht closed. Einfach ein umgedrehtes "V".
     *
     * @return
     */
    public static ShapeGeometry buildRoof() {
        Shape shape = new Shape();
        shape.addPoint(-1f, 0f);
        shape.addPoint(0, 1f, true);
        shape.addPoint(1f, 0f);
        return new ShapeGeometry(shape, 1, 1);
    }

    public static ShapeGeometry buildRoofUnEdged() {
        Shape shape = new Shape();
        shape.addPoint(-1f, 0f);
        shape.addPoint(0, 1f);
        shape.addPoint(1f, 0f);
        return new ShapeGeometry(shape, 1, 1);
    }

    /**
     * Wie ein nach links gedrehtes grosses U.
     * Defaultmaessig ohne Kanten.
     * 02.12.16: Liefert nur noch den Shape, um vielfältiger testen zu können.
     * @return
     */
    public static Shape buildULike() {
        float width = 3, thickness = 0.5f;
        Shape shape = new Shape();
        shape.addTopLine(width, thickness / 2);
        shape.addSemiSymetricArc(new Degree(0));
        shape.addBottomLine(width);
        //ShapeGeometry sg = new ShapeGeometry(shape, 2, 1);
        return shape;
    }
    
    /**
     * Baut eine etwas kantige Kugel mit vertikal 4 und horizontal 16 Segmenten
     *
     * @return
     */
    public static ShapeGeometry buildRoughShere() {
        ShapeGeometry g = ShapeGeometry.buildSphere(4,16,new Degree(360));
        return g;
    }


}
