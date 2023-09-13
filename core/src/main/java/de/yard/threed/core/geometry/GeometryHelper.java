package de.yard.threed.core.geometry;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.SmartArrayList;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Vector2Array;

import de.yard.threed.core.Vector3Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomass on 06.02.16.
 */
public class GeometryHelper {
    static Log logger = Platform.getInstance().getLog(GeometryHelper.class);


    /**
     * Die Facelisten in separate Geometrien aufsplitten. Erforderlich, wenn multiple materials verwendet werden.
     * Das ist ziehmlich ineffizient.
     * Eine evtl. Triangulation muss vorher passiert sein. Hier wird nur Fac3 erwartet.
     * <p/>
     * Da nur die verwendeten Vertices uebernommen werden, werden alle Faces angepasst und sind nachher
     * nicht mehr wie vorher!
     * <p/>
     * 18.7.16: Weil in SimpleGeometry normals jetzt mandatory sind, hier auch.
     *
     * @param vertices
     * @param faces
     * @param normals
     * @return
     */
    public static List<SimpleGeometry> extractSubmeshes(List</*7.2.18 Native*/Vector3> vertices, List<Face3List> faces, List<Vector3> normals) {
        if (normals == null) {
            throw new RuntimeException("no normals");
        }

        List<SimpleGeometry> list = new ArrayList<SimpleGeometry>();
        for (Face3List fl : faces) {
            List</*7.2.18 Native*/Vector3> vlist = new ArrayList</*7.2.18 Native*/Vector3>();
            Face3List flist = new Face3List();
            List<Vector3> nlist = new ArrayList<Vector3>();

            /*18.7.16 if (normals != null) {
                // dann bekommt der Submesh auch seine Normalen
                geo.getN = new ArrayList<Vector3>();
            }*/
            HashMap<Integer, Integer> indexmap = new HashMap<Integer, Integer>();
            for (Face gface : fl.faces) {
                Face3 face = (Face3) gface;
                face.index0 = handleVertex(face.index0, indexmap, vlist, vertices, nlist, normals);
                face.index1 = handleVertex(face.index1, indexmap, vlist, vertices, nlist, normals);
                face.index2 = handleVertex(face.index2, indexmap, vlist, vertices, nlist, normals);
                flist.faces.add(face);
            }
            SimpleGeometry geo = new SimpleGeometry(vlist, flist, nlist);
            list.add(geo);
        }
        return list;
    }

    private static int handleVertex(int index, HashMap<Integer, Integer> indexmap, List</*7.2.18 Native*/Vector3> newvertices, List</*7.2.18 Native*/Vector3> vertices, List<Vector3> newnormals, List<Vector3> normals) {
        Integer newindex = indexmap.get(index);
        if (newindex != null) {
            return (int) newindex;
        }
        newvertices.add(vertices.get(index));
        if (normals != null) {
            newnormals.add(normals.get(index));
        }
        int ni = newvertices.size() - 1;
        indexmap.put(index, ni);

        return ni;
    }

   /*29.7.21 private static int addFaceVertex(List<Vector3> vertices, int vindex, Vector2 uv, Vector3 normal, NativeVBO vbo) {
        int index = vbo.addRow(vertices.get(vindex), normal, uv);
        return index;
       /* nnormals.add(((JmeVector3) normal).vector3);
        nvertices.add(((JmeVector3) vertices.get(vindex)).vector3);
        int index = nvertices.size() - 1;
        indexes.add(index);
        uvs.add(new Vector2f(uv.getX(), uv.getY()));
        logger.info("adding vertex at index " + index + ": v=" + nvertices.get(index) + ", uv=" + uvs.get(uvs.size() - 1));* /

    }*/

