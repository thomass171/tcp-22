package de.yard.threed.core.geometry;


import de.yard.threed.core.platform.Platform;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;

import java.util.List;


/**
 * Surface die sich durch Aufliegen auf einem Shape ergibt. Verwendet z.B. beim Schliessen von Front und Back.
 * <p/>
 * <p/>
 * Oder sollte das FlatSurface heissen?
 * <p/>
 * Date: 02.09.14
 */
public class ShapeSurface extends Surface {
    Log logger = Platform.getInstance().getLog(ShapeSurface.class);
    // Die xy Koordinaten jedes Vertex .
    //24.11.15 HashMap<Integer, Vector2> vloc = new HashMap<Integer, Vector2>();
    Shape shape;
    boolean isback = false;

    public ShapeSurface(Shape shape, boolean isback) {
        this.shape = shape;
        this.isback = isback;
    }

   /*24.11.15 void addVertex(int index, Vector2 loc) {
        // Der Check greift, ist geprueft
        if (vloc.containsKey(index)) {
            throw new RuntimeException("Duplicate index " + index);
        }
        vloc.put(index, loc);
    }*/


    /**
     * Liefert die relative bzw. natürliche Position eines Vertex auf seiner "Surface".
     * Der Wert (jeweils x und y) liegt zwischen 0 (ganz links/unten) bis 1 (ganz rechts/oben).
     * Die UvMap ist optional.
     * <p/>
     *
     * @return
     */
    public Vector2 calcVertexLocation(/*7.2.18 Native*/Vector3 v, UvMap1 uvmap) {
        double minx = shape.getMinX();
        double maxx = shape.getMaxX();
        double miny = shape.getMinY();
        double maxy = shape.getMaxY();
        double xdiff = maxx - minx;
        double ydiff = maxy - miny;
        double xd = Math.abs(v.getX() - minx);
        double yd = Math.abs(v.getY() - maxy);
        double x = xd / xdiff;
        double y = yd / ydiff;
        //logger.debug(Util.format("calcVertexLocation found x=%f,y=%f for vertex %s. minx=%f, maxx=%f, miny=%f, maxy=%f", x, y, v, minx, maxx, miny, maxy));
        // y=0 bei Texturen ist ja unten
        // 24.11.15: Bei Back den berechneten x-Wert spiegeln, damit die Rueckseiten UVs so wie gedacht sind (in Tests und Bechreibungen). Ob das
        // gut oder richtig ist, sei mal dahingestellt.
        if (isback) {
            x = 1 - x;
        }
        Vector2 uv = new Vector2(x, 1 - y);
        if (uvmap != null) {
            uv = uvmap.getUvFromNativeUv(uv);
        }
        return uv;
    }

    /**
     * Liefert die "echte" xy-Position eines Vertex auf seiner "Surface".
     * <p/>
     * <p/>
     * vertexindex muss der Originalvertex sein, nicht das Duplikat.
     * 24.11.2015: Ich glaube, Duplikate gibt es hier nicht mehr.
     *
     * @return
     */
    /*24.11.15 public Vector2 getVertexLocation(int vertexindex) {
        //Vector2 loc = vloc.get(vertexindex);
        //if (loc == null)
        //7    throw new RuntimeException("Location not found for vertex index " + vertexindex + " vloc=" + dumpVloc());
        //logger.debug("found location" + loc + " for vertex index " + vertexindex);
       // Vector3 v = ver
        Vector2 loc = new Vector2();
        return loc;
    }*/

    /**
     * Die UvMap ist optional.
     *
     * @return
     */
    public int addFace3(int a, int b, int c, List</*7.2.18 Native*/Vector3> vertices, UvMap1 uvmap) {
        Vector2 uv0 = calcVertexLocation(vertices.get(a), uvmap);
        Vector2 uv1 = calcVertexLocation(vertices.get(b), uvmap);
        Vector2 uv2 = calcVertexLocation(vertices.get(c), uvmap);

        Face3 face = new Face3(a, b, c, uv0, uv1, uv2);
        getFaces().add(face);
        int index = getFaces().size();
        //logger.debug(Util.format("Face3  a=%d,b=%d,c=%d  at %d, uv0=%f, uv1=%f, uv2=%f", a, b, c, index,uv0,uv1,uv2));
        return index;
    }

    /**
     * Die UvMap ist optional.
     * Die Liste der Vertices wird reverse in die Face gehangen, damit sie CCW ist.
     *
     * @return
     */
    public int addFaceN(int size, List</*7.2.18 Native*/Vector3> vertices, UvMap1 uvmap) {
        Vector2[] uv = new Vector2[size];
        int[] index = new int[size];
        for (int i = 0; i < size; i++) {
            index[i] = size-i-1;
            uv[i] = calcVertexLocation(vertices.get(index[i]), uvmap);
        }
        FaceN face = new FaceN(index, uv);
        getFaces().add(face);
        int idx = getFaces().size();
        //logger.debug(Util.format("Face3  a=%d,b=%d,c=%d  at %d, uv0=%f, uv1=%f, uv2=%f", a, b, c, index,uv0,uv1,uv2));
        return idx;
    }

    /*24.11.15 String dumpVloc() {
        String s = "";
        for (Integer key : vloc.keySet()) {
            s += "" + key + ":(" + vloc.get(key) + "),";
        }
        return s;
    }*/
}




