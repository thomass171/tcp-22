package de.yard.threed.core;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.buffer.NativeByteBuffer;

/**
 * Preferrably something like a Float32Array optimized for the platform.
 * <p>
 * 16.10.18: No more a native interface but just a view on an underlying byte buffer.
 * <p>
 * Created by thomass on 01.08.16.
 */
public class Vector2Array {
    public NativeByteBuffer basedata;
    public int byteOffset, sizeinvec2;
    int bytestride = 8;

    public Vector2Array(NativeByteBuffer basedata, int byteOffset, int sizeinvec2) {
        this.basedata = basedata;
        this.byteOffset = byteOffset;
        this.sizeinvec2 = sizeinvec2;
    }

    public void setElement(int i, float x, float y) {
        basedata.setFloat(byteOffset + i * bytestride, x);
        basedata.setFloat(byteOffset + i * bytestride + 4, y);
    }

    public int size() {
        return sizeinvec2;
    }

    /**
     * Just for convenience and testing. Might be too inefficient for real use.
     *
     * @param i
     * @return
     */
    public Vector2 getElement(int i) {
        float f0 = basedata.readFloat(byteOffset + i * bytestride);
        float f1 = basedata.readFloat(byteOffset + i * bytestride + 4);
        return new Vector2(f0, f1);
    }

    /**
     * Just for convenience and testing. Might be too inefficient for real use.
     *
     * @param i
     * @return
     */
    public void setElement(int i, Vector2 v) {
        setElement(i, (float) v.getX(), (float) v.getY());
    }
}
