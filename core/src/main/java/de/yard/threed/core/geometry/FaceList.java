package de.yard.threed.core.geometry;




import java.util.ArrayList;
import java.util.List;

/**
 * Eine einfache Faceliste (beliebige Faces), um Signaturen zu vereinfachen.
 * 16.2.16
 *
 */
public  class FaceList   {
    public List<Face> faces = new ArrayList<Face>();
    // Ein Flag zur Optimierung, um unnötiges Kopieren/triangulieren zu vermeiden.
    public boolean onlyface3;
    // 31.3.17: Flag nur fuer AC import um anzuzeigen, das surface unshaded ist.
    public boolean unshaded = false;

    public FaceList(List<Face> faces) {
        this.faces = faces;
    }

    public FaceList(Face face) {
        faces.add(face);
    }

    public FaceList() {

    }
    
    public void add(Face f){
        faces.add(f);
    }

    /*3.12.16 problemtisch public Map<Integer, Integer> buildUniqueIndexMap(List<Vector3> vertices) {
        Map<Integer, Integer> uniquemap = new HashMap<Integer, Integer>();

        for (Face gface : facelist.faces) {

        }
    }*/
}