    /**
     * Die drei Vektoren sind CCW orientiert (aus Sicht der hier berechneten Normale).
     * 3.12.16: Wenn zwei Vertices an quasi gleicher Stelle liegen, kann die Berechnung der Normelen
     * völlig falsche Ergebnisse leiefern mit entsprechend auffälliger Falschdarstellung. Darum wird in dem Fall
     * null geliefert, damit der Aufrufer das Problem lösen kann.
     * Die gelieferte Normale ist normalisiert. Sonst würde es beim Smoothing falsche Ergebnisse geben können.
     *
     * @param v0
     * @param v1
     * @param v2
     */
    private static Vector3 getNormal(/*7.2.18 Native*/Vector3 v0, /*7.2.18 Native*/Vector3 v1, /*7.2.18 Native*/Vector3 v2, int debughelper) {
        float tolerance = 0.0000001f;
        Vector3 nv1 = MathUtil2.subtract(v1, v0);
        if (nv1.length() < tolerance)
            return null;
        Vector3 nv2 = MathUtil2.subtract(v2, v0);
        if (nv2.length() < tolerance)
            return null;
        if (MathUtil2.getDistance(v1, v2) < tolerance)
            return null;
        Vector3 res = MathUtil2.getCrossProduct(nv1, nv2);
        Vector3 resn = res.normalize();
        //logger.debug("cross=" + new Vector3(res) + ",crossn=" + new Vector3(resn) + ",helper=" + debughelper);
        return resn;
    }

    public static List<Face3List> triangulate(List</*7.2.18 Native*/Vector3> vertices, List<FaceList> faces) {
        List<Face3List> faces3 = new ArrayList<Face3List>();
        for (FaceList l : faces) {
            if (l.onlyface3) {
                faces3.add(new Face3List(l.faces));
            } else {
                faces3.add(GeometryHelper.triangulate(vertices, l));
            }
        }
        return faces3;
    }

    /**
     * TODO: Das geht aber nur mit konvexen (kein Innenwinkel > 180 Grad) Faces zuverlässig.
     * 02.12.16: FaceN mit uebereinanderliegenden Vertices werden zu nur EINEM Face3!
     * <p>
     * Bei durch Extraktion entstandenen Polen liegen Vertices teilweise an derselben Stelle. Daraus
     * entshen dann abnormale Triangles, fuer eine Normale nicht berechenbar ist, weil ein Vektor 0 ist. Darum
     * solche Triangle verhindern.
     * Bloed ist allerdings, das dadurch vorhandene shared vertices elimniert werden. Das zu vermeiden erfordert aber
     * eine wirklich umfassende Optimierung. Muss aber sein, da sonst kein Smoothing geht.
     * Dazu zunächst ein Mapping von Indizes auf Indizes erstellen, dass doppelte Vertices einheitlich auf einen einzigen Vertex
     * mappt. Boah.
     * 03.12.16: Das ist alles problematisch. Doppelte Vertices können Absicht sein, darum ist wegoptimieren riskant.
     * Besser nicht. Und auch keine entarteten Triangles entfernen. Die müssen ja vielleicht einen Beitrag zum Smoothing leisten.
     * Daher versuchen, bei diesen Triangles die Normale des Nachbarn zu nehmen.
     *
     * @param facelist
     * @return
     */
    public static Face3List triangulate(List</*7.2.18 Native*/Vector3> vertices, FaceList facelist) {
        //3.12.16 Map<Integer,Integer> uniquemap = facelist.buildUniqueIndexMap(vertices);
        int debughelper = 0;
        Face3List faces3 = new Face3List();
        for (Face gface : facelist.faces) {
            if (gface instanceof Face3) {
                faces3.faces.add(gface);
            } else if (gface instanceof FaceN) {
                FaceN face = (FaceN) gface;
                // 12.6.16 Fuer FaceN faecherartig aus index0 CCW Face3 erstellen. Sind dann anders als die Face4 bisher
                // 3.12.16: Es koennen entartete Triangles entstehen, wenn zwei Vertices an derselben Stelle liegen. Für die lässt sich dann
                // nicht zuverlaessig eine Normale berechnen. Darum eine erfolgreich erstellte Normale in solche Triangles übernehmen.

                Vector3 bestnormal = null;
                List<Face3> missingnormal = new ArrayList<Face3>();
                for (int i = 2; i < face.index.length; i++) {
                    int a = (0);
                    int b = (i - 1);
                    int c = (i);
                    Face3 f3 = new Face3(face.index[a], face.index[b], face.index[c], face.uv[a], face.uv[b], face.uv[c]);
                    f3.normal = calculateFaceNormal(vertices, f3, debughelper);
                    if (f3.normal == null) {
                        missingnormal.add(f3);
                    } else {
                        bestnormal = f3.normal;
                    }
                    faces3.faces.add(f3);
                }
                for (Face3 f : missingnormal) {
                    f.normal = bestnormal;
                }
                debughelper++;
            } else {
                throw new RuntimeException("not supported face");
            }
            debughelper += 1000000;
        }
        return faces3;
    }

