package de.yard.threed.core.buffer;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;


/**
 * Ein Kuddelmuddel aus dem SG Konzept mit streamartigem LEsen mit aktueller Position 
 * und einem ArrayBuffer Konzept.
 * 13.10.18: Streamreading ist jetzt in ByteArrayInputStream.
 * 28.7.21:Renamed from sgSimpleBuffer. WebGL has its own due to byte[].
 * Created by thomass on 04.04.16.
 */
public class SimpleByteBuffer implements NativeByteBuffer {
    static Log logger = Platform.getInstance().getLog(SimpleByteBuffer.class);
    // Bloede Kruecke für den eigene Cache
    public byte[] buf;

    public SimpleByteBuffer(byte[] buf) {
        this.buf = buf;
    }

    public int get_size()    {
        return buf.length;
    }

    public float readFloat(int position) {
        float f = Platform.getInstance().getFloat(buf, position);
        return f;
    }

    public void setFloat(int position, float f) {
        Platform.getInstance().setFloat(buf, position,f);
    }
    
    @Override
    public int readUShort(int position) {
        int v = readUShort(buf, position);
        return v;
    }

    @Override
    public byte getByte(int position) {
        return buf[position];
    }

    public int readUInt(int position) {
        long v = readUInt(buf, position);
        return (int) v;
    }
    
    @Override
    public int readUByte(int position) {
        byte b = buf[position];
        return Util.byte2int(b);
    }

    @Override
    public byte[] getBuffer() {
        return buf;
    }
    
    public static int readUShort(byte[] buf, int offset) {
        int b0 = Util.byte2int(buf[offset]);
        int b1 = Util.byte2int(buf[offset + 1]);
        return (b1 << 8) + b0;
    }

    public static long readUInt(byte[] buf, int offset) {
        long b0 = Util.byte2int(buf[offset]);
        long b1 = Util.byte2int(buf[offset + 1]);
        long b2 = Util.byte2int(buf[offset + 2]);
        long b3 = Util.byte2int(buf[offset + 3]);
        return (b3 << 24) + (b2 << 16) + (b1 << 8) + b0;
    }
    
    @Override
    public int getSize() {
        return buf.length;
    }

    
};