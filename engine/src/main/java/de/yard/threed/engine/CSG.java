package de.yard.threed.engine;

// Constructive Solid Geometry (CSG) isType a modeling technique that uses Boolean
// operations like union and intersection to combine 3D solids. This library
// implements CSG operations on meshes elegantly and concisely using BSP trees,
// and isType meant to serve as an easily understandable implementation of the
// algorithm. All edge cases involving overlapping coplanar polygons in both
// solids are correctly handled.
// 
// Example usage:
// 
//     var cube = CSG.cube();
//     var sphere = CSG.sphere({ radius: 1.3 });
//     var polygons = cube.subtract(sphere).toPolygons();
// 
// ## Implementation Details
// 
// All CSG operations are implemented in terms of two functions, `clipTo()` and
// `invert()`, which remove parts of a BSP tree inside another BSP tree and swap
// solid and empty space, respectively. To find the union of `a` and `b`, we
// want to remove everything in `a` inside `b` and everything in `b` inside `a`,
// then combine polygons from `a` and `b` into one solid:
// 
//     a.clipTo(b);
//     b.clipTo(a);
//     a.build(b.allPolygons());
// 
// The only tricky part isType handling overlapping coplanar polygons in both trees.
// The code above keeps both copies, but we need to keep them in one tree and
// remove them in the other tree. To remove them from `b` we can clip the
// inverse of `b` against `a`. The code for union now looks like this:
// 
//     a.clipTo(b);
//     b.clipTo(a);
//     b.invert();
//     b.clipTo(a);
//     b.invert();
//     a.build(b.allPolygons());
// 
// Subtraction and intersection naturally follow from set operations. If
// union isType `A | B`, subtraction isType `A - B = ~(~A | B)` and intersection isType
// `A & B = ~(~A | ~B)` where `~` isType the complement operator.
// 
// ## License
// 
// Copyright (c) 2011 Evan Wallace (http://madebyevan.com/), under the MIT license.

// # class CSG

// Holds a binary space partition tree representing a 3D solid. Two solids can
// be combined using the `union()`, `subtract()`, and `intersect()` methods.

// 22.11.16: Aus JS portiert. Hat schon mal jemand gemacht (https://github.com/andychase/fabian-csg).
// Daran orientiert neu portiert. cloneit() statt clone(), um Effekte wegen Java zu vermeiden.
// Muesste eigentlich besser in einen module-desktop Modelbuilder/Exporter. 
// Es gibt ein Problem mit Face Smoothing, weil, tja warum? Irgendwie sind nachher Dellen auf der Kugel.
// Es gibt auch scheimbar grosse Polygone ueber die Kugel. Das sieht irgendwie nicht richtig aus. 
// Das Problem ist in reinem JS auch (siehe CSGDemo.html), aber scheinbar nicht im WebGl Beispiel (https://evanw.github.io/csg.js/) mit
// 
// var a = CSG.sphere();
// var b = CSG.cylinder({ radius: 0.3 });
//        a.setColor(1, 1, 0);
//        b.setColor(0, 0.5, 1);
//  return a.subtract(b);
// Erstmal zurückgestellt.