    /**
     * Liefert die Indices der Indices, um nachher noch die uvs finden zu koennen!
     * Es wird nur auf den unmittelbaren Vorgaenger geprueft. Alles andere scheint vage/unklar/schwer abschätzbr in der Auswirkung.
     * Das reicht aber nicht, auch der erst und letzte koennen gleich liegen. Also doch alle pruefen.
     * Wenn ein Duplikat gefunden wird, wird immer einheitlich derselbe Index verwendet.
     *
     * @param face
     * @param vertices
     * @param uniquemap
     * @return
     */
    /*private static List<Integer> findUniqueIndices(FaceN face, List<Vector3> vertices, Map<Integer, Integer> uniquemap) {
        
        return unilist;
    }*/

    /**
     * 23.11.16: TODO Besser customgeomerty bauen, um CSG verwenden zu koennen?
     *
     * @param width
     * @param height
     * @param depth
     * @return
     */
    public static SimpleGeometry buildCubeGeometry(float width, float height, float depth) {
        return Primitives.buildBox(width,height,depth);
    }

    

    /**
     * Sicherstellen, dass jedes Face eine Normale enthaelt.
     *
     * @param vertices
     * @param faces
     * @return
     */
    public static void calculateMissingFaceNormals(List</*7.2.18 Native*/Vector3> vertices, List<Face3List> faces) {
        int debughelper = 0;

        Vector3 validnormal = null;
        for (Face3List fl : faces) {
            for (Face gface : fl.faces) {
                Face3 face = (Face3) gface;
                // Mehrere normale in der Face sind obselet.
                Vector3 normal;
                if (face.normal == null) {
                    face.normal = calculateFaceNormal(vertices, face, debughelper);
                    if (face.normal == null) {
                        // dann ist das ein entartetes Triangle? Kommt bei 777-200 wohl mal vor. Vielleicht durch fehlerhafte Triangulation.
                        // 15.12.16: Die vom Vorgaenger nehmen. Das passierte frueher nicht. TODO nochmal untersuchen
                        logger.warn("no normal calculated");
                        if (validnormal == null) {
                            //throw new RuntimeException("no valid normal found");
                            logger.warn("no valid normal");
                            validnormal =new Vector3(1, 0, 0);
                        }
                        face.normal = validnormal;
                    }
                }
                validnormal = face.normal;

                debughelper++;
            }
            debughelper += 100000;
        }
    }

    public static Vector3 calculateFaceNormal(List</*7.2.18 Native*/Vector3> vertices, Face3 face, int debughelper) {
        Vector3 v0 = vertices.get(face.index0);
        Vector3 v1 = vertices.get(face.index1);
        Vector3 v2 = vertices.get(face.index2);
        return getNormal(v0, v1, v2, debughelper);
    }

