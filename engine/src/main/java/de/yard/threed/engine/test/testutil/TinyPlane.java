package de.yard.threed.engine.test.testutil;


import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.UvMap1;

/**
 * Date: 31.05.14
 */
public class TinyPlane {
    static final float xplanesize = 1;
    static final float zplanesize = 1;
    static final int xsegments = 1;
    static final int zsegments = 1;

    public static ShapeGeometry buildTinyPlane(UvMap1 uvmap) {
        return ShapeGeometry.buildPlane(xplanesize, zplanesize, xsegments, zsegments,uvmap);
    }

    public static ShapeGeometry buildTinyPlane() {

        return ShapeGeometry.buildPlane(xplanesize, zplanesize, xsegments, zsegments);
    }
}