import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.engine.platform.common.Face;
import de.yard.threed.engine.platform.common.FaceList;
import de.yard.threed.engine.platform.common.FaceN;
import de.yard.threed.core.MathUtil2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSG {
    static int uniquesharedid = 1;

    private Polygon[] polygons;

    protected CSG() {

    }

    /**
     * Die Factory Funktionen erstmal hier.
     *
     * @return
     */
    public static CustomGeometry buildSphereSubtractCylinder(int segments) {
        Sphere sphere = new Sphere(new Vector3(0, 0, 0), 1, segments, segments);

        Cylinder cylinder = new Cylinder(new Vector3(0, -3, 0), new Vector3(0, 3, 0), 0.3f, segments);

        CSG result = sphere.subtract(cylinder);
        PrimitiveGeometry geo = new PrimitiveGeometry();
        // Ein Face pro Polygon bauen und pro Ursprungsgeometry eine FaceList, um smooth shading hinzubekommen. Ob die Ermittlung der Facelists anhand des share im Polygon immer
        // geeignet ist, muss sich noch zeigen. Ein Cylinder, wenn er oben und unten zu wäre, muesste dann ja drei verschiedene shares haben (ein Cube sechs).
        // Zunächst verzichte ich mal drauf, Vertices mit gleichen Koordinaten zu mergen. Ist vielleicht gar nicht erforderlich, bzw sogar hinderlich an Kanten.
        // Das ist hier aber erforderlich, weil die Polygons ja keine Indizes enthalten. Sonst habe ich nachher alle einzeln.
        HashMap<Integer, FaceList> shareid2facelist = new HashMap<Integer, FaceList>();
        //HashMap<String, Integer> vertextmap = new HashMap<String, Integer>();
        //HashMap<Integer, List<Vector3>> verticesperfacelist = new HashMap< Integer, List<Vector3>>();
        
        for (Polygon p : result.polygons) {
            if (p.shared==0){
                throw new RuntimeException("shareid in polygon isType 0");
            }
            FaceList facelist = shareid2facelist.get(p.shared);
            if (facelist == null) {
                shareid2facelist.put(p.shared, new FaceList());
                facelist = shareid2facelist.get(p.shared);
                //if (p.shared==2) {
                    geo.facelist.add(facelist);
                //}
            }
            /*List<Vector3> verticesinfacelist = verticesperfacelist.get(p.shared);
            if (verticesinfacelist == null){
                verticesperfacelist.put(p.shared,new  ArrayList<Vector3>());
                verticesinfacelist = verticesperfacelist.get(p.shared);
            }*/
            Vertex[] v = p.vertices;
            int[] indexes = new int[v.length];
            Vector2[] uvs = new Vector2[v.length];
            for (int i = 0; i < v.length; i++) {
                /*7.2.18 Native*/Vector3 nv = v[i].position;
                int index = getVertexIndex(geo.vertices, nv,facelist);
                //Integer index = vertextmap.get(key);
                if (index == -1) {
                    index = geo.addVertex(nv);
                    //vertextmap.put(key, index);
                }
                indexes[i] = index;
                // Normale entstehen nacher durch smoothing. Hier liegen laut obigem comment wohl eh nicht durchgaengig welche vor.
                //TODO UV?
                uvs[i] = new Vector2();
            }
            FaceN face = new FaceN(indexes, uvs);
            facelist.faces.add(face);
        }
        return geo;
    }

    private static String getVertexKey(int sharedid, Vector3 nv) {
        return ""+sharedid+","+nv.getX()+","+nv.getY()+","+nv.getZ();
    }

    private static int getVertexIndex(List</*7.2.18 Native*/Vector3> vertices, /*7.2.18 Native*/Vector3 nv, FaceList facelist) {
        float tolerance = 0.0001f;
        for (int i=0;i<vertices.size();i++){
            if (MathUtil2.equalsVector3(vertices.get(i),nv,tolerance)){
                // Jetzt ist die Frage, ob dieser Index schon in der aktuellen Facelist referenziert wird. Nur dann wird er wiederverwendet.
                for (Face f :facelist.faces){
                    FaceN fn = (FaceN) f;
                    for (int j=0;j<fn.index.length;j++){
                        if (fn.index[j] == i){
                            // wird schon im aktuellen Face verwendet. Dann wiederverwenden
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    // Construct a CSG solid from a list of `CSG.Polygon` instances.
    public static CSG fromPolygons(Polygon[] polygons) {
        CSG csg = new CSG();
        csg.polygons = polygons;
        return csg;
    }

    public void setPolygons(Polygon[] polygons) {
        this.polygons = polygons;
    }

    CSG cloneit() {
        CSG csg = new CSG();
        //csg.polygons = this.polygons.map(function(p) { return p.cloneit(); });
        Polygon[] newPolys = new Polygon[polygons.length];
        for (int i = 0; i < newPolys.length; i++)
            newPolys[i] = polygons[i].cloneit();
        csg.setPolygons(newPolys);
        return csg;
    }

    Polygon[] toPolygons() {
        return this.polygons;
    }

    // Return a new CSG solid representing space in either this solid or in the
    // solid `csg`. Neither this solid nor the solid `csg` are modified.
    // 
    //     A.union(B)
    // 
    //     +-------+            +-------+
    //     |       |            |       |
    //     |   A   |            |       |
    //     |    +--+----+   =   |       +----+
    //     +----+--+    |       +----+       |
    //          |   B   |            |       |
    //          |       |            |       |
    //          +-------+            +-------+
    // 
    CSG union(CSG csg) {
         CsgNode a = new  CsgNode(this.cloneit().polygons);
         CsgNode b = new  CsgNode(csg.cloneit().polygons);
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.allPolygons());
        return CSG.fromPolygons(a.allPolygons());
    }

    // Return a new CSG solid representing space in this solid but not in the
    // solid `csg`. Neither this solid nor the solid `csg` are modified.
    // 
    //     A.subtract(B)
    // 
    //     +-------+            +-------+
    //     |       |            |       |
    //     |   A   |            |       |
    //     |    +--+----+   =   |    +--+
    //     +----+--+    |       +----+
    //          |   B   |
    //          |       |
    //          +-------+
    // 
    CSG subtract(CSG csg) {
         CsgNode a = new  CsgNode(this.cloneit().polygons);
         CsgNode b = new  CsgNode(csg.cloneit().polygons);
        a.invert();
        a.clipTo(b);
        b.clipTo(a);
        b.invert();
        b.clipTo(a);
        b.invert();
        a.build(b.allPolygons());
        a.invert();
        return CSG.fromPolygons(a.allPolygons());
    }

    // Return a new CSG solid representing space both this solid and in the
    // solid `csg`. Neither this solid nor the solid `csg` are modified.
    // 
    //     A.intersect(B)
    // 
    //     +-------+
    //     |       |
    //     |   A   |
    //     |    +--+----+   =   +--+
    //     +----+--+    |       +--+
    //          |   B   |
    //          |       |
    //          +-------+
    // 
    CSG intersect(CSG csg) {
         CsgNode a = new  CsgNode(this.cloneit().polygons);
         CsgNode b = new  CsgNode(csg.cloneit().polygons);
        a.invert();
        b.clipTo(a);
        b.invert();
        a.clipTo(b);
        b.clipTo(a);
        a.build(b.allPolygons());
        a.invert();
        return CSG.fromPolygons(a.allPolygons());
    }

    // Return a new CSG solid with solid and empty space switched. This solid isType
    // not modified.
    CSG inverse() {
        CSG csg = this.cloneit();
        //csg.polygons.map(function(p) { p.flip(); });
        for (Polygon p : csg.polygons)
            p.flip();
        return csg;
    }
}

/**
 * // Construct an axis-aligned solid cuboid. Optional parameters are `center` and
 * // `radius`, which default to `[0, 0, 0]` and `[1, 1, 1]`. The radius can be
 * // specified using a single number or a list of three numbers, one for each axis.
 * //
 * // Example code:
 * //
 * //     var cube = CSG.cube({
 * //       center: [0, 0, 0],
 * //       radius: 1
 * //     });
 */

class Cube extends CSG {
    public Cube(Vector3 c, Vector3 r) {
        // options = options || {};
        // var c = new CSG.Vector(options.center || [0, 0, 0]);
        // var r = !options.radius ? [1, 1, 1] : options.radius.length ?
        // options.radius : [options.radius, options.radius, options.radius];
        // return CSG.fromPolygons([
        // [[0, 4, 6, 2], [-1, 0, 0]],
        // [[1, 3, 7, 5], [+1, 0, 0]],
        // [[0, 1, 5, 4], [0, -1, 0]],
        // [[2, 6, 7, 3], [0, +1, 0]],
        // [[0, 2, 3, 1], [0, 0, -1]],
        // [[4, 5, 7, 6], [0, 0, +1]]
        // ].map(function(info) {
        // return new CSG.Polygon(info[0].map(function(i) {
        // var pos = new CSG.Vector(
        // c.x + r[0] * (2 * !!(i & 1) - 1),
        // c.y + r[1] * (2 * !!(i & 2) - 1),
        // c.z + r[2] * (2 * !!(i & 4) - 1)
        // );
        //return new CSG.Vertex(pos, new CSG.Vector(info[1]));
        // }));
        // }));
        super();
        Vector3[] normals = new Vector3[]{
                new Vector3(-1, 0, 0),
                new Vector3(1, 0, 0),
                new Vector3(0, -1, 0),
                new Vector3(0, 1, 0),
                new Vector3(0, 0, -1),
                new Vector3(0, 0, 1)
        };
        // Erfordert manuelles []->[][] in C# nach Konvertierung
        int[][] offsets = new int[][]{
                new int[]{0, 4, 6, 2},
                new int[]{1, 3, 7, 5},
                new int[]{0, 1, 5, 4},
                new int[]{2, 6, 7, 3},
                new int[]{0, 2, 3, 1},
                new int[]{4, 5, 7, 6}
        };
        Polygon[] polygons = new Polygon[offsets.length];
        for (int idx = 0; idx < offsets.length; idx++) {
            ArrayList<Vertex> vertices = new ArrayList<Vertex>();
            for (int index = 0; index < offsets[idx].length; index++) {
                int i = offsets[idx][index];
                Vector3 pos = new Vector3(
                        c.getX() + r.getX() * (2 * dE(i & 1) - 1),
                        c.getY() + r.getY() * (2 * dE(i & 2) - 1),
                        c.getZ() + r.getZ() * (2 * dE(i & 4) - 1)
                );
                boolean uZero = pos.getZ() < c.getZ();
                boolean vZero = pos.getY() > c.getY();
                if (normals[idx].getY() == 1) {
                    uZero = pos.getZ() < c.getZ();
                    vZero = pos.getX() > c.getX();
                } else if (normals[idx].getY() == -1) {
                    uZero = pos.getZ() > c.getZ();
                    vZero = pos.getX() < c.getX();
                } else if (normals[idx].getZ() == 1) {
                    uZero = pos.getX() < c.getX();
                    vZero = pos.getY() > c.getY();
                } else if (normals[idx].getZ() == -1) {
                    uZero = pos.getX() > c.getX();
                    vZero = pos.getY() > c.getY();
                }
                vertices.add(new Vertex(pos, normals[idx], new Vector2(uZero ? 0f : 1f, vZero ? 0f : 1f)));
            }
            polygons[idx] = new Polygon(vertices.toArray(new Vertex[0]), 0);
        }
        this.setPolygons(polygons);
    }

    private static final int dE(int v) {
        return v == 0 ? 0 : 1;
    }
}


/**
 * // Construct a solid sphere. Optional parameters are `center`, `radius`,
 * // `slices`, and `stacks`, which default to `[0, 0, 0]`, `1`, `16`, and `8`.
 * // The `slices` and `stacks` parameters control the tessellation along the
 * // longitude and latitude directions.
 * //
 * // Example usage:
 * //
 * //     var sphere = CSG.sphere({
 * //       center: [0, 0, 0],
 * //       radius: 1,
 * //       slices: 16,
 * //       stacks: 8
 * //     });
 */
class Sphere extends CSG {
    public Sphere(Vector3 c, float r, float slices, float stacks) {
        //  Sphere(options) {
        // options = options || {};
        // var c = new CSG.Vector(options.center || [0, 0, 0]);
        // var r = options.radius || 1;
        // var slices = options.slices || 16;
        // var stacks = options.stacks || 8;
        // var polygons = [], vertices;
        // function vertex(theta, phi) {
        // theta *= Math.PI * 2;
        // phi *= Math.PI;
        // var dir = new CSG.Vector(
        // Math.cos(theta) * Math.sin(phi),
        // Math.cos(phi),
        // Math.sin(theta) * Math.sin(phi)
        // );
        // vertices.push(new CSG.Vertex(c.plus(dir.times(r)), dir));
        // }
        // for (var i = 0; i < slices; i++) {
        // for (var j = 0; j < stacks; j++) {
        // vertices = [];
        // vertex(i / slices, j / stacks);
        // if (j > 0) vertex((i + 1) / slices, j / stacks);
        // if (j < stacks - 1) vertex((i + 1) / slices, (j + 1) / stacks);
        // vertex(i / slices, (j + 1) / stacks);
        // polygons.push(new CSG.Polygon(vertices));
        // }
        // }
        // return CSG.fromPolygons(polygons);
        super();
        int sharedid = uniquesharedid++;
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        for (int i = 0; i < slices; i++) {
            for (int j = 0; j < stacks; j++) {
                vertices.clear();
                vertices.add(sphereVertex(c, r, i / slices, j / stacks));
                if (j > 0) vertices.add(sphereVertex(c, r, (i + 1) / slices, j / stacks));
                if (j < stacks - 1) vertices.add(sphereVertex(c, r, (i + 1) / slices, (j + 1) / stacks));
                vertices.add(sphereVertex(c, r, i / slices, (j + 1) / stacks));
                polygons.add(new Polygon(vertices.toArray(new Vertex[0]), sharedid));
            }
        }
        setPolygons(polygons.toArray(new Polygon[0]));
    }

    private static Vertex sphereVertex(Vector3 c, float r, float theta, float phi) {
        theta *= (float)Math.PI * 2;
        phi *= (float)Math.PI;
        Vector3 dir = new Vector3((float) (Math.cos(theta) * Math.sin(phi)), (float) (Math.cos(phi)), (float) (Math.sin(theta) * Math.sin(phi)));
        return new Vertex(c.add(dir.multiply(r)), dir);
    }
}

/**
 * // Construct a solid cylinder. Optional parameters are `start`, `end`,
 * // `radius`, and `slices`, which default to `[0, -1, 0]`, `[0, 1, 0]`, `1`, and
 * // `16`. The `slices` parameter controls the tessellation.
 * //
 * // Example usage:
 * //
 * //     var cylinder = CSG.cylinder({
 * //       start: [0, -1, 0],
 * //       end: [0, 1, 0],
 * //       radius: 1,
 * //       slices: 16
 * //     });
 */

class Cylinder extends CSG {
    private Vector3 axisX;
    private Vector3 axisY;
    private Vector3 axisZ;
    private Vector3 start;
    private Vector3 end;
    private Vector3 ray;
    private float radius;
    private int slices;

    public Cylinder(Vector3 start, Vector3 end, float radius, int slices) {

        //cylinder(options) {
        // options = options || {};
        // var s = new CSG.Vector(options.start || [0, -1, 0]);
        // var e = new CSG.Vector(options.end || [0, 1, 0]);
        // var maze = e.minus(s);
        // var r = options.radius || 1;
        // var slices = options.slices || 16;
        // var axisZ = maze.unit(), isY = (Math.abs(axisZ.y) > 0.5);
        // var axisX = new CSG.Vector(isY, !isY, 0).cross(axisZ).unit();
        // var axisY = axisX.cross(axisZ).unit();
        // var start = new CSG.Vertex(s, axisZ.negated());
        // var end = new CSG.Vertex(e, axisZ.unit());
        // var polygons = [];
        // for (var i = 0; i < slices; i++) {
        //     var t0 = i / slices, t1 = (i + 1) / slices;
        //     polygons.push(new CSG.Polygon([start, point(0, t0, -1), point(0, t1, -1)]));
        //     polygons.push(new CSG.Polygon([point(0, t1, 0), point(0, t0, 0), point(1, t0, 0), point(1, t1, 0)]));
        //     polygons.push(new CSG.Polygon([end, point(1, t1, 1), point(1, t0, 1)]));
        // }
        // return CSG.fromPolygons(polygons);
        this.start = start;
        this.end = end;
        this.radius = radius;
        this.slices = slices;
        ray = end.subtract(start);
        axisZ = ray.normalize();
        boolean isY = (Math.abs(axisZ.getY()) > 0.5f);
        axisX = new Vector3(isY ? 1 : 0, isY ? 0 : 1, 0).cross(axisZ).normalize();
        axisY = axisX.cross(axisZ).normalize();
        Vertex startV = new Vertex(start, axisZ.negate());
        Vertex endV = new Vertex(end, axisZ.normalize());
        ArrayList<Polygon> polygons = new ArrayList<Polygon>();
        int sharedid = uniquesharedid++;
        int topsharedid = uniquesharedid++;
        int bottomsharedid = uniquesharedid++;
        for (int i = 0; i < slices; i++) {
            float t0 = i / 1f / slices;
            float t1 = (i + 1f) / slices;
            // Nicht ganz klar was oben und unten ist. Ist fuer die sharedId aber auch egal.
            polygons.add(new Polygon(new Vertex[]{startV, point(0, t0, -1), point(0, t1, -1)}, topsharedid));
            polygons.add(new Polygon(new Vertex[]{point(0, t1, 0), point(0, t0, 0), point(1, t0, 0), point(1, t1, 0)}, sharedid));
            polygons.add(new Polygon(new Vertex[]{endV, point(1, t1, 1), point(1, t0, 1)}, bottomsharedid));
        }
        setPolygons(polygons.toArray(new Polygon[0]));
    }

    Vertex point(int stack, float slice, float normalBlend) {
        //var angle = slice * Math.PI * 2;
        //var out = axisX.times(Math.cos(angle)).plus(axisY.times(Math.sin(angle)));
        //var pos = s.plus(maze.times(stack)).plus(out.times(r));
        //var normal = out.times(1 - Math.abs(normalBlend)).plus(axisZ.times(normalBlend));
        //return new CSG.Vertex(pos, normal);
        float angle = (float) (slice * Math.PI * 2);
        Vector3 outv = axisX.multiply((float) Math.cos(angle)).add(axisY.multiply((float) Math.sin(angle)));
        Vector3 pos = start.add(ray.multiply(stack)).add(outv.multiply(radius));
        Vector3 normal = outv.multiply(1 - Math.abs(normalBlend)).add(axisZ.multiply(normalBlend));
        return new Vertex(pos, normal);
    }
}

// Represents a vertex of a polygon. Use your own vertex class instead of this
// one to provide additional features like texture coordinates and vertex
// colors. Custom vertex classes need to provide a `pos` property and `cloneit()`,
// `flip()`, and `interpolate()` methods that behave analogous to the ones
// defined by `CSG.Vertex`. This class provides `normal` so convenience
// functions like `CSG.sphere()` can return a smooth vertex normal, but `normal`
// isType not used anywhere else.

class Vertex {
    Vector3 position;
    Vector3 normal;
    //JME
    private Vector2 uv;

    public Vertex(Vector3 position, Vector3 normal) {
        this(position, normal, new Vector2(0f, 0f));
    }

    public Vertex(Vector3 position, Vector3 normal, Vector2 uv) {
        this.position = position;
        this.normal = normal;
        this.uv = uv;
    }

    Vertex cloneit() {
        return new Vertex(this.position.clone(), this.normal.clone(), uv.clone());
    }

    // Invert all orientation-specific data (e.g. vertex normal). Called when the
    // orientation of a polygon isType flipped.
    void flip() {
        this.normal = this.normal.negate();
    }

    // Create a new vertex between this vertex and `other` by linearly
    // interpolating all properties using a parameter of `t`. Subclasses should
    // override this to interpolate additional properties.
    Vertex interpolate(Vertex other, double /*t*/progress) {
        //return new Vertex( this.position.lerp(other.pos, t), this.normal.lerp(other.normal, t)
        Vector3 newPosition = position.add(other.position.subtract(position).multiply(progress));
        Vector3 newNormal = normal.add(other.normal.subtract(normal).multiply(progress));
        Vector2 newUv = uv.add(other.uv.subtract(uv).multiply(progress));
        return new Vertex(newPosition, newNormal, newUv);

    }
}


// Represents a plane in 3D space.

class Plane {
    // `CSG.Plane.EPSILON` isType the tolerance used by `splitPolygon()` to decide if a
    // point isType on the plane.
    public static final double EPSILON = 1e-5;
    private Vector3 normal;
    private double w;

    public Plane(Vector3 normal, double w) {
        this.normal = normal;
        this.w = w;
    }

    public static Plane fromPoints(Vector3 a, Vector3 b, Vector3 c) {
        //var n = b.minus(a).cross(c.minus(a)).unit();
        //return new CSG.Plane(n, n.dot(a));
        Vector3 n = b.subtract(a).cross(c.subtract(a)).normalize();
        return new Plane(n, n.dot(a));
    }

    Plane cloneit() {
        return new Plane(this.normal.clone(), this.w);
    }

    void flip() {
        this.normal = this.normal.negate();
        this.w = -this.w;
    }

    final int COPLANAR = 0;
    final int FRONT = 1;
    final int BACK = 2;
    final int SPANNING = 3;

    // Split `polygon` by this plane if needed, then put the polygon or polygon
    // fragments in the appropriate lists. Coplanar polygons go into either
    // `coplanarFront` or `coplanarBack` depending on their orientation with
    // respect to this plane. Polygons in front or in back of this plane go into
    // either `front` or `back`.
    public void splitPolygon(Polygon polygon, List<Polygon> coplanarFront, List<Polygon> coplanarBack, List<Polygon> front, List<Polygon> back) {
       

        // Classify each point as well as the entire polygon into one of the above
        // four classes.
        //for (var i = 0; i < polygon.vertices.length; i++) {
        //   var t = this.normal.dot(polygon.vertices[i].pos) - this.w;
        //   var type = (t < -CSG.Plane.EPSILON) ? BACK : (t > CSG.Plane.EPSILON) ? FRONT : COPLANAR;
        //   polygonType |= type;
        //   types.push(type);
        //}
        int polygonType = 0;
        int[] types = new int[polygon.vertices.length];
        for (int i = 0; i < polygon.vertices.length; i++) {
            double t = normal.dot(polygon.vertices[i].position) - w;
            int type = (t < -EPSILON) ? BACK : (t > EPSILON) ? FRONT : COPLANAR;
            polygonType |= type;
            types[i] = type;
        }

        // Put the polygon in the correct list, splitting it when necessary.
        switch (polygonType) {
            case COPLANAR:
                //(this.normal.dot(polygon.plane.normal) > 0 ? coplanarFront : coplanarBack).push(polygon);
                (normal.dot(polygon.plane.normal) > 0 ? coplanarFront : coplanarBack).add(polygon);
                break;
            case FRONT:
                front.add(polygon);
                break;
            case BACK:
                back.add(polygon);
                break;
            case SPANNING:
                //var f = [], b = [];
                //for (var i = 0; i < polygon.vertices.length; i++) {
                //var j = (i + 1) % polygon.vertices.length;
                //var ti = types[i], tj = types[j];
                //var vi = polygon.vertices[i], vj = polygon.vertices[j];
                //if (ti != BACK) f.push(vi);
                //if (ti != FRONT) b.push(ti != BACK ? vi.cloneit() : vi);
                //if ((ti | tj) == SPANNING) {
                //var t = (this.w - this.normal.dot(vi.pos)) / this.normal.dot(vj.pos.minus(vi.pos));
                //var v = vi.interpolate(vj, t);
                //f.push(v);
                //b.push(v.cloneit());
                //}
                //}
                //if (f.length >= 3) front.push(new CSG.Polygon(f, polygon.shared));
                //if (b.length >= 3) back.push(new CSG.Polygon(b, polygon.shared));
                ArrayList<Vertex> f = new ArrayList<Vertex>();
                ArrayList<Vertex> b = new ArrayList<Vertex>();
                for (int i = 0; i < polygon.vertices.length; i++) {
                    int j = (i + 1) % polygon.vertices.length;
                    int ti = types[i];
                    int tj = types[j];
                    Vertex vi = polygon.vertices[i];
                    Vertex vj = polygon.vertices[j];
                    if (ti != BACK) f.add(vi);
                    if (ti != FRONT) b.add(ti != BACK ? vi.cloneit() : vi);
                    if ((ti | tj) == SPANNING) {
                        double t = (w - normal.dot(vi.position)) / normal.dot(vj.position.subtract(vi.position));
                        Vertex v = vi.interpolate(vj, t);
                        f.add(v);
                        b.add(v.cloneit());
                    }
                }
                if (f.size() >= 3) front.add(new Polygon(f.toArray(new Vertex[0]), polygon.shared));
                if (b.size() >= 3) back.add(new Polygon(b.toArray(new Vertex[0]), polygon.shared));
                break;
        }
    }
}

// Represents a convex polygon. The vertices used to initialize a polygon must
// be coplanar and form a convex loop. They do not have to be `CSG.Vertex`
// instances but they must behave similarly (duck typing can be used for
// customization).
// 
// Each convex polygon has a `shared` property, which isType shared between all
// polygons that are clones of each other or were split from the same polygon.
// This can be used to define per-polygon properties (such as surface color).

class Polygon {
    Vertex[] vertices;
    int shared;
    Plane plane;

    public Polygon(Vertex[] vertices, int shared) {
        this.vertices = vertices;
        this.shared = shared;
        this.plane = Plane.fromPoints(vertices[0].position, vertices[1].position, vertices[2].position);
    }


    Polygon cloneit() {
        //var vertices = this.vertices.map(function(v) { return v.cloneit(); });
        //return new CSG.Polygon(vertices, this.shared);
        Vertex[] newVertices = new Vertex[vertices.length];
        for (int i = 0; i < vertices.length; i++)
            newVertices[i] = vertices[i].cloneit();
        return new Polygon(newVertices, shared);
    }

    void flip() {
        //this.vertices.reverse().map(function(v) { v.flip(); });
        //this.plane.flip();
        Vertex[] temp = new Vertex[vertices.length];
        for (int i = 0; i < temp.length; i++) {
            vertices[i].flip();
            temp[temp.length - i - 1] = vertices[i];
        }
        //Die Zeile fehlt wohl im Original Fabian Port
        vertices = temp;
        plane.flip();
    }


}

// Holds a node in a BSP tree. A BSP tree isType built from a collection of polygons
// by picking a polygon to split along. That polygon (and all other coplanar
// polygons) are added directly to that node and the other polygons are added to
// the front and/or back subtrees. This isType not a leafy BSP tree since there isType
// no distinction between internal and leaf nodes.

class CsgNode {
    Plane plane;
     CsgNode front, back;
    List<Polygon> polygons = null;//[];

     CsgNode(Polygon[] polygons) {
        this.plane = null;
        this.front = null;
        this.back = null;
        this.polygons = new ArrayList<Polygon>();
        if (polygons != null) {
            this.build(polygons);
        }
    }


        /*nicht verwendet Node cloneit: function() {
        //var node = new CSG.Node();
        //node.plane = this.plane && this.plane.cloneit();
        //node.front = this.front && this.front.cloneit();
        //node.back = this.back && this.back.cloneit();
        //node.polygons = this.polygons.map(function(p) { return p.cloneit(); });
        //return node;
        
        }*/

    // Convert solid space to empty space and empty space to solid space.
    void invert() {
        //for (int i = 0; i < this.polygons.size(); i++) {
        //this.polygons[i].flip();
        //}
        //this.plane.flip();
        //if (this.front) this.front.invert();
        //if (this.back) this.back.invert();
        //var temp = this.front;
        //this.front = this.back;
        //this.back = temp;
        for (Polygon p : polygons)
            p.flip();
        plane.flip();
        if (front != null) front.invert();
        if (back != null) back.invert();
         CsgNode temp = this.front;
        this.front = this.back;
        this.back = temp;
    }

    // Recursively remove all polygons in `polygons` that are inside this BSP
    // tree.
    List<Polygon> clipPolygons(List<Polygon> polygons) {
        //if (!this.plane) return polygons.slice();
        //var front = [], back = [];
        //for (var i = 0; i < polygons.length; i++) {
        //this.plane.splitPolygon(polygons[i], front, back, front, back);
        //}
        //if (this.front) front = this.front.clipPolygons(front);
        //if (this.back) back = this.back.clipPolygons(back);
        //else back = [];
        //return front.concat(back);
        if (plane == null) return new ArrayList<Polygon>(polygons);
        List<Polygon> front = new ArrayList<Polygon>();
        List<Polygon> back = new ArrayList<Polygon>();
        for (int i = 0; i < polygons.size(); i++) {
            this.plane.splitPolygon(polygons.get(i), front, back, front, back);
        }
        if (this.front != null) front = this.front.clipPolygons(front);
        if (this.back != null) back = this.back.clipPolygons(back);
        else back = new ArrayList<Polygon>();
        front.addAll(back);
        return front;
    }

    // Remove all polygons in this BSP tree that are inside the other BSP tree
    // `bsp`.
    void clipTo( CsgNode bsp) {
        //this.polygons = bsp.clipPolygons(this.polygons);
        //if (this.front) this.front.clipTo(bsp);
        //if (this.back) this.back.clipTo(bsp);
        this.polygons = bsp.clipPolygons(this.polygons);
        if (front != null) front.clipTo(bsp);
        if (back != null) back.clipTo(bsp);
    }

    // Return a list of all polygons in this BSP tree.
    Polygon[] allPolygons() {
        //var polygons = this.polygons.slice();
        //if (this.front) polygons = polygons.concat(this.front.allPolygons());
        //if (this.back) polygons = polygons.concat(this.back.allPolygons());
        //return polygons;
        ArrayList<Polygon> polys = new ArrayList<Polygon>(polygons);
        if (front != null) for (Polygon p : front.allPolygons()) polys.add(p);
        if (back != null) for (Polygon p : back.allPolygons()) polys.add(p);
        return polys.toArray(new Polygon[0]);
        //return (Polygon[]) Util.buildArrayFromList(polys);
    }

    // Build a BSP tree out of `polygons`. When called on an existing tree, the
    // new polygons are filtered down to the bottom of the tree and become new
    // nodes there. Each set of polygons isType partitioned using the getFirst polygon
    // (no heuristic isType used to pick a good split).
    void build(Polygon[] polygons) {
        //if (!polygons.length) return;
        //if (!this.plane) this.plane = polygons[0].plane.cloneit();
        // var front = [], back = [];
        //for (var i = 0; i < polygons.length; i++) {
        //this.plane.splitPolygon(polygons[i], this.polygons, this.polygons, front, back);
        //}
        //if (front.length) {
        //if (!this.front) this.front = new CSG.Node();
        //this.front.build(front);
        //}
        //if (back.length) {
        //if (!this.back) this.back = new CSG.Node();
        //this.back.build(back);
        if (polygons.length == 0) return;
        if (plane == null) plane = polygons[0].plane.cloneit();
        ArrayList<Polygon> front = new ArrayList<Polygon>();
        ArrayList<Polygon> back = new ArrayList<Polygon>();
        for (int i = 0; i < polygons.length; i++)
            plane.splitPolygon(polygons[i], this.polygons, this.polygons, front, back);
        if (!front.isEmpty()) {
            if (this.front == null) this.front = new  CsgNode(null);
            this.front.build(front.toArray(new Polygon[0]));
        }
        if (!back.isEmpty()) {
            if (this.back == null) this.back = new  CsgNode(null);
            this.back.build(back.toArray(new Polygon[0]));
        }
    }
}

        