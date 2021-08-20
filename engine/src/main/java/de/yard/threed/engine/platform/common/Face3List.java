package de.yard.threed.engine.platform.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Eine einfache Faceliste (nur Face3), um Signaturen zu vereinfachen.
 * 16.2.16
 *
 */
public  class Face3List {
    // um unötiges Kopieren zu vermeiden, als Face deklariert, muss aber alles Face3 sein.
    public List<Face> faces = new ArrayList<Face>();

    public Face3List(List<Face> l) {
        faces = l;
    }

    public Face3List() {

    }

    /**
     * Eine Liste von Listen in eine einzelne konvertieren.
     * 
     * @param faces3
     * @return
     */
    public static Face3List flatten(List<Face3List> faces3) {
        Face3List result = new Face3List();
        for (Face3List f : faces3){
            result.faces.addAll(f.faces);
        }
        return result;
    }

    public void add(Face3 face3) {
        faces.add(face3);
    }
}
