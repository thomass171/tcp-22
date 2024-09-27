package de.yard.threed.core.geometry;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;


import java.util.ArrayList;
import java.util.List;

/**
 * Ein selbsterstellte Geometrie im Unterschied zur Native.
 * Darum ist das auch keine Ableitung der Klasse Geometry, die
 * eine NativeGeometry abbildet.
 * <p>
 * Wichtig/Nützlich z.B. um CSG verwenden zu koennen. 17.3.17: Das trifft nicht wirklich zu. SimpleGeometry ist viel besser geeignet.
 * 01.06.21: CustomGeometry hat aber die property "hasedges", die hier und da ja wichtig ist. Die hat SimpleGeometry doch nicht(?).
 *
 * Somehow this class has a confusing/overloaded design, {@link SimpleGeometryBuilder} is more straightforward.
 *
 * Date: 17.07.15
 * Time: 08:31
 */
public abstract class CustomGeometry {
    boolean dynamic = false;
    Log logger = Platform.getInstance().getLog(CustomGeometry.class);
    public boolean hasedges = false;
    // 8.6.14: Nach einigem Hin und Her sind Faces und Vertices zusammen hier, ohne
    // das dadurch eine spezielle Geometrie oder Geometrieherkunft definiert ist.
    // So kann jede Ableitung beliebig Geometrien aufbauen.
    // 29.09.14: Faces jetzt in Surface und die stattdessen hier.
    // 29.5.15: Ist jetzt in Ableitung
    //List<Surface> surfaces = new ArrayList<Surface>();
    //8.2.18: Das als native ist doch witzlos und konzeptionell fraglich. Dafuer gibt es doch SimpleGeometry.
    public List<Vector3> vertices = new ArrayList<Vector3>();
    // 19.12.16: Die Normalen aus einigen ableitenden Klassen hochgezogen. Ist optional. Praktisch wenn die
    // Normalen vorliegen oder fuer Sonderaelle nicht gut aus den Faces erzeugt werden koennen.
    // 29.12.18: Das spart das spätere fehlerträchtige Smoothing. Es sei denn, es ist explizit ein Flat Shading gewunscht.
    public List<Vector3> normals = null;

    // 19.12.16: Eine optionale Smoothing Map. Kann verwendet werden, wenn das Standard smoothen nicht geeignet ist.
    public VertexMap smoothingMap = null;

    public CustomGeometry(/*int planecnt, int verticesperplane*/) {

    }

    public int addVertex(Vector3 vertex) {
        vertices.add(vertex/*.vector3*/);
        return vertices.size() - 1;
    }


    public List</*Native*/Vector3> getVertices() {
        return vertices;
    }

    /**
     * 28.8.15: Kann jetzt Face3 und FaceN enthalten
     *
     * @return
     */
    public abstract List<FaceList> getFaceLists();

    
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * TODO: muss ueber construcotr gehen
     */
    @Deprecated
    public void setDynamic() {
        dynamic = true;
    }

    public List<Vector3> getNormals() {
        return normals;
    }
}
