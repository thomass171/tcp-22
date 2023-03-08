package de.yard.threed.platform.homebrew;


import de.yard.threed.core.Matrix4;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeElementArray;
import de.yard.threed.core.platform.Platform;

import de.yard.threed.javacommon.BufferHelper;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Die Daten fuer ein Element Array.
 * <p/>
 * <p/>
 * Date: 24.09.19
 */
public /*abstract*/ class OpenGlElementArray implements NativeElementArray /* implements Dumpable*/ {
    List<IndexList> l_indices = new ArrayList<IndexList>();
    // Zunaechst werden die schon bekannten Vertices angelegt.
    // Evtl. fuer Color, Normale etc duplizierte kommen spaeter hinten dran.
    private int vboId = -1;
private static boolean usesvec3 = true;
    Log logger = Platform.getInstance().getLog(OpenGlIndexedVBO.class);

    private static final int bytesPerFloat = 4;

    // Position und Color bestehen aus jeweils vier floats
    private static final int positionElementCount = (usesvec3)?3:4;
    // color sind immer 4 Floats wegen alpha
    private static final int colorElementCount = 4;
    // Texturekoordinaten sind zwei Floats (uv)
    private static final int textureElementCount = 2;
    private static final int normalElementCount = (usesvec3)?3:4;

    // Bytes per parameter
    private static final int positionByteCount = positionElementCount * bytesPerFloat;
    private static final int colorByteCount = colorElementCount * bytesPerFloat;
    private static final int textureByteCount = textureElementCount * bytesPerFloat;
    private static final int normalByteCount = normalElementCount * bytesPerFloat;

    // Byte offsets per parameter
    private static final int positionByteOffset = 0;
    private static int colorByteOffset = -1;
    private static int textureByteOffset = -1;
    private static int normalByteOffset = -1;

    // The amount of elements that a vertex has
    private static final int elementCount = positionElementCount + colorElementCount + textureElementCount;
    private int stride = -1;

    public static int totalvertexcnt = 0;
    /**
     * Zunaechst werden die schon bekannten Vertices angelegt.
     * Evtl. fuer Color, Normale etc duplizierte kommen spaeter hinten dran.
     * <p/>
     * Bei diesem Constructor werden die Farben schon mitgegeben.
     */
    public OpenGlElementArray() {
        // Die Position enthaelt jeder Buffer
        stride = positionByteCount;



       /*OGL  for (Vector3 v : vertices) {
            addVector3((OpenGlVector3) v, false);
            /*OGL if (colors != null && usecolors)
                colors.set(colors.size() - 1, v.getColor());
            if (sts != null && usetextures)
                sts.set(sts.size() - 1, v.st);* /
        }*/
    }

    @Override
    public void addTriangle(int ili, int[] i0/*, int i1, int i2*/) {
//        addFace(i0, i1, i2, (f3.normal == null) ? null : (OpenGlVector3) f3.normal.vector3, null);
        IndexList list;
        while (l_indices.size() <= ili) {
            l_indices.add(new IndexList());
        }
        list = l_indices.get(ili);
        list.l_indices=i0;

        // das muss passen
        list.triangles = i0.length/3;
    }



   
    /*111.3.16 public void addLine(int i1, int i2) {
        //TODO die Pruefungen wie fuer Faces
        l_indices.add(i1);
        l_indices.add(i2);
        lines++;
    }*/

    /**
     * Wenn der indizierte Vertex schon eine Farbe hat, muss es dieselbe sein.
     * Sonst stimmt irgendwas nicht. Auch wenn der VBO nicht colored ist und
     * eine Farbe uebergeben wird.
     */
   /*11.3.16 private void addColoredVertex(int i, Color color, OpenGlVector3 normal) {
        if (color != null) {
            //if (colors != null && color == null)
            //    throw new RuntimeException("no color in colored at index " + i);
            if (colors == null)
                throw new RuntimeException("color in non colored VBO");
            // 10.4.15: Das mit dem equal auf color ist schon etwas merkwürdig. Wohl nur'ne Konsistenzpruefung
            if (colors.get(i) != null && !color.isEqual(colors.get(i)))
                throw new RuntimeException("already colored at index=" + i + ", total colors=" + colors.size());
            if (colors.get(i) == null)
                colors.set(i, color);
        }
        if (normal != null) {
            normals.add(normal);
        }

        l_indices.add(i);
    }*/
    public int getVboId() {
        return vboId;
    }



    /**
     * Es sollte ein VAO schon gebinded sein, sonst laesst sich der jetzt angelegte Buffer
     * spaeter nicht mehr binden.
     */
    public void setup(GlInterface gl, Matrix4 transformationmatrix) {
       build(gl, transformationmatrix);
        //totalvertexcnt += vertices.size();
        //logger.debug("setup completed totalvertexcnt="+totalvertexcnt+" : "+(float)totalvertexcnt*(12+12+8)/(1024*1024)+" MB");
      }

    public void bindBuffer() {
        //20.6 indices.bindBuffer();

    }



    //20.6 public void drawPolygon() {
    //20.6 indices.drawPolygon();

    //20.6 }

    public void unbindBuffer() {
        //20.6 indices.unbindBuffer();
    }



    /*public void rebuild(Matrix4 transformation) {
        vbo.rebuild(transformation);
    }*/

    //@Override
    public void build(GlInterface gl, Matrix4 transformationmatrix) {
        for (IndexList list : l_indices) {
            list.genBuffer(gl);
        }

        gl.exitOnGLError(gl,"rebuild");
        rebuild(gl, transformationmatrix);


    }

    /**
     * Bei Textures ein einzelner sonst zwei Buffer (ohne besonderen Grund). 20.6.14: Erstmal immer ein einzelner.
     */
    //@Override
    public void rebuild(GlInterface gl, Matrix4 transformationmatrix) {
        validate();

        gl.glBindBuffer(gl.GL_ARRAY_BUFFER(), 0);
        gl.exitOnGLError(gl,"rebuild");

        for (IndexList list : l_indices) {
            list.fillBuffer(gl);
        }
    }

    private void validate() {

    }

    public String dump(String lineseparator) {
        String s = "";


        return s;
    }

    //@Override
    /*THREED TODO public void destroyOpenGL() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        if (vboId != -1)
            GL15.glDeleteBuffers(vboId);

        // Delete the color VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        if (vbocId != -1)
            GL15.glDeleteBuffers(vbocId);

    }*/


    /**
     * Die Normalen sind pro Vertex. Darum brauchts fuer einen Marker eigentlich nur einen zusaetzlichen Vertex.
     * Da die Normale aber immer in blau sein soll, wird auch der Original dupliziert.
     * Je nach verwendetem Shader greifen die Farben aber eh nicht, sondern evtl. auch die Texturpixel.
     */
   /*11.3.16  public void addNormalMarker() {
        if (normals == null)
            throw new RuntimeException("VBO has no normals");

        if (normalmarkerstart != -1)
            throw new RuntimeException("normal marker already added");
        if (vertexmarkerstart != -1)
            throw new RuntimeException("normal marker must reside before normal marker");
        normalmarkerstart = vertices.size();
        int nativevertexcnt = vertices.size();
        for (int i = 0; i < nativevertexcnt; i++) {
            OpenGlVector3 base = vertices.get(i);
            OpenGlVector3 normal = new OpenGlVector3(base.getX(), base.getY(), base.getZ());
            addVector3(normal, true);
            normal = (OpenGlVector3) MathUtil2.add(base, normals.get(i));
            addVector3(normal, true);
            if (colors != null) {
                colors.set(normalmarkerstart + 2 * i, Color.BLUE);
                colors.set(normalmarkerstart + 2 * i + 1, Color.BLUE);
            }
            if (sts != null) {
                // Nur damit was drinsteht
                sts.set(normalmarkerstart + 2 * i, new Vector2(0, 0));
                sts.set(normalmarkerstart + 2 * i + 1, new Vector2(0, 0));
            }
            if (normals != null) {
                // Nur damit was drinsteht

                normals.set(normalmarkerstart + 2 * i, new OpenGlVector3(0, 0, 0));
                normals.set(normalmarkerstart + 2 * i + 1, new OpenGlVector3(0, 0, 0));
            }
            addLine(normalmarkerstart + 2 * i, normalmarkerstart + 2 * i + 1);
        }
    }*/

    /**
     * Die Marker sind pro Vertex. Darum brauchts fuer einen Marker eigentlich nur einen zusaetzlichen Vertex.
     * Da die Marker aber immer in gruen sein soll, wird auch der Original dupliziert.
     * Je nach verwendetem Shader greifen die Farben aber eh nicht, sondern evtl. auch die Texturpixel.
     */
   /*11.3.16  public void addVertexMarker(int vertexindex) {

        if (vertexmarkerstart != -1) {
            vertexmarkerstart = vertices.size();
        }
        OpenGlVector3 base = vertices.get(vertexindex);
        int markerindex = addVector3(base, true);
        OpenGlVector3 marker = (OpenGlVector3) MathUtil2.add(base,new OpenGlVector3(0.2f, 0.2f, 0.2f));
        addVector3(marker, true);
        if (colors != null) {
            colors.set(markerindex, Color.GREEN);
            colors.set(markerindex + 1, Color.GREEN);
        }
        if (sts != null) {
            // Nur damit was drinsteht
            sts.set(markerindex, new Vector2(0, 0));
            sts.set(markerindex + 1, new Vector2(0, 0));
        }
        if (normals != null) {
            // Nur damit was drinsteht

            normals.set(markerindex, new OpenGlVector3(0, 0, 0));
            normals.set(markerindex + 1, new OpenGlVector3(0, 0, 0));
        }
        addLine(markerindex, markerindex + 1);
    }*/


}

