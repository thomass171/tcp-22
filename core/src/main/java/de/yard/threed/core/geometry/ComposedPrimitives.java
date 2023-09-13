package de.yard.threed.core.geometry;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;

import de.yard.threed.core.MathUtil2;

/**
 * Eigentlich keine Primitives, sondern Geometrien, die sich aus Primitives zusammensetzen.
 * <p>
 * <p>
 * Created by thomass on 09.03.17.
 */
public class ComposedPrimitives {

    public static SimpleGeometry buildBox(double width, double height, double depth) {
        Matrix4 m;
        SimpleGeometry geo = new SimpleGeometry();
        //oben und unten
        m = Matrix4.buildTransformationMatrix(new Vector3(0, (height) / 2, 0), new Quaternion());
        geo = geo.add(Primitives.buildPlaneGeometry(width, depth, 1, 1).transform(m));
        m = Matrix4.buildTransformationMatrix(new Vector3(0, -(height) / 2, 0),  Quaternion.buildFromAngles(new Degree(180), new Degree(0), new Degree(0)));
        geo = geo.add(Primitives.buildPlaneGeometry(width, depth, 1, 1).transform(m));
        //vorne und hinten. Plane an x-Achse aufrichten
        m = Matrix4.buildTransformationMatrix(new Vector3(0, 0, (depth) / 2),  Quaternion.buildFromAngles(new Degree(90), new Degree(0), new Degree(0)));
        geo = geo.add(Primitives.buildPlaneGeometry(width, height, 1, 1).transform(m));
        m = Matrix4.buildTransformationMatrix(new Vector3(0, 0, -(depth) / 2),  Quaternion.buildFromAngles(new Degree(-90), new Degree(0), new Degree(0)));
        geo = geo.add(Primitives.buildPlaneGeometry(width, height, 1, 1).transform(m));
        //links und rechts. Plane an z-Achse aufrichten
        m = Matrix4.buildTransformationMatrix(new Vector3(-width / 2, 0, 0),  Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(90)));
        geo = geo.add(Primitives.buildPlaneGeometry(height, depth, 1, 1).transform(m));
        m = Matrix4.buildTransformationMatrix(new Vector3(width / 2, 0, 0),  Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(-90)));
        geo = geo.add(Primitives.buildPlaneGeometry(height, depth, 1, 1).transform(m));
        return geo;
    }

    /**
     * width an x-Achse, height an y und depth an z-Achse.
     * segments = 0 und radius = 0 führt zu einem normalen Würfel.
     * Setzt sich zusammen aus 6 Planes, 8-Achtelkugeln und 8 ViertelCylinder.
     * Skizze 15
     * <p>
     * Infos, wie man sowas mit Blender macht, gibts in
     * -http://blender.stackexchange.com/questions/10787/simulating-rounded-edges-on-a-mesh und
     * -http://blender.stackexchange.com/questions/2534/how-can-i-round-the-edges-of-a-mesh
     *
     * @param width
     * @param height
     * @param depth
     * @return
     */
    public static SimpleGeometry buildRoundedBox(float width, float height, float depth, float radius, int segments) {
        Matrix4 m;
        SimpleGeometry geo = new SimpleGeometry();
        //oben und unten
        m = Matrix4.buildTransformationMatrix(new Vector3(0, (height) / 2, 0), new Quaternion());
        geo = geo.add(Primitives.buildPlaneGeometry(width - 2 * radius, depth - 2 * radius, 1, 1).transform(m));
        m = Matrix4.buildTransformationMatrix(new Vector3(0, -(height) / 2, 0),  Quaternion.buildFromAngles(new Degree(180), new Degree(0), new Degree(0)));
        geo = geo.add(Primitives.buildPlaneGeometry(width - 2 * radius, depth - 2 * radius, 1, 1).transform(m));
        //vorne und hinten. Plane an x-Achse aufrichten
        m = Matrix4.buildTransformationMatrix(new Vector3(0, 0, (depth) / 2),  Quaternion.buildFromAngles(new Degree(90), new Degree(0), new Degree(0)));
        geo = geo.add(Primitives.buildPlaneGeometry(width - 2 * radius, height - 2 * radius, 1, 1).transform(m));
        m = Matrix4.buildTransformationMatrix(new Vector3(0, 0, -(depth) / 2),  Quaternion.buildFromAngles(new Degree(-90), new Degree(0), new Degree(0)));
        geo = geo.add(Primitives.buildPlaneGeometry(width - 2 * radius, height - 2 * radius, 1, 1).transform(m));
        //links und rechts. Plane an z-Achse aufrichten
        m = Matrix4.buildTransformationMatrix(new Vector3(-width / 2, 0, 0),  Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(90)));
        geo = geo.add(Primitives.buildPlaneGeometry(height - 2 * radius, depth - 2 * radius, 1, 1).transform(m));
        m = Matrix4.buildTransformationMatrix(new Vector3(width / 2, 0, 0),  Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(-90)));
        geo = geo.add(Primitives.buildPlaneGeometry(height - 2 * radius, depth - 2 * radius, 1, 1).transform(m));
        //4 Viertelzylinder, die ohne Rotation auskommen (die "stehenden" parallel zur y-Achse).
        //links vorne
        m = Matrix4.buildTransformationMatrix(new Vector3(-width / 2 + radius, 0, depth / 2 - radius), new Quaternion());
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, height - 2 * radius, segments, MathUtil2.PI, MathUtil2.PI_2).transform(m));
        //rechts vorne
        m = Matrix4.buildTransformationMatrix(new Vector3(width / 2 - radius, 0, depth / 2 - radius), new Quaternion());
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, height - 2 * radius, segments, 3 * MathUtil2.PI_2, MathUtil2.PI_2).transform(m));
        //rechts hinten
        m = Matrix4.buildTransformationMatrix(new Vector3(width / 2 - radius, 0, -depth / 2 + radius), new Quaternion());
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, height - 2 * radius, segments, 0, MathUtil2.PI_2).transform(m));
        //links hinten
        m = Matrix4.buildTransformationMatrix(new Vector3(-width / 2 + radius, 0, -depth / 2 + radius), new Quaternion());
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, height - 2 * radius, segments, MathUtil2.PI_2, MathUtil2.PI_2).transform(m));
        //jetzt 4 Viertelzylinder, alle um die x-Achse rotiert.
        //links unten
        m = Matrix4.buildTransformationMatrix(new Vector3(-width / 2 + radius, -height / 2 + radius, 0),  Quaternion.buildFromAngles(new Degree(90), new Degree(0), new Degree(0)));
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, depth - 2 * radius, segments, MathUtil2.PI, MathUtil2.PI_2).transform(m));
        //rechts unten
        m = Matrix4.buildTransformationMatrix(new Vector3(width / 2 - radius, -height / 2 + radius, 0),  Quaternion.buildFromAngles(new Degree(90), new Degree(0), new Degree(0)));
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, depth - 2 * radius, segments, 3 * MathUtil2.PI_2, MathUtil2.PI_2).transform(m));
        //rechts oben
        m = Matrix4.buildTransformationMatrix(new Vector3(width / 2 - radius, height / 2 - radius, 0),  Quaternion.buildFromAngles(new Degree(90), new Degree(0), new Degree(0)));
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, depth - 2 * radius, segments, 0, MathUtil2.PI_2).transform(m));
        //links oben
        m = Matrix4.buildTransformationMatrix(new Vector3(-width / 2 + radius, height / 2 - radius, 0),  Quaternion.buildFromAngles(new Degree(90), new Degree(0), new Degree(0)));
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, depth - 2 * radius, segments, MathUtil2.PI_2, MathUtil2.PI_2).transform(m));
        //und nochmal 4 Viertelzylinder, alle um die z-Achse rotiert.
        //vorne unten
        m = Matrix4.buildTransformationMatrix(new Vector3(0, -height / 2 + radius, depth / 2 - radius),  Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(90)));
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, width - 2 * radius, segments, MathUtil2.PI, MathUtil2.PI_2).transform(m));
        //vorne oben
        m = Matrix4.buildTransformationMatrix(new Vector3(0, height / 2 - radius, depth / 2 - radius),  Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(90)));
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, width - 2 * radius, segments, 3 * MathUtil2.PI_2, MathUtil2.PI_2).transform(m));
        //hinten oben
        m = Matrix4.buildTransformationMatrix(new Vector3(0, height / 2 - radius, -depth / 2 + radius),  Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(90)));
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, width - 2 * radius, segments, 0, MathUtil2.PI_2).transform(m));
        //hinten unten
        m = Matrix4.buildTransformationMatrix(new Vector3(0, -height / 2 + radius, -depth / 2 + radius),  Quaternion.buildFromAngles(new Degree(0), new Degree(0), new Degree(90)));
        geo = geo.add(Primitives.buildCylinderGeometry(radius, radius, width - 2 * radius, segments, MathUtil2.PI_2, MathUtil2.PI_2).transform(m));
        // und jetzt 8 8el Spheres
        // links unten vorne
        m = Matrix4.buildTransformationMatrix(new Vector3(-width / 2 + radius, -height / 2 + radius, depth / 2 - radius), new Quaternion());
        geo = geo.add(Primitives.buildSphereGeometry(radius, segments, MathUtil2.PI, MathUtil2.PI_2, segments, MathUtil2.PI_2, MathUtil2.PI_2).transform(m));
        // rechts unten vorne
        m = Matrix4.buildTransformationMatrix(new Vector3(width / 2 - radius, -height / 2 + radius, depth / 2 - radius), new Quaternion());
        geo = geo.add(Primitives.buildSphereGeometry(radius, segments, 3 * MathUtil2.PI_2, MathUtil2.PI_2, segments, MathUtil2.PI_2, MathUtil2.PI_2).transform(m));
        // rechts oben vorne
        m = Matrix4.buildTransformationMatrix(new Vector3(width / 2 - radius, height / 2 - radius, depth / 2 - radius), new Quaternion());
        geo = geo.add(Primitives.buildSphereGeometry(radius, segments, 3 * MathUtil2.PI_2, MathUtil2.PI_2, segments, 0, MathUtil2.PI_2).transform(m));
        // links oben vorne
        m = Matrix4.buildTransformationMatrix(new Vector3(-width / 2 + radius, height / 2 - radius, depth / 2 - radius), new Quaternion());
        geo = geo.add(Primitives.buildSphereGeometry(radius, segments, MathUtil2.PI, MathUtil2.PI_2, segments, 0, MathUtil2.PI_2).transform(m));
        // links unten hinten
        m = Matrix4.buildTransformationMatrix(new Vector3(-width / 2 + radius, -height / 2 + radius, -depth / 2 + radius), new Quaternion());
        geo = geo.add(Primitives.buildSphereGeometry(radius, segments, MathUtil2.PI_2, MathUtil2.PI_2, segments, MathUtil2.PI_2, MathUtil2.PI_2).transform(m));
        // rechts unten hinten
        m = Matrix4.buildTransformationMatrix(new Vector3(width / 2 - radius, -height / 2 + radius, -depth / 2 + radius), new Quaternion());
        geo = geo.add(Primitives.buildSphereGeometry(radius, segments, 0, MathUtil2.PI_2, segments, MathUtil2.PI_2, MathUtil2.PI_2).transform(m));
        // rechts oben hinten
        m = Matrix4.buildTransformationMatrix(new Vector3(width / 2 - radius, height / 2 - radius, -depth / 2 + radius), new Quaternion());
        geo = geo.add(Primitives.buildSphereGeometry(radius, segments, 0, MathUtil2.PI_2, segments, 0, MathUtil2.PI_2).transform(m));
        // links oben hinten
        m = Matrix4.buildTransformationMatrix(new Vector3(-width / 2 + radius, height / 2 - radius, -depth / 2 + radius), new Quaternion());
        geo = geo.add(Primitives.buildSphereGeometry(radius, segments, MathUtil2.PI_2, MathUtil2.PI_2, segments, 0, MathUtil2.PI_2).transform(m));

        return geo;// geo;
    }


}
