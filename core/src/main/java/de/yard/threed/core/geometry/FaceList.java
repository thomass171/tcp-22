package de.yard.threed.core.geometry;


import java.util.ArrayList;
import java.util.List;

/**
 * A simple face list (arbitrary faces).
 * 16.2.16
 */
public class FaceList {
    public List<Face> faces = new ArrayList<Face>();
    // Flag for optimizing to avoid unnecessary triangulation
    public boolean onlyface3;
    // 31.3.17: Originated from AC. Was "unshaded=false" until 18.9.24.
    private boolean shaded = true;

    public FaceList(List<Face> faces, boolean shaded) {
        this.faces = faces;
        this.shaded = shaded;
    }

    public FaceList(Face face, boolean shaded) {
        faces.add(face);
        this.shaded = shaded;
    }

    public FaceList(boolean shaded) {
        this.shaded = shaded;
    }

    public void add(Face f) {
        faces.add(f);
    }

    public boolean isShaded() {
        return shaded;
    }
}