    /**
     * Eine Berechnung der Vertexnormalen mit Smoothing. Die Normalenvektoren der Faces werden
     * dafuer zunaechst im Vertex addiert und danach normalisiert.
     * <p/>
     * Algorithmus aus http://schemingdeveloper.com/2014/10/17/better-method-recalculate-normals-unity/
     * <p/>
     * Dieser einfache Algorithmus geht aber nicht, wenn das smoothing von einem smoothingangle oder einer smoothinfgroup abhaengt.
     * <p/>
     * Wenn Eckpunkte nicht gesmoothed werden (können), werden Vertices dupliziert.
     * <p>
     * 02.12.16: Verschiedene uvs pro Vertex duerften/sollten hier nicht mehr problematisch sein, weil dafuer vorher schon Vertices dupliziert worden
     * sein sollten. Hier wird davon ausgegangen, dass die Faces ALLER Listen eine gemeinsame Fläche bilden (unter Berücksichtigung von crease/grouping).
     * Es werden keine Vertices mit denselben Werten zusammengefasst. Dafuer muss der Aufrufer sorgen. Das würde wegen evtl. unterschiedlicher uv Werte
     * ja auch gar nicht gehen.
     * <p>
     * 15.12.16: Jetzt mit allgemeingültiger SmoothingMap statt crease.
     *
     * @param vertices
     * @param faces
     * @return
     */
    public static List<Vector3> calculateSmoothVertexNormals(List</*7.2.18 Native*/Vector3> vertices, List<Face3List> faces, /*Degree crease,*/ SmoothingMap smoothingMap) {

        if (smoothingMap != null) {
            // 15.12.16: DER Weg
            // Initialbefuellung
            Vector3[] normals = new Vector3[vertices.size()];
            for (int i = 0; i < normals.length; i++) {
                normals[i] = new Vector3(0, 0, 0);
            }

            for (int index : smoothingMap.map.keySet()) {
                for (Face3 face : smoothingMap.map.get(index)) {
                    // Mehrere normale in der Face sind obselet.
                    Vector3 facenormal = face.normal;
                    normals[index] = MathUtil2.add(normals[index], facenormal);
                    //normals[face.index1] = MathUtil2.add(normals[face.index1], normal0);
                    //normals[face.index2] = MathUtil2.add(normals[face.index2], normal0);
                }
            }
            List<Vector3> normallist = new ArrayList<Vector3>();
            for (int i = 0; i < normals.length; i++) {
                normallist.add(normals[i].normalize());
            }
            return normallist;
        }

        return (List<Vector3>) Util.notyet();
        // 15.12.16: Ab hier deprecated
        /*if (crease == null) {
            // Initialbefuellung
            Vector3[] normals = new Vector3[vertices.size()];
            for (int i = 0; i < normals.length; i++) {
                normals[i] = Platform.getInstance().buildVector3(0, 0, 0);
            }

            // Die einfache Form des Smoothing
            for (Face3List fl : faces) {
                for (Face gface : fl.faces) {
                    Face3 face = (Face3) gface;
                    //Vector3 v0 = vertices.get(face.index0);
                    //Vector3 v1 = vertices.get(face.index1);
                    //Vector3 v2 = vertices.get(face.index2);
                    // Mehrere normale in der Face sind obselet.
                    Vector3 normal0 = face.normal;
                    //normal0=Platform.getInstance().buildVector3(1,0,0);
                    normals[face.index0] = MathUtil2.add(normals[face.index0], normal0);
                    normals[face.index1] = MathUtil2.add(normals[face.index1], normal0);
                    normals[face.index2] = MathUtil2.add(normals[face.index2], normal0);
                }
            }
            List<Vector3> normallist = new ArrayList<Vector3>();
            for (int i = 0; i < normals.length; i++) {
                normallist.add(MathUtil2.normalize(normals[i]));
            }
            return normallist;
        } else {
            return new SmoothingHelper().calculateSmoothVertexNormalsByGroups(vertices, faces, crease);
        }*/
    }


