package de.yard.threed.engine.platform.common;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.buffer.NativeOutputStream;

import de.yard.threed.core.buffer.ByteArrayInputStream;

/**
 * Date: 14.02.14
 * Time: 17:38
 * GL_QUADS gibts ab 3.1 nicht mehr (widerspricht das nicht ct BlenderRochenArtikel?) und immediate mode auch nicht
 * Erstmal nicht mehr deprecated. Wir machen einfach zwei Dreieicke draus.
 * 21.08.15: Da AC und OBJ auch Polygonfaces haben, koennen wir uns hier getrost das Leben mit einem
 * Face4 vereinfachen. Aber UVs kommen immer dazu, denn hier ist ja noch gar nicht sicher, ob die nachher entbehrlich sind.
 * <p/>
 * Mit Blick auf die Vorderseite:
 * <p/>
 * 3 - 2
 * 0 - 1
 * <p/>
 * 11.06.16: Verallgemeinert zu FaceN. Die obige Face4 Zuordnung stimmt immer noch und auch sinngemaess für hoehere.
 * 30.11.16: Anordnung ueber ShapeGeometry (cuboid) ist aber:
 * <p/>
 * 2 - 1
 * 3 - 0
 * <p/>
 */
public class FaceN extends Face {
    //02.12.16: Lists statt arrays wegen einfacherem Handling
    public int[] index;
    public Vector2[] uv = new Vector2[4];

    /**
     * Constructor fuer Face4
     * Laut
     */
    public FaceN(int index0, int index1, int index2, int index3, Vector2[] uv) {

        if (index0 < 0 || index1 < 0 || index2 < 0 || index3 < 0)
            throw new RuntimeException("Invalid Parameter");
        index = new int[4];
        this.index[0] = index0;
        this.index[1] = index1;
        this.index[2] = index2;
        this.index[3] = index3;
        this.uv = uv;
        hasUV = true;
    }

    /**
     * Constructor fuer Face4
     * Laut
     */
    public FaceN(int index0, int index1, int index2, int index3, Vector2 uv0, Vector2 uv1, Vector2 uv2, Vector2 uv3) {

        if (index0 < 0 || index1 < 0 || index2 < 0 || index3 < 0)
            throw new RuntimeException("Invalid Parameter");
        uv = new Vector2[4];
        index = new int[4];
        this.index[0] = index0;
        this.index[1] = index1;
        this.index[2] = index2;
        this.index[3] = index3;

        this.uv[0] = uv0;
        this.uv[1] = uv1;
        this.uv[2] = uv2;
        this.uv[3] = uv3;

        hasUV = true;
    }

    public FaceN(ByteArrayInputStream ins, int cnt) {
        // cnt already read
        index = new int[cnt];
        uv = new Vector2[cnt];
        for (int i = 0; i < cnt; i++) {
            index[i] = ins.readInt();
        }
        for (int i = 0; i < cnt; i++) {
            uv[i] = new Vector2(ins.readFloat(), ins.readFloat());
        }
        hasUV = true;
    }

    /**
     * Die Listen muessen CCW sein bzw. ergeben!
     *
     * @param index
     * @param uv
     */
    public FaceN(int[] index, Vector2[] uv) {
        this.index = index;
        this.uv = uv;
        hasUV = true;
    }

    @Override
    public void serialize(NativeOutputStream outs) {
        throw new RuntimeException("only Face3");
        /*outs.writeInt(index.length);
        for (int i : index) {
            outs.writeInt(i);
        }

        for (Vector2 v : uv) {
            outs.writeFloat(v.getX());
            outs.writeFloat(v.getY());
        }*/
    }

    @Override
    public void replaceIndex(int vindex, int newindex) {
        for (int i = 0; i < index.length; i++) {
            if (index[i] == vindex) {
                index[i] = newindex;
                return;
            }
        }
        // to be sure. might be removed in furture?
        throw new RuntimeException("index not found");
    }

    @Override
    public int[] getIndices() {
        return index;
    }

    /**
     * Die Reihenfolge reverten (for building the nack side of a two sided face.
     */
    public void revert() {
        int len = index.length;
        int[] nindex = new int[len];
        Vector2[] nuv = new Vector2[len];
        for (int i = 0; i < len; i++) {
            nindex[i] = index[len - i - 1];
            nuv[i] = uv[len - i - 1];
        }
        uv = nuv;
        index = nindex;
    }

    /**
     * 1) die Indexes einheitlich durch Indizes auf identische Vertices ersetzen.
     * 2) Duplikate rauswerfen.
     *
     * 3.12.16: Problematisch. Doppelte Vertices koennn Absicht sein. Siehe GeoHelper triangalate Kommentar.
     * @param vertices
     * @param uniquemap
     */
   /* public void optimize(List<Vector3> vertices, Map<Integer, Integer> uniquemap) {
        //List<Integer> toberemoved = new ArrayList<Integer>();
        int toberemoved = 0;

        for (int i = 0; i < index.length; i++) {
            index[i] = uniquemap.get(index[i]);
        }
        for (int i = 0; i < index.length; i++) {
            int idx = index[i];
            boolean exists = false;

            for (int j = i + 1; j < index.length; j++) {
                if (MathUtil2.equalsVector3(vertices.get(index[j]), vertices.get(idx), 0.000001f)) {
                    // Die Positionen i und j referenzieren einen Vertex an selber Stelle.
                    // welcher entfertn wird, duerfte egal sein. Ich nehm den letzten (j). Nee ersten, den kann ich markieren.
                    //toberemoved.add(j);
                    toberemoved++;
                    index[i] = -9000;
                    exists = true;
                }
            }
          
        }
        if (toberemoved > 0) {
            int s = 0;
            int[] nindex = new int[index.length - toberemoved];
            Vector2[] nuv = new Vector2[index.length - toberemoved];
            for (int i = 0; i < index.length; i++) {
                if (index[i] != -9000) {
                    nindex[s] = index[i];
                    nuv[s] = uv[i];
                    s++;
                }
            }
            index = nindex;
            uv = nuv;
        }
    }*/
}
