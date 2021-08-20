package de.yard.threed.engine;

/**
 * Zwei Vertex Buffer Object, eins fuer die Vertices und eins fuer die Color
 * Date: 11.04.14
 */
/*28.6.21public class ColoredVBO /*extends IndexedVBO* / {
    ArrayList<Vector3> vertices = new ArrayList<Vector3>();
    ArrayList<Color> colors = new ArrayList<Color>();
    private int vboId = 0;
    private int vbocId = 0;

    /*public ColoredVBO() {

    }

    public ColoredVBO(int[] indices) {
          super(indices);
    } * /

    /**
     * Farbe ergibt sich aus den Vertices
     * @param vertices
     */
    /*public ColoredVBO(List<Vertex> vertices) {
        for (Vertex v : vertices)
            addVertex(v.getPosition(),v.getColor());
    }   */

    /**
     * Farbe der Vertices wird ignoriert
     * /
  /*  public ColoredVBO(List<Vertex> vertices, Color c) {
        for (Vertex v : vertices)
            addVertex(v.getPosition(),c);
    }   * /

    public void addVertex(Vector3 position, Color color) {
        vertices.add(position);
        colors.add(color);
    }

    /*THREED //@Override
    public void build(Matrix4 transformationmatrix) {
        vboId = GL15.glGenBuffers();

        // Create a new VBO for the indices and select it (bind) - COLORS
        vbocId = GL15.glGenBuffers();

         rebuild(transformationmatrix);
    }* /


}*/
