package de.yard.threed.core.geometry;

import de.yard.threed.core.Degree;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.SmartArrayList;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Vector2Array;

import de.yard.threed.core.Vector3Array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            // 16.9.24 skip/ignore empty lists
            if (flist.faces.size() > 0) {
                SimpleGeometry geo = new SimpleGeometry(vlist, flist, nlist);
                list.add(geo);
            }
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
        return Primitives.buildBox(width, height, depth);
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
                            validnormal = new Vector3(1, 0, 0);
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
     * Teil aus fruehere Platform.buildMeshG() Methode.
     * 10.3.16: Da auch WebGl und Opengl keine multiple material koennen, den extract aus JME hierauf gezogen. Auch weil es erstmal ganz gut funktioniert.
     * 02.05.16: Im Sinne der Vereinfachung generell keine multiple material vorsehen, auch wenn Unity es kann. Die Engine extrahiert jetzt Submeshes.
     * Ist in Model statt in Mesh, weil durch die Aufsplittung ja auch ein Model rauskommt. Die Methode ist jetzt auch wirklich nur fuer Multi Material gedacht.
     * 13.7.16: Fuer smooth Shading muessen die Normalen hier berechnet werden, weil in die Berechnung ALLE(?) Faces einfliessen muessen. Das ist aber zweifelhaft, ob das
     * beabsichtigt ist. Andererseits kann man die Entscheidung auch dem Aufrufer ueberlassen. Wenn Faces einen Vertex sharen, wird halt gesmoothed. Sonst soll
     * der Aufrufer den Vertex duplizieren. Ist aber einfacher gesagt. Wenn Modelfiles sowas nunmal so enthalten...
     * 13.7.16: This is the main location for
     * - triangulation of non Face3 faces
     * - normal calculation, ie. smoothing und vertex duplication.
     * - vertex duplication for different uvs
     * - geometry split of facelists
     * - facelist flatten
     * <p>
     * Hier werden
     * die mehreren Facelisten entweder in eine geflattet oder in mehrere Geometrien gesplittet, je nach dem, wie der
     * Aufrufer es braucht. Das Splitten ist erforderlich, wenn es mehr als ein Material gibt.
     * Es werden keine Vertices mit denselben Werten zusammengefasst. Dafuer muss der Aufrufer sorgen, wenn das wichtig ist (und fuer smoothing duerfte es das sein).
     * 01.12.16: Die Vertexduplizierung an Kanten (z.B. Box) ist verschütt gegangen. War vorher im VBO Builder. Wo kommt denn die Idee her, das multiple Facelisten
     * einfach geflattet werden können? Das gibt es wohl bei importierten Modellen. Dann soll der Aufrufer das aber mitteilen (Parameter hasedges).
     * TODO Die beiden Parameter muessten in einen mode zusammengefasst werden, weil sich split und hasedges ueberschneiden koennen.
     * <p>
     * 15.12.16: Jetzt mit allgemeingültiger SmoothingMap statt crease.
     * <p>
     * 09.09.24: split should/might be done by caller or at least in other method. Will make normal creating
     * more straightforward here. But for now we do it here.
     * Flatten at the end however is useful for having multiple facelist with multiple NormalBuilder for use cases like wheel/disc.
     * Vertices will not be shared across facelists but duplicated, so there will be no smooth shading across facelists. If that
     * is not intended, the caller should flatten the facelists preliminary.
     * BTW: The normal calculation of shaded surfaces isn't accurate in many cases due to unbalanced majority of faces connected to a vertex.
     */
    public static List<SimpleGeometry> prepareGeometry(List<Vector3> vertices, List<FaceList> faces, List<Vector3> normals, boolean split, Degree crease/*, boolean hasedgesUnused, NormalBuilder normalBuilderUnused*/) {
        // das G steht fuer generic faces.
        List<Face3List> faces3 = GeometryHelper.triangulate(vertices, faces);

        List<VertexMap> vertexMaps = null;
        if (normals == null) {
            //18.9.24 if (faces.size() > 1 && !split && hasedges) {
            // Dann gibt es wohl Kanten. Die ziwschen den Facelisten geshareten Vertices duplizieren.
            // 25.1.17: Das ist fuer die Normalen wichtig. uvs erfordern vielleicht auch eine Duplizierung. Das wird aber separat gemacht.
            /*vertices =*/
            //8.9.24 moved to Normalbuilder duplicateVertices(vertices, faces3);
            //}
            // wenn zwei Vertices in verschiedenen Faces verschiedene UVs haben, duplizieren. Dann geh ich mal davon aus, dass diese Faces nicht gemeinsam gesmotthed werden
            // Beispiel egkk_tower
            //9.9.24 List<UsedIndex> usedIndexes = duplicateVerticesDueToUv(vertices, faces3);
            // Wenn ich eine SmoothingMap bekomme, verwende ich die. Ansonsten eine StandardMap erstellen. Dazu muessen die Facenormelen noch nicht
            // bekannt sein.
            //8.9.24 New approach with NormalBuilder. What does 'unshaded' really mean? We use it as
            // flag for deciding which NormalBuilder
            // to use. Note: ac sometimes mixes unshaded with crease, but for unshaded crease is ignored.
            // See README.md.
            List<NormalBuilder> normalBuilder = new ArrayList<>();
            //if (normalBuilder == null) {
            for (int i = 0; i < faces.size(); i++) {

                //normalBuilder = SmoothingHelper.buildStandardSmoothingMap(vertices, faces3);
                if (faces.get(i).isShaded()) {
                    normalBuilder.add(new SmoothShadingNormalBuilder(crease));
                } else {
                    normalBuilder.add(new FlatShadingNormalBuilder());
                }
            }
            //}
            calculateMissingFaceNormals(vertices, faces3);

            // be prepared for different NormalBuilder per facelist which shouldn't share vertices.
            // So create vertex maps per facelist and duplicate vertices as needed.
            // As a consequence, there will be no smooth shading across facelists!
            vertexMaps = VertexMap.prepareVertexMaps(vertices, faces3);
            /*8.9.24
            // Sicherstellen, dass jedes Face eine Normale enthaelt.
            calculateMissingFaceNormals(vertices, faces3);
            if (crease != null) {
                // Erst wenn die Normalen in den Faces stehen, kann ein Crease eingesetzt werden, um bisher gesharte Vertices zu trennen
                smoothingMap.applyCrease(vertices, faces3, crease);
            }
            normals = GeometryHelper.calculateSmoothVertexNormals(vertices, faces3, /*crease,* /smoothingMap);
            */
            Map<Integer, Vector3> normalsMap = new HashMap<Integer, Vector3>();
            for (int i = 0; i < faces3.size(); i++) {
                normalsMap.putAll(normalBuilder.get(i).calculateVertexNormals(vertices, faces3.get(i), vertexMaps.get(i)));
            }
            normals = new ArrayList<>();
            while (normals.size() < vertices.size()) {
                normals.add(null);
            }
            for (Integer i : normalsMap.keySet()) {
                normals.set(i, normalsMap.get(i).normalize());
            }
            for (int i = 0; i < normals.size(); i++) {
                if (normals.get(i) == null) {
                    logger.warn("missing normal");
                    normals.set(i, new Vector3());
                }
            }
        }

        // 9.9.24 Do this AFTER normal calculation because it might break information about connected faces.
        // wenn zwei Vertices in verschiedenen Faces verschiedene UVs haben, duplizieren. Dann geh ich mal davon aus, dass diese Faces nicht gemeinsam gesmotthed werden
        // Beispiel egkk_tower
        List<UsedIndex> usedIndexes = duplicateVerticesDueToUv(vertices, faces3, normals);

        if (faces.size() > 1 /*&& material.size() > 1*/) {
            List<SimpleGeometry> geolist;
            if (split) {
                // fuer multiple Material
                geolist = GeometryHelper.extractSubmeshes(vertices, faces3, normals);
            } else {
                SimpleGeometry geo = new SimpleGeometry(vertices, Face3List.flatten(faces3), normals);
                geo.vertexMaps = vertexMaps;
                geo.usedIndexes = usedIndexes;
                geolist = new SmartArrayList<SimpleGeometry>(geo);
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
     * 10.9.24: Normals also need to be duplicated.
     *
     * @param vertices
     * @param faces3
     */
    private static List<UsedIndex> duplicateVerticesDueToUv(List</*7.2.18 Native*/Vector3> vertices, List<Face3List> faces3, List<Vector3> normals) {
        List<Vector3> nvertices = new ArrayList<Vector3>();
        List<Vector3> nnormals = new ArrayList<Vector3>();
        // In welchem Face wurde ein Vertex verwendet. Es mag auch noch andere Faces geben, aber da sind die uvs dieselben, daher brauchts hier nur das erste Face
        HashMap<Integer, Face3> faceofvertex = new HashMap<Integer, Face3>();
        List<UsedIndex> usedIndexes = new ArrayList<>();

        for (Face3List fl : faces3) {
            for (Face f : fl.faces) {
                Face3 face = (Face3) f;
                if (face.hasUV) {
                    // ohne uvs witzlos
                    face.index0 = checkFaceVertex(vertices, face, face.index0, face.uv[0], faceofvertex, nvertices, usedIndexes, normals, nnormals);
                    face.index1 = checkFaceVertex(vertices, face, face.index1, face.uv[1], faceofvertex, nvertices, usedIndexes, normals, nnormals);
                    face.index2 = checkFaceVertex(vertices, face, face.index2, face.uv[2], faceofvertex, nvertices, usedIndexes, normals, nnormals);
                }
            }
        }
        vertices.addAll(nvertices);
        normals.addAll(nnormals);
        return usedIndexes;
    }

    private static int checkFaceVertex(List<Vector3> vertices, Face3 face, int indexInFace, Vector2 uv,
                                       HashMap<Integer, Face3> faceofvertex, List<Vector3> nvertices, List<UsedIndex> usedIndexes,
                                       List<Vector3> normals, List<Vector3> nnormals) {
        /*Face3 faceref;
        if ((faceref = faceofvertex.get(indexInFace)) == null) {
            // Vertex noch nicht verwendet.
            faceofvertex.put(indexInFace, face);
            return;
        }
        // Vertex "indexInFace" already used for a face. Assume an index is only used once per face, so we can use "else".
        // 5.9.24: It might happen that we duplicate a vertex more than once just because we do not track the duplicate in
        // "faceofvertex". Maybe even more often for twosided. But that should be no problem, just a little waste of resources.
        // tracking not easy?!
        if (faceref.index0 == indexInFace && !uv.equalsVector2(faceref.uv[0])) {
            fixIndexOfFaceForDifferentUV(vertices, face, indexInFace,faceofvertex, nvertices);
        } else {
            if (faceref.index1 == indexInFace && !uv.equalsVector2(faceref.uv[1])) {
                fixIndexOfFaceForDifferentUV(vertices, face, indexInFace,faceofvertex, nvertices);
            } else {
                if (faceref.index2 == indexInFace && !uv.equalsVector2(faceref.uv[2])) {
                    fixIndexOfFaceForDifferentUV(vertices, face, indexInFace,faceofvertex, nvertices);
                }
            }
        }*/
        /* case 2:
                // index found but different uv
                usedIndexes.add(new Pair(indexInFace, uv));
                fixIndexOfFaceForDifferentUV(vertices, face, indexInFace, faceofvertex, nvertices);
                usedIndexes.add(new Pair(indexInFace, uv));
*/

        boolean foundIndex = false;
        for (int i = 0; i < usedIndexes.size(); i++) {
            UsedIndex p = usedIndexes.get(i);
            if (p.index == indexInFace) {
                foundIndex = true;
                if (p.uv.equalsVector2(uv)) {
                    // found with same uv. Nothing to do, Just keep it
                    return indexInFace;
                } /*else {
                    return 2;
                }*/
            }
            // do we know a duplicate?
            if (p.duplicateOf != -1 && p.duplicateOf == indexInFace) {
                if (p.uv.equalsVector2(uv)) {
                    // found duplicate of indexInFace with same uv. We can be sure that the original will
                    // not fit due to uv. So replace in face.
                    return p.duplicateOf;
                } /*else {
                    return 2;
                }*/
            }
        }
        if (foundIndex) {
            // 25.9.24 not really helpful logger.debug("Duplicating index ");

            // found index, but not expected nor duplicated fits. Duplicate it.
            nvertices.add(vertices.get(indexInFace));
            nnormals.add(normals.get(indexInFace));
            int newIndex = vertices.size() + nvertices.size() - 1;
            usedIndexes.add(new UsedIndex(newIndex, uv, indexInFace));
            return newIndex;
        }
        // index not found at all. Just register with uv and keep it
        usedIndexes.add(new UsedIndex(indexInFace, uv));
        return indexInFace;
    }

    private static void fixIndexOfFaceForDifferentUV(List<Vector3> vertices, Face3 face, int indexInFace, HashMap<Integer, Face3> faceofvertex, List<Vector3> nvertices) {
        nvertices.add(vertices.get(indexInFace));
        int newIndex = vertices.size() + nvertices.size() - 1;
        face.replaceIndex(indexInFace, newIndex);
        //faceofvertex.put(newIndex, face);
    }


    public static List<SimpleGeometry> prepareGeometry(List</*7.2.18 Native*/Vector3> vertices, List<FaceList> faces,/* List<NativeMaterial> material,*/ List<Vector3> normals, boolean split, boolean hasedges) {
        return prepareGeometry(vertices, faces, normals, split, null/*, hasedges, null*/);
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
            a.setElement(i, (float) v.get(i).getX(), (float) v.get(i).getY());
        }
        return a;
    }

    /**
     * 28.12.18: Zusätzlicher Helper. Den gibt es quasi genauso schon in GenericGeometry.
     *
     * @param cg
     * @return
     */
    public static List<SimpleGeometry> buildSimpleGeometry(CustomGeometry cg) {
        // 13.7.16: Splitten duerfte nicht erforderlich sein, weil es nur ein Material gibt.
        // 01.12.16: Es kann aber Kanten geben, die Vertexduplizierung erfordern.
        List<SimpleGeometry> geos = GeometryHelper.prepareGeometry(cg.getVertices(), cg.getFaceLists(), cg.getNormals(), false, cg.hasedges);
        return geos;
    }

    /**
     * Ray and vertices need to be in same space. Assume indices are triangles.
     * Order of intersection points is non deterministic. Intersection data is in
     * same space.
     * <p/>
     * <p>
     * Die Vertexdaten sind alle im local space. Der Ray muss dahin transformiert werden.
     *
     * @return
     */
    public static List<Vector3> getRayIntersections(Vector3Array vertices, int[] indices, Vector3 origin, Vector3 direction) {
        List<Vector3> intersections = new ArrayList<Vector3>();
        for (int i = 0; i < indices.length; i += 3) {
            Vector3 intersection = null;
            Vector3 v0 = vertices.getElement(indices[i]);
            Vector3 v1 = vertices.getElement(indices[i + 1]);
            Vector3 v2 = vertices.getElement(indices[i + 2]);
            intersection = MathUtil2.getTriangleIntersection(origin, direction, v0, v1, v2);
            if (intersection != null) {
                intersections.add(intersection);
            }
            // backside also? Probably not.
            /*wohl nicht intersection = MathUtil2.getTriangleIntersection(origin, direction, v2, v1, v0);
            if (intersection != null) {
                intersections.add(intersection);
            }*/
        }
        return intersections;
    }
}