    /**
     * Teil aus fruehere Platform.buildMeshG() Methode.
     * 10.3.16: Da auch WebGl und Opengl keine multiple material koennen, den extract aus JME hierauf gezogen. Auch weil es erstmal ganz gut funktioniert.
     * 02.05.16: Im Sinne der Vereinfachung generell keine multiple material vorsehen, auch wenn Unity es kann. Die Engine extrahiert jetzt Submeshes.
     * Ist in Model statt in Mesh, weil durch die Aufsplittung ja auch ein Model rauskommt. Die Methode ist jetzt auch wirklich nur fuer Multi Material gedacht.
     * 13.7.16: Fuer smooth Shading muessen die Normalen hier berechnet werden, weil in die Berechnung ALLE(?) Faces einfliessen muessen. Das ist aber zweifelhaft, ob das
     * beabsichtigt ist. Andererseits kann man die Entscheidung auch dem Aufrufer ueberlassen. Wenn Faces einen Vertex sharen, wird halt gesmoothed. Sonst soll
     * der Aufrufer den Vertex duplizieren. Ist aber einfacher gesagt. Wenn Modelfiles sowas nunmal so enthalten...
     * 13.7.16: Das soll jetzt DIE Stelle sein, an der Triangulation, Normalenberechnung bzw. smoothing und Vertexduplizierung stattfindet. Hier werden
     * die mehreren Facelisten entweder in eine geflattet oder in mehrere Geometrien gesplittet, je nach dem, wie der
     * Aufrufer es braucht. Das Splitten ist erforderlich, wenn es mehr als ein Material gibt.
     * Es werden keine Vertices mit denselben Werten zusammengefasst. Dafuer muss der Aufrufer sorgen, wenn das wichtig ist (und fuer smoothing duerfte es das sein).
     * 01.12.16: Die Vertexduplizierung an Kanten (z.B. Box) ist verschütt gegangen. War vorher im VBO Builder. Wo kommt denn die Idee her, das multiple Facelisten
     * einfach geflattet werden können? Das gibt es wohl bei importierten Modellen. Dann soll der Aufrufer das aber mitteilen (Parameter hasedges).
     * TODO Die beiden Parameter muessten in einen mode zusammengefasst werden, weil sich split und hasedges ueberschneiden koennen.
     * <p>
     * 15.12.16: Jetzt mit allgemeingültiger SmoothingMap statt crease.
     */
    public static List<SimpleGeometry> prepareGeometry(List</*7.2.18 Native*/Vector3> vertices, List<FaceList> faces,/* List<NativeMaterial> material,*/ List<Vector3> normals, boolean split, Degree crease, boolean hasedges, SmoothingMap smoothingMap) {
        boolean flatshading = false;
        // das G steht fuer generic faces.
        List<Face3List> faces3 = GeometryHelper.triangulate(vertices, faces);
        if (normals == null) {
            if (flatshading) {
                // never reached
            } else {
                if (faces.size() > 1 && !split && hasedges) {
                    // Dann gibt es wohl Kanten. Die ziwschen den Facelisten geshareten Vertices duplizieren.
                    // 25.1.17: Das ist fuer die Normalen wichtig. uvs erfordern vielleicht auch eine Duplizierung. Das wird aber separat gemacht.
                    /*vertices =*/
                    duplicateVertices(vertices, faces3);
                }
                // wenn zwei Vertices in verschiedenen Faces verschiedene UVs haben, duplizieren. Dann geh ich mal davon aus, dass diese Faces nicht gemeinsam gesmotthed werden
                // Beispiel egkk_tower
                duplicateVerticesDueToUv(vertices, faces3);
                // Wenn ich eine SmoothingMap bekomme, verwende ich die. Ansonsten eine StandardMap erstellen. Dazu muessen die Facenormelen noch nicht
                // bekannt sein.
                if (smoothingMap == null) {
                    smoothingMap = SmoothingHelper.buildStandardSmoothingMap(vertices, faces3);
                }
                // Sicherstellen, dass jedes Face eine Normale enthaelt.
                calculateMissingFaceNormals(vertices, faces3);
                if (crease != null) {
                    // Erst wenn die Normalen in den Faces stehen, kann ein Crease eingesetzt werden, um bisher gesharte Vertices zu trennen
                    smoothingMap.applyCrease(vertices, faces3, crease);
                }
                normals = GeometryHelper.calculateSmoothVertexNormals(vertices, faces3, /*crease,*/smoothingMap);
            }
        }
        if (faces.size() > 1 /*&& material.size() > 1*/) {
            List<SimpleGeometry> geolist;
            if (split) {
                // fuer multiple Material
                geolist = GeometryHelper.extractSubmeshes(vertices, faces3, normals);
            } else {
                geolist = new SmartArrayList<SimpleGeometry>(new SimpleGeometry(vertices, Face3List.flatten(faces3), normals));
            }
            return geolist;
        }
        if (faces3.size() == 0) {
            //1.8.16: Gibts bei Genielamp. Komisch, gab vorher keine Probleme
            faces3.add(new Face3List());
        }
        return new SmartArrayList<SimpleGeometry>(new SimpleGeometry(vertices, faces3.get(0), normals));

    }

