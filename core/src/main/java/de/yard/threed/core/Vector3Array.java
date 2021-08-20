package de.yard.threed.core;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.buffer.NativeByteBuffer;

/**
 * Preferrably something like a Float32Array optimized for the platform.
 * <p>
 * 16.10.18: No more a native interface but just a view on an underlying byte buffer.
 * <p>
 * Created by thomass on 01.08.16.
 */
public class Vector3Array {
    public NativeByteBuffer basedata;
    public int byteOffset, sizeinvec3;
    int bytestride = 12;

    public Vector3Array(NativeByteBuffer basedata, int byteOffset, int sizeinvec3) {
        this.basedata = basedata;
        this.byteOffset = byteOffset;
        this.sizeinvec3 = sizeinvec3;
    }

    public void setElement(int i, float x, float y, float z) {
        basedata.setFloat(byteOffset + i * bytestride, x);
        basedata.setFloat(byteOffset + i * bytestride + 4, y);
        basedata.setFloat(byteOffset + i * bytestride + 8, z);
    }

    public int size() {
        return sizeinvec3;
    }

    /**
     * Just for convenience and testing. Might be too inefficient for real use.
     *
     * @param i
     * @return
     */
    public Vector3 getElement(int i) {
        float f0 = basedata.readFloat(byteOffset + i * bytestride);
        float f1 = basedata.readFloat(byteOffset + i * bytestride + 4);
        float f2 = basedata.readFloat(byteOffset + i * bytestride + 8);
        return new Vector3(f0, f1, f2);
    }

    /**
     * Just for convenience and testing. Might be too inefficient for real use.
     *
     * @param i
     * @return
     */
    public void setElement(int i, Vector3 v) {
        setElement(i, (float)v.getX(), (float)v.getY(), (float)v.getZ());
    }
}