class IndexList {
   // List<Integer> l_indices = new ArrayList<Integer>();
   int[] l_indices ;
    protected int vboiId = -1;
    // Anzahl der Triangles. Die stehen am Anfang des Array
    protected int triangles = 0;
    // Anzahl ZUSAETZLICHER Lines. Die stehen nach den Triangles (immer paarweise)
    protected int lines = 0;

    public void genBuffer(GlInterface gl) {
        vboiId = gl.GenBuffers();
    }

    public void fillBuffer(GlInterface gl) {
        // Indexbuffer
        IntBuffer intbuffer = BufferHelper.createIntBuffer(l_indices.length);
        for (int f : l_indices) {
            intbuffer.put(f);
        }
        intbuffer.flip();
        gl.glBindBuffer(gl.GL_ELEMENT_ARRAY_BUFFER(), vboiId);
        gl.exitOnGLError(gl,"rebuild");
        gl.glBufferData(gl.GL_ELEMENT_ARRAY_BUFFER(), intbuffer, gl.GL_STATIC_DRAW());
        gl.exitOnGLError(gl,"rebuild");
        //29.1.16 gl.glBindBuffer(glcontext.GL_ELEMENT_ARRAY_BUFFER(), 0);
        gl.exitOnGLError(gl,"rebuild");

    }
}