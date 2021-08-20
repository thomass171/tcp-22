package de.yard.threed.engine;

/**
 * Date: 09.05.14
 */
/*28.6.21 public class ElementArray {
    public int[] indices;
    //20.6.14 private Buffer vboiId = null;
    // Anzahl der Triangles. Die stehen am Anfang des Array
    private int triangles;
    // Anzahl ZUSAETZLICHER Lines. Die stehen nach den Triangles (immer paarweise)
    private int lines;
    Log logger = Platform.getInstance().getLog(ElementArray.class);

    /*public ElementArray(int[] indices) {
        this.indices = indices;
        triangles = indices.length / 3; //TODO pruefen dass teilbar
        lines = 0;
    }* /

    public ElementArray(List<Integer> indices) {
        this.indices = new int[indices.size()];
        for (int index = 0; index < indices.size(); index++)
            this.indices[index] = (int)indices.get(index);
        triangles = indices.size() / 3; //TODO pruefen dass teilbar
        lines = 0;
    }

    /*public ElementArray(int[] indices, int triangles, int lines) {
        this.indices = indices;
        this.triangles = triangles;
        this.lines = lines;
        logger.debug(Util.format("%d indices, %d triangles, %d lines", indices.length, triangles, lines));

    } * /



}*/
