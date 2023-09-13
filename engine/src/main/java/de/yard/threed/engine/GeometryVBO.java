package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.platform.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Eine Geometry ist kein 3d Objekt, d.h. hat z.B. keine Position.
 *
 * Date: 14.02.14
 * Time: 08:31
 */
public abstract class GeometryVBO /*extends Object3D*/ {
    List<Face3> faces = new ArrayList<Face3>();
    //List<Vertex> vertices = new ArrayList<Vertex>();
    private Integer vboid = null, vbocid, indexBufferID;
    Log logger = Platform.getInstance().getLog(GeometryVBO.class);
    

    public void addFace(Face3 f) {
        faces.add(f);
    }







    /*26.1.16 public List<Vertex> getVertices() {
        return vertices;
    }*/

    public List<Face3> getFaces() {
        return faces;
    }


}