    /**
     * wenn zwei Vertices in verschiedenen Faces verschiedene UVs haben, duplizieren. Dann geh ich mal davon aus, dass diese Faces nicht gemeinsam gesmotthed werden
     * Beispiel egkk_tower
     *
     * @param vertices
     * @param faces3
     */
    private static void duplicateVerticesDueToUv(List</*7.2.18 Native*/Vector3> vertices, List<Face3List> faces3) {
       List</*7.2.18 Native*/Vector3> nvertices = new ArrayList<Vector3>();
        // In welchem Face wurde ein Vertex verwendet. Es mag auch noch andere Faces geben, aber da sind die uvs dieselben, daher brauchts hier nur das erste Face
        HashMap<Integer, Face3> faceofvertex = new HashMap<Integer, Face3>();
        for (Face3List fl : faces3) {
            for (Face f : fl.faces) {
                Face3 face = (Face3) f;
                if (face.hasUV) {
                    // ohne uvs witzlos
                    checkFaceVertex(vertices, face, face.index0, face.uv[0], faceofvertex, nvertices);
                    checkFaceVertex(vertices, face, face.index1, face.uv[1], faceofvertex, nvertices);
                    checkFaceVertex(vertices, face, face.index2, face.uv[2], faceofvertex, nvertices);
                }
            }
        }
        vertices.addAll(nvertices);
    }

    private static void checkFaceVertex(List</*7.2.18 Native*/Vector3> vertices, Face3 face, int index, Vector2 uv, HashMap<Integer, Face3> faceofvertex, List</*7.2.18 Native*/Vector3> nvertices) {
        Face3 faceref;
        if ((faceref = faceofvertex.get(index)) == null) {
            // Vertex noch nicht verwendet.
            faceofvertex.put(index, face);
            return;
        }
        // Vertex wurde schon mal in faceref verwendet. Ich geh mal davon aus, dass ein Index nur einmal verwendet wird (darum der else).
        // Es gibt da zwar mal Anomalien, aber das ist dann doch eh krumm.
        if (faceref.index0 == index && !uv.equalsVector2(faceref.uv[0])) {
            //duplizieren
            nvertices.add(vertices.get(index));
            face.replaceIndex(index, vertices.size()+nvertices.size() - 1);
        }else {
            if (faceref.index1 == index && !uv.equalsVector2(faceref.uv[1])) {
                //duplizieren
                nvertices.add(vertices.get(index));
                face.replaceIndex(index, vertices.size() + nvertices.size() - 1);
            } else {
                if (faceref.index2 == index && !uv.equalsVector2(faceref.uv[2])) {
                    //duplizieren
                    nvertices.add(vertices.get(index));
                    face.replaceIndex(index, vertices.size() + nvertices.size() - 1);
                }
            }
        }
    }

    /**
     * Mehrfach verwendete Vertices derart duplizieren, dass jede Facelist quasi ihre eigenen hat. Erforderlich bei Kanten in der Geometrie, z.B. Boxen.
     * Die Indizes in den Facelists werden hier angepasst.
     * 5.12.16: Die Indizes in die schon vorhandenen Vertices sollen aber nicht verändert werden. Das ist für Analysen sonst total verwirrend.
     * Darum gibt es auch keine neue Liste, sondern die bestehende wird erweitert. Das heisst füer Cubes aber, dass die bisherige Symmetrie bei der uv Zuordnung
     * nicht mehr besteht? Das ist eh nur fuer generisches testen wichtig.
     *
     * @param vertices
     * @param faces3
     * @return
     */
    private static /*List<Vector3>*/void duplicateVertices(List</*7.2.18 Native*/Vector3> vertices, List<Face3List> faces3) {
        // in welche Facelist gehoert ein Vertex
        HashMap<Integer, Integer> vertexowner = new HashMap<Integer, Integer>();

        List</*7.2.18 Native*/Vector3> nvertices = new ArrayList</*7.2.18 Native*/Vector3>();
        int facelistindex = 0;
        for (Face3List fl : faces3) {
            // Pro Facelist Indizes auf alte Liste auf Indizes auf neue Liste mappen.
            HashMap<Integer, Integer> facemap = new HashMap<Integer, Integer>();
            for (Face f : fl.faces) {
                Face3 face = (Face3) f;
                face.index0 = handleVertex(vertices, face.index0, facemap, nvertices, vertexowner, facelistindex);
                face.index1 = handleVertex(vertices, face.index1, facemap, nvertices, vertexowner, facelistindex);
                face.index2 = handleVertex(vertices, face.index2, facemap, nvertices, vertexowner, facelistindex);
            }
            facelistindex++;
        }
        vertices.addAll(nvertices);
        //return nvertices;
    }

