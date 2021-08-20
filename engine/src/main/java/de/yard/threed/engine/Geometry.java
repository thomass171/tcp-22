package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeGeometry;
import de.yard.threed.core.MathUtil2;

/**
 * Ist jetzt nur noch zur Abbildung einer NativeGeometry, aber nicht mehr
 * fuer selbstgemachte (dafuer ist CustomGeometry).
 *
 * 29.1.16: Eigentlich reicht custom doch aus, oder? Nur Cube zum Testen. 14.3.17: Nee, SimpleGeo ist auch wichtig.
 *
 * Date: 14.02.14
 * Time: 08:31
 */
public abstract class Geometry /*extends Object3D*/ {
    Log logger = Platform.getInstance().getLog(Geometry.class);
    protected NativeGeometry geometry;

    // 8.6.14: Nach einigem Hin und Her sind Faces und Vertices zusammen hier, ohne
    // das dadurch eine spezielle Geometrie oder Geometrieherkunft definiert ist.
    // So kann jede Ableitung beliebig Geometrien aufbauen.
    // 29.09.14: Faces jetzt in Surface und die stattdessen hier.
    // 29.5.15: Ist jetzt in Ableitung 
    //List<Surface> surfaces = new ArrayList<Surface>();
   //26.1.16 protected List<Vertex> vertices = new ArrayList<Vertex>();
    //boolean isVBO;
    //   private int verticesperplane;
    //private int planeCnt;
    private int markercnt = 0;
    
   /* Geometry() {
      //  planeCnt = -1;
        //verticesperplane = -1;
    } */

    protected Geometry(/*int planecnt, int verticesperplane*/) {

    }    
    
    /*3.5.16 ist ja noch abstract public Geometry(NativeGeometry geo){
        geometry=geo;
    }*/

    /*public void addVertex(Vertex vertex) {
        vertices.add(vertex);
    }*/



    /*26.1.16 public List<Vertex> getVertices() {
        return vertices;
    }*/


   /*26.1.16  public void addMarker() {
        markercnt++;
        vertices.add(new Vertex(new Vector3(10f, 10f, 10f)));
    }*/

    public int getMarkerCount() {
        return markercnt;
    }


    public NativeGeometry getNativeGeometry() {
        return geometry;
    }

    public String getId() {
        return geometry.getId();
    }

    public static Geometry buildCube(double width, double height, double depth){
       // geometry = Platform.getInstance().buildCubeGeometry(width, height, depth);
        return new GenericGeometry(Primitives.buildBox(width,height,depth));
    }

    public static Geometry buildPlaneGeometry(double width, double depth, int widthSegments, int depthSegments){
        //geometry = Platform.getInstance().buildPlaneGeometry(width, height, widthSegments, heightSegments);
        return new GenericGeometry(Primitives.buildPlaneGeometry(width,depth,widthSegments,depthSegments));
    }

    public static Geometry buildCylinderGeometry(double radiusTop, double radiusBottom, double height, int segments, double startangle, double spanangle){
        return new GenericGeometry(Primitives.buildCylinderGeometry(radiusTop,radiusBottom,height,segments,startangle,spanangle));
    }
        
    public static Geometry buildCylinderGeometry(double radiusTop, double radiusBottom, double height, int segments) {
        return buildCylinderGeometry(radiusTop, radiusBottom, height, segments, 0, MathUtil2.PI2);
    }
}
