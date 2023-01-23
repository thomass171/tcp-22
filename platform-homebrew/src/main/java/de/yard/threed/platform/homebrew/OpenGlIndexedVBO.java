package de.yard.threed.platform.homebrew;


import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.Vector2Array;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.Color;
import de.yard.threed.javacommon.BufferHelper;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Die Daten fuer ein VBO.
 * <p/>
 * Der VBO bzw. die Bufferid vboId, der hier angelegt und befuellt wird, ist ausserhalb nicht bekannt.
 * Die Aktivierung (Binding) erfolgt nur implizit ueber das VAO. Wobei, evtl. ist das nicht erforderlich,
 * weil die Pointer schon am VAO haengen?
 * <p/>
 * Date: 26.05.14
 */
public /*abstract*/ class OpenGlIndexedVBO /*29.7.21 implements NativeVBO /* implements Dumpable*/ {
    public int mode;
    //OpenGlVBO vbo;
    //List<IndexList> l_indices = new ArrayList<IndexList>();
    // Zunaechst werden die schon bekannten Vertices angelegt.
    // Evtl. fuer Color, Normale etc duplizierte kommen spaeter hinten dran.
    ArrayList<Vector3> vertices = new ArrayList<Vector3>();
    ArrayList<Color> colors;
    ArrayList<Vector2> sts;
    //public zum testen
    public ArrayList<Vector3> normals;
    private int vboId = -1;
    private static boolean usesvec3 = true;
    Log logger = Platform.getInstance().getLog(OpenGlIndexedVBO.class);
    public static final int MODE_COLORED = 1;
    public static final int MODE_TEXTURED = 2;
    public static final int MODE_NORMAL = 4;
    // erster MArker Vector in vertices
    private int normalmarkerstart = -1;
    // weitere Marker Vector in vertices
    private int vertexmarkerstart = -1;

    private static final int bytesPerFloat = 4;

    // Position und Color bestehen aus jeweils vier floats
    private static final int positionElementCount = (usesvec3) ? 3 : 4;
    // color sind immer 4 Floats wegen alpha
    private static final int colorElementCount = 4;
    // Texturekoordinaten sind zwei Floats (uv)
    private static final int textureElementCount = 2;
    private static final int normalElementCount = (usesvec3) ? 3 : 4;

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

    GlInterface glcontext;

    public static int totalvertexcnt = 0;

    /**
     * Zunaechst werden die schon bekannten Vertices angelegt.
     * Evtl. fuer Color, Normale etc duplizierte kommen spaeter hinten dran.
     * <p/>
     * Bei diesem Constructor werden die Farben schon mitgegeben.
     */
    public OpenGlIndexedVBO(/*List<Vector3> vertices,*/ int mode, boolean usecolors, boolean usetextures) {
        this.mode = mode;
        // Die Position enthaelt jeder Buffer
        stride = positionByteCount;

        if ((mode & MODE_COLORED) > 0) {
            colors = new ArrayList<Color>();
            colorByteOffset = 0 + stride;
            stride += colorByteCount;
        }

        if ((mode & MODE_TEXTURED) > 0) {
            sts = new ArrayList<Vector2>();
            textureByteOffset = stride;
            stride += textureByteCount;
        }

        if ((mode & MODE_NORMAL) > 0) {
            normals = new ArrayList<Vector3>();
            normalByteOffset = stride;
            stride += normalByteCount;
        }

       /*OGL  for (Vector3 v : vertices) {
            addVector3((OpenGlVector3) v, false);
            /*OGL if (colors != null && usecolors)
                colors.set(colors.size() - 1, v.getColor());
            if (sts != null && usetextures)
                sts.set(sts.size() - 1, v.st);* /
        }*/
        glcontext = OpenGlContext.getGlContext();
    }

    //@Override
    public int addRow(Vector3 position, Vector3 normal, Vector2 dummy) {
        vertices.add((position));
        if (colors != null)
            colors.add(null);
        //if (sts != null)
        //    sts.add(uv);
        if (normals != null) {
            if (normal == null) {
                int h = 9;
            }
            normals.add((normal));
        }
        return vertices.size() - 1;
    }

    /*@Override
    public void addTriangle(int ili, int[] i0/*, int i1, int i2* /) {
//        addFace(i0, i1, i2, (f3.normal == null) ? null : (OpenGlVector3) f3.normal.vector3, null);
        IndexList list;
        while (l_indices.size() <= ili) {
            l_indices.add(new IndexList());
        }
        list = l_indices.get(ili);
        list.l_indices=i0;

        // das muss passen
        list.triangles = i0.length/3;
    }*/

    //@Override
    public void setUvs(Vector2Array uvs) {
        if (sts != null) {
            for (int i = 0; i < uvs.size(); i++) {
                sts.add(uvs.getElement(i));
            }
        }
    }

    /**
     * Vertices bekommen noch keine Farbe.
     *
     * @param vectors
     */
    public OpenGlIndexedVBO(List<Vector3> vectors) {
        for (Vector3 v : vectors) {
            addVector3(v, false);
        }
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
     * @returns Index des neuen Vertex
     */
    private int addVector3(Vector3 v, boolean formarker) {
        if (!formarker && (normalmarkerstart != -1 || vertexmarkerstart != -1))
            throw new RuntimeException("No more vertices possible, marker exist");
        vertices.add(v);
        if (colors != null)
            colors.add(null);
        if (sts != null)
            sts.add(null);
        if (normals != null)
            normals.add(null);
        return vertices.size() - 1;
    }

    /**
     * Es sollte ein VAO schon gebinded sein, sonst laesst sich der jetzt angelegte Buffer
     * spaeter nicht mehr binden.
     */
    public void setup(GlInterface gl, Matrix4 transformationmatrix) {
        //21.6 if (indices == null)
        //21.6     indices = new ElementArray(l_indices);
        //logger.debug(Util.format("setup with %d vertices and %d index lists", vertices.size(), l_indices.size()));
        //21.6 indices.setup();
        build(gl, transformationmatrix);
        totalvertexcnt += vertices.size();
        //logger.debug("setup completed totalvertexcnt="+totalvertexcnt+" : "+(float)totalvertexcnt*(12+12+8)/(1024*1024)+" MB");
    }

    public void bindBuffer() {
        //20.6 indices.bindBuffer();

    }

    /**
     * Triangles zeichnen (oder als wireframe)
     * 24.9.19: Das sollte woanders hin, raus aus die Buffer.
     * Returns false on GL error.
     */
    public static boolean drawElements(GlInterface glcontext, OpenGlIndexedVBO vbo, OpenGlElementArray ea, int indexlistindex, boolean wireframe, Log logger) {
        //glcontext.glBindBuffer(glcontext.GL_ARRAY_BUFFER(), vboId);
        boolean success = true;
        IndexList list = ea.l_indices.get(indexlistindex);
        // Der Element Buffer wird nicht auch ueber das VAO gebinded. Sonst koennte man auch nicht mehrere draws mit demselben VAO machen.
        glcontext.glBindBuffer(glcontext.GL_ELEMENT_ARRAY_BUFFER(), list.vboiId);
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "drawElements");
        //20.6 indices.drawElements(wireframe);
        final int sizeofunsignedint = 4;
        //logger.debug("drawElements: glcontext="+glcontext.dump(""));
        if (wireframe) {
            // 8.5.14: Geht wireframe nur mit geometry? Mittlerweile doch immer, oder?
            //  Check auf teilbar durch 3
            if (list.l_indices.length % 3 != 0 || list.l_indices.length != list.triangles * 3) {
                throw new RuntimeException("nicht teilbar durch 3");
            }

            for (int i = 0; i < list.triangles; i++) {
                glcontext.glDrawElements(glcontext.GL_LINE_LOOP(), 3, glcontext.GL_UNSIGNED_INT(), i * 3 * sizeofunsignedint);
            }
            if (list.lines > 0) {
                glcontext.glDrawElements(glcontext.GL_LINES(), list.lines * 2, glcontext.GL_UNSIGNED_INT(), list.triangles * 3 * sizeofunsignedint);
            }
        } else {
            glcontext.glDrawElements(glcontext.GL_TRIANGLES(), list.triangles * 3, glcontext.GL_UNSIGNED_INT(), 0);
            //25.9.19 nicht abbrechen, sondern reporten OpenGlContext.getGlContext().exitOnGLError(glcontext,"drawElements");
            if (OpenGlContext.getGlContext().hadGLError(glcontext, "drawElements", logger)) {
                success = false;
            }

            if (list.lines > 0) {
                glcontext.glDrawElements(glcontext.GL_LINES(), list.lines * 2, glcontext.GL_UNSIGNED_INT(), list.triangles * 3 * sizeofunsignedint);
            }
        }
        glcontext.glBindBuffer(glcontext.GL_ELEMENT_ARRAY_BUFFER(), 0);
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "drawElements");
        return success;
    }

    /*THREED TODO public void drawPolygon() {
        //29.814: Muss der hier nicht rein?
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
        GL11.glDrawElements(GL11.GL_LINE_LOOP, l_indices.size(), GL11.GL_UNSIGNED_INT, 0);
    }*/

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
        /*for (IndexList list : l_indices) {
            list.genBuffer(gl);
        }
        OpenGlContext.getGlContext().exitOnGLError(glcontext,"rebuild");*/

        vboId = gl.GenBuffers();
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "rebuild");

        rebuild(gl, transformationmatrix);
        // Bei vielen (z.B.40000) dauer der Dump Ewigkeiten
        //if (vertices.size() < 200)
        //    logger.debug(dump("\n"));
    }

    /**
     * Bei Textures ein einzelner sonst zwei Buffer (ohne besonderen Grund). 20.6.14: Erstmal immer ein einzelner.
     */
    //@Override
    public void rebuild(GlInterface gl, Matrix4 transformationmatrix) {
        validate();
        FloatBuffer verticesBuffer = BufferHelper.createFloatBuffer(vertices.size() * (stride / bytesPerFloat));
        for (int i = 0; i < vertices.size(); i++) {
            Vector3 v = transformationmatrix.transform(vertices.get(i));
            v = vertices.get(i);

            verticesBuffer.put(getElements(v, !usesvec3));
            if (colors != null) {
                Color c = colors.get(i);
                verticesBuffer.put(c.getR());
                verticesBuffer.put(c.getG());
                verticesBuffer.put(c.getB());
                verticesBuffer.put(c.getAlpha());
            }
            if (sts != null) {
                Vector2 st = sts.get(i);
                verticesBuffer.put((float) st.getX());
                verticesBuffer.put((float) st.getY());
            }
            if (normals != null)
                verticesBuffer.put(getElements(normals.get(i), !usesvec3));
        }
        verticesBuffer.flip();

        gl.glBindBuffer(glcontext.GL_ARRAY_BUFFER(), vboId);
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "rebuild");
        gl.glBufferData(glcontext.GL_ARRAY_BUFFER(), verticesBuffer, glcontext.GL_STATIC_DRAW());
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "rebuild");
        int attribindex = 0;
        gl.VertexAttribPointer(attribindex++, positionElementCount, glcontext.GL_FLOAT(),
                false, stride, positionByteOffset);
        OpenGlContext.getGlContext().exitOnGLError(glcontext, "rebuild");
        if (colors != null) {
            gl.VertexAttribPointer(attribindex++, colorElementCount, glcontext.GL_FLOAT(),
                    false, stride, colorByteOffset);
            OpenGlContext.getGlContext().exitOnGLError(glcontext, "rebuild");
        }
        if (sts != null) {
            gl.VertexAttribPointer(attribindex++, textureElementCount, glcontext.GL_FLOAT(),
                    false, stride, textureByteOffset);
            OpenGlContext.getGlContext().exitOnGLError(glcontext, "rebuild");
        }
        if (normals != null) {
            gl.VertexAttribPointer(attribindex++, normalElementCount, glcontext.GL_FLOAT(),
                    false, stride, normalByteOffset);
            OpenGlContext.getGlContext().exitOnGLError(glcontext, "rebuild");
        }
        /*gl.glBindBuffer(glcontext.GL_ARRAY_BUFFER(), 0);
        OpenGlContext.getGlContext().exitOnGLError(glcontext,"rebuild");

        for (IndexList list : l_indices) {
            list.fillBuffer(gl);
        }*/
    }

    private void validate() {
        if (colors != null && vertices.size() != colors.size()) {
            throw new RuntimeException(Util.format("vertices.size(%d) != colors.size(%d) ", vertices.size(), colors.size()));
        }
        if (sts != null && vertices.size() != sts.size()) {
            throw new RuntimeException(Util.format("vertices.size(%d) != sts.size(%d) ", vertices.size(), sts.size()));
        }
        if (normals != null && vertices.size() != normals.size()) {
            throw new RuntimeException(Util.format("vertices.size(%d) != normals.size(%d) ", vertices.size(), normals.size()));
        }
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i) == null)
                throw new RuntimeException(Util.format("vertex at %d isType null ", i));
        }
        for (int i = 0; colors != null && i < colors.size(); i++) {
            if (colors.get(i) == null)
                throw new RuntimeException(Util.format("color at %d isType null ", i));
        }
        for (int i = 0; sts != null && i < sts.size(); i++) {
            if (sts.get(i) == null)
                throw new RuntimeException(Util.format("sts at %d isType null ", i));
        }
        for (int i = 0; normals != null && i < normals.size(); i++) {
            if (normals.get(i) == null)
                throw new RuntimeException(Util.format("normals at %d isType null ", i));
        }
    }

    public String dump(String lineseparator) {
        String s = "";

        for (int i = 0; i < vertices.size(); i++) {
            Vector3 v = vertices.get(i);
            s += "vertex " + i + ":";
            s += Util.formatFloats(new String[]{"x", "y", "z"}, v.getX(), v.getY(), v.getZ());
            if (colors != null) {
                Color c = colors.get(i);
                s += " " + Util.formatFloats(new String[]{"r", "g", "b"}, c.getR(), c.getG(), c.getB());
            }
            if (sts != null) {
                Vector2 st = sts.get(i);
                s += " " + Util.formatFloats(new String[]{"s", "t"}, st.getX(), st.getY());
            }
            if (normals != null) {
                Vector3 n = normals.get(i);
                s += " " + Util.formatFloats(new String[]{"x", "y", "z"}, n.getX(), n.getY(), n.getZ());
            }

            s += "\n";
        }
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

    /**
     * XYZW instead of XYZ
     *
     * @return
     */
    public static float[] getElements(Vector3 v, boolean mitw) {
        float[] out = new float[mitw ? 4 : 3];
        int i = 0;

// Insert XYZW elements
        out[i++] = (float) v.getX();
        out[i++] = (float) v.getY();
        out[i++] = (float) v.getZ();
        if (mitw)
            out[i++] = 1f;
        return out;
    }

}