    /**
     * Liefert den neuen Index (Index in die neue Liste) dieses Vertex.
     *
     * @param vertices
     * @param index
     * @param facemap
     * @param nvertices
     * @param vertexowner
     * @return
     */
    private static int handleVertex(List</*7.2.18 Native*/Vector3> vertices, int index, HashMap<Integer, Integer> facemap, List</*7.2.18 Native*/Vector3> nvertices, HashMap<Integer, Integer> vertexowner, int facelistindex) {
        Integer i;
        /*if ((i = facemap.get(index)) == null) {
            // Noch kein Mapping für diesen Index/Vertex
            nvertices.add(vertices.get(index));
            i = nvertices.size() - 1;
            facemap.put(index, i);
        }*/
        if (vertexowner.get(index) == null) {
            // Vertex ist noch keiner Facelist zugeordnet. Kann dann so uebernommen werden.
            vertexowner.put(index, facelistindex);
            return index;
        }
        // Neumapping und evtl. neuer Vertex erforderlich
        if (vertexowner.get(index) == facelistindex) {
            // Vertex ist dieser Facelist zugeordnet. Kann dann auch so uebernommen werden.
            return index;
        }
        if ((i = facemap.get(index)) == null) {
            // Noch kein Mapping für diesen neuen Index/Vertex vorhanden
            nvertices.add(vertices.get(index));
            i = vertices.size() + nvertices.size() - 1;
            facemap.put(index, i);
        }
        return (int) i;
    }

    public static List<SimpleGeometry> prepareGeometry(List</*7.2.18 Native*/Vector3> vertices, List<FaceList> faces,/* List<NativeMaterial> material,*/ List<Vector3> normals, boolean split, boolean hasedges) {
        return prepareGeometry(vertices, faces, normals, split, null, hasedges, null);
    }

    /**
     * Auf deprecated gesetzt, weil sowas aus Effizinetgruenden vermieden werden sollte.
     *
     * @param v
     * @return
     */
    @Deprecated
    public static Vector3Array buildVector3Array(List<Vector3> v) {
        Vector3Array a = Platform.getInstance().buildVector3Array(v.size());
        for (int i = 0; i < v.size(); i++) {
            a.setElement(i, v.get(i));
        }
        return a;
    }

    /**
     * Auf deprecated gesetzt, weil sowas aus Effizinetgruenden vermieden werden sollte.
     *
     * @param v
     * @return
     */
    @Deprecated
    public static Vector2Array buildNativeVector2Array(List<Vector2> v) {
        Vector2Array a = Platform.getInstance().buildVector2Array(v.size());
        for (int i = 0; i < v.size(); i++) {
            a.setElement(i, (float)v.get(i).getX(), (float)v.get(i).getY());
        }
        return a;
    }

    /**
     * 28.12.18: Zusätzlicher Helper. Den gibt es quasi genauso schon in GenericGeometry.
     * 
     * @param cg
     * @return
     */
    public static List<SimpleGeometry> buildSimpleGeometry(CustomGeometry cg){
        // 13.7.16: Splitten duerfte nicht erforderlich sein, weil es nur ein Material gibt.
        // 01.12.16: Es kann aber Kanten geben, die Vertexduplizierung erfordern.
        List<SimpleGeometry> geos = GeometryHelper.prepareGeometry(cg.getVertices(), cg.getFaceLists(), cg.getNormals(), false,cg.hasedges);
        return geos;
    }
}

