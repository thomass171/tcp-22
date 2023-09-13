package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Vector2Array;
import de.yard.threed.core.Vector3Array;
import de.yard.threed.core.platform.NativeGeometry;
import de.yard.threed.core.geometry.SimpleGeometry;

/**
 * Created by thomass on 29.01.16.
 */
public class HomeBrewGeometry implements NativeGeometry {
    //public zum Testen
    public OpenGlIndexedVBO ivbo;
    public OpenGlElementArray ea;
    // vorhalten der vertices/faces fuer collision detection (optimieren mit VBO, fuer alle Platformen, nur bestimmte Geos).
    Vector3Array vertices;
    int[] faces;

    HomeBrewGeometry(OpenGlIndexedVBO ivbo, Vector3Array vertices, int[] faces, OpenGlElementArray ea) {
        this.ivbo = ivbo;
        this.vertices = vertices;
        this.faces = faces;
        this.ea = ea;
    }

    @Override
    public String getId() {
        return "";
    }

    public static HomeBrewGeometry buildGeometry(SimpleGeometry simpleGeometry) {
        //List<Face3List> flist = new ArrayList<Face3List>();
        //flist.add(simpleGeometry.faces);
        return buildGeometry(simpleGeometry.getVertices(),/*flist*/simpleGeometry.getIndices(), simpleGeometry.getUvs(), simpleGeometry.getNormals());
    }

    /**
     * 23.9,.19: faces, uvs und normals sind optional und dürfen null sein.
     */
    public static HomeBrewGeometry buildGeometry(Vector3Array vertices, int[] indices, Vector2Array uvs, Vector3Array normals) {
        int mode = 0;
        if (normals!=null){
            mode |= OpenGlIndexedVBO.MODE_NORMAL;
        }
        // Das Kriterium fuer uv Buffer ist das Material. Wenn keine UVs in den Faces vorliegen, muessen notfalls welche genriert werden (oder es sieht halt nicht aus)
        // 11.3.16 Wenn es mehr als ein Material gibt, einfach immer einen UV Buffer anlegen.
        // 15.3.16 Entscheidender ist, dass der VBO zu den VBO Pointern im Material passt. Und das heisst, die muessen alle einheitlich sein. Sonst
        // gibt das Kuddelmuddel.
        //15.3.16 Siehe material. Nicht einfach zu klären. Erstmal immer UVs anlegen.
        //25.9.19: Naheliegend ist doch, es abhaengig von den uvs zu machen!
        if (uvs!=null/* nmaterials.size() > 1 ||*/ /*((OpenGlMaterial) nmaterial/*s.get(0))* / ).textures != null*/) {
            mode |= OpenGlIndexedVBO.MODE_TEXTURED;
        }

        // }
        OpenGlIndexedVBO ivbo = new OpenGlIndexedVBO(mode, false, false);
        OpenGlElementArray ea = new OpenGlElementArray();
        buildVBOandTriangles(vertices, indices, uvs, ivbo, ea, normals);

        return new HomeBrewGeometry(ivbo, vertices, indices, ea);
    }

    /**
     * Aus den verschiedenen Vertex/Face Abbildungen eine VBO Struktur machen.
     * Fuer die Plattformen, die so arbeiten (JME, OpenGL, Unity, aber nicht ThreeJS).
     * Erst werden die Daten in Listen konsolidiert, die dann in einen FloatBuffer kommen. Das nutzt aber nicht fuer Unity!
     * <p/>
     * Hier wird auch die Normale fuer die drei Vertices berechnet.
     * 26.1.16: Hier wird keine Normale berechnet. Das ist nicht Sache der Platform. Auch das mit der Farbe ist Kokolores.
     * 29.1.16: Doch, Normalenberechnung ist doch Sache der Plattform, wenn sie nicht bekannt ist.
     * 11.3.16: Für jede Face3List wird eine eigene Indexliste erstellt, die separat gedrawed wird. Damit können dann auch multiple materials verwendet werden.
     * 29.4.16: Wenn Facenormale vorliegen (auch wenn sie generiert wurden), wird pro Face3List ein Smoothing durchgeführt (und keine Vertices dupliziert).
     * 11.7.16: Die uebergebenen uvs werden hier nicht verwendet und es uebergibt auch keiner welche. Darum entfernt.
     * 13.7.16: Das mit mehreren Indexlisten fuer multiple material wird doch gar nicht mehr verwendet. Sowas wird doch jetzt immer gesplittet. Darum kommt
     * hier jetzt auch nur noch EINE Face3List rein. Und Normalen sollen hier auch nicht mehr berechnet werden. Sie werden bestenfalls aus dem Face genommen.
     */
    public static void buildVBOandTriangles(Vector3Array vertices, /*List<* /Face3List*/int[] faces, Vector2Array uvs, OpenGlIndexedVBO vbo, OpenGlElementArray ea, Vector3Array normals) {

        // 29.4.16: Mittlerweile werden hier keine Vertices mehr dupliziert. Ist wahrscheinlich auch gut so, zumindest pro Face3List.
        // Ha, getaeuscht! Es wird immer noch implizit kopiert durch den addRow pro Facepunkt.
        // 11.7.16: Und das kann durchaus erforderlich sein, z.B. bei twosided meshes, wenn derselbe Vertex verschiedene Normalen bekommt (z.B. AC Import).
        // Auch wenn derselbe Vertex mit unterschiedlichen uvs verwendet wird.
        // 25.9.19: Das passiert dann aber nicht hier, sondern ausserhalb.

        // Direkt die Werte aus den Listen nehmen und keine Vertices mehr duplizieren.

        int vcnt = vertices.size();
        for (int i = 0; i < vcnt; i++) {
            vbo.addRow(vertices.getElement(i), (normals==null)?null:normals.getElement(i), null/*, uv*/);
        }
        vbo.setUvs(uvs);
        //for (Face gface : /*fl.*/faces.faces) {
        //    Face3 face = (Face3) gface;
        ea.addTriangle(0, faces);//face.index0, face.index1, face.index2);
        // }

    }

}
