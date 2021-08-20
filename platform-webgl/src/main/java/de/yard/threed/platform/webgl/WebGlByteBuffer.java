package de.yard.threed.platform.webgl;

import com.google.gwt.typedarrays.client.DataViewNative;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.DataView;
import com.google.gwt.typedarrays.shared.TypedArrays;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.buffer.NativeByteBuffer;


/**
 * Created by thomass on 21.04.17.
 */
public class WebGlByteBuffer implements NativeByteBuffer {
    // Direkt auf die Plattform, um Abhaengigkeit auf engine zu vermeiden
    Log logger = new WebGlLog(WebGlByteBuffer.class.getName());
    // See https://developer.mozilla.org/de/docs/Web/JavaScript/Reference/Global_Objects/ArrayBuffer
    ArrayBuffer buffer;
    DataView dv;
    private boolean littleEndian = true;
    int size;

    public WebGlByteBuffer(ArrayBuffer buffer) {
        //logger.debug("Building WebGlByteBuffer");
        this.buffer = buffer;
        dv = DataViewNative.create(buffer);
        size = buffer.byteLength();
    }

    public WebGlByteBuffer(ArrayBuffer buffer, int start, int len) {
        //logger.debug("Building WebGlByteBuffer");
        this.buffer = buffer;
        dv = DataViewNative.create(buffer/*, start, len*/);
        size = len;
        //offset = start;
    }

    public WebGlByteBuffer(int size) {
        this(TypedArrays.createArrayBuffer(size));
    }

    @Override
    public float readFloat(int position) {
        float i = dv.getFloat32(position, littleEndian);
        return i;
    }

    @Override
    public void setFloat(int position, float f) {
        dv.setFloat32(position, f, littleEndian);
    }

    @Override
    public int readUShort(int position) {
        int i = dv.getInt16(position, littleEndian);
        if (i < 0) {
            i += 65536;
        }
        return i;
    }

    @Override
    public byte getByte(int position) {
        return dv.getInt8(position);
    }

    @Override
    public int readUInt(int position) {
        long i = dv.getInt32(position, littleEndian);
        if (i < 0) {
            i += 4294967296l;
        }
        return (int) i;
    }

    @Override
    public int readUByte(int position) {
        int i = dv.getInt8(position);
        return i;
    }

    @Override
    public byte[] getBuffer() {
        byte[] b = new byte[size];
        for (int i = 0; i < size; i++) {
            //ob das 8 bit sicher ist ist unklar, aber solche Zeichen werden wohl gar nicht enthalten sein.
            b[i] = dv.getInt8( i);
        }
        return b;
    }

    @Override
    public int getSize() {
        return size;
    }


}
