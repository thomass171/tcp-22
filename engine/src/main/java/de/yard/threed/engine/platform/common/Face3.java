package de.yard.threed.engine.platform.common;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.buffer.NativeOutputStream;
import de.yard.threed.core.buffer.ByteArrayInputStream;

/**
 * 17.07.2015: Das AC Format hat die UVs an den Surfacepunkten. Das ist eigentlich
 * ganz passend. Das versuch ich auch mal.
 * <p/>
 * Eigentlich gehoert hier auch eine Normale rein. Oder zumindest wäre es praktisch.
 * Notfalls koennte man sie berechnen. Das ist, wenn die Vertexreihenfolge im Face
 * richtig ist, auch zuverlaessig moeglich.
 * Das AC Format hat wohl keine Normale an einer Surface. Braucht es vielleicht auch nicht
 * unbedingt, weil über die Art der Surface die Richtung feststeht. Und ann kann man berechnen.
 * <p/>
 * <p/>
 * 2.4.16: 3DS hat die UVs aber pro Vertex, nicht an den Faces. Und Unity hat sie auch pro Vertex. Das
 * passt natürlich auch besser zum VBO. JME hat sie auch "parallel" zu den Vertices, wahrscheinlich wegen VBO.
 * ThreeJS hat sie aber anscheinend am Face.
 * <p/>
 * Date: 14.02.14
 * Time: 17:35
 */
public class Face3 extends Face {
    public int index0;
    public int index1;
    public int index2;
    public Vector2[] uv = new Vector2[3];

    /**
     * Constructor ueber Indizes in die Vertexliste
     * 21.8.15 nicht mehr ohne UV
     * 12.02.16: Es gibt es ja nicht immer zwingend UVs
     */
    public Face3(int index0, int index1, int index2, Vector2 uv0, Vector2 uv1, Vector2 uv2) {
        this.index0 = index0;
        this.index1 = index1;
        this.index2 = index2;
        uv[0] = uv0;
        uv[1] = uv1;
        uv[2] = uv2;
        hasUV = true;
    }

    public Face3(int index0, int index1, int index2) {
        this.index0 = index0;
        this.index1 = index1;
        this.index2 = index2;
        uv[0] = null;
        uv[1] = null;
        uv[2] = null;

    }

    public Face3(ByteArrayInputStream ins) {
        // cnt already read
        index0 = ins.readInt();
        index1 = ins.readInt();
        index2 = ins.readInt();
        uv[0] = new Vector2(ins.readFloat(),ins.readFloat());
        uv[1] = new Vector2(ins.readFloat(),ins.readFloat());
        uv[2] = new Vector2(ins.readFloat(),ins.readFloat());
        hasUV =true;
    }

    public void setUV(double u0, double v0, double u1, double v1, double u2, double v2) {
        uv[0] = new Vector2(u0, v0);
        uv[1] = new Vector2(u1, v1);
        uv[2] = new Vector2(u2, v2);
        hasUV = true;
    }

    public void setUV(Vector2 uv0, Vector2 uv1, Vector2 uv2) {
        uv[0] = uv0;
        uv[1] = uv1;
        uv[2] = uv2;
        hasUV = true;
    }

    public Vector2 getUV(int i) {
        if (hasUV)
            return uv[i];
        // wird zum Befuellen des VBO verwendet. Darum immer was liefern.
        return new Vector2(0, 0);
    }

    @Override
    public void serialize(NativeOutputStream outs) {
       // outs.writeInt(3);
        outs.writeInt(index0);
        outs.writeInt(index1);
        outs.writeInt(index2);
        outs.writeFloat((float)uv[0].getX());
        outs.writeFloat((float)uv[0].getY());
        outs.writeFloat((float)uv[1].getX());
        outs.writeFloat((float)uv[1].getY());
        outs.writeFloat((float)uv[2].getX());
        outs.writeFloat((float)uv[2].getY());
    }

    @Override
    public void replaceIndex(int vindex, int newindex) {
        if (vindex == index0){
            index0=newindex;
            return;
        }
        if (vindex == index1){
            index1=newindex;
            return;
        }
        if (vindex == index2){
            index2=newindex;
            return;
        }
        // to be sure. might be removed in furture?
        throw new RuntimeException("index not found");
    }

    @Override
    public int[] getIndices() {
        return new int[]{index0,index1,index2};
    }
}
