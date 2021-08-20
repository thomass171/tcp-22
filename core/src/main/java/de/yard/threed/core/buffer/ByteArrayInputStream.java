package de.yard.threed.core.buffer;

import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Platform;

/**
 * Interessant vor allem fuer Tools etc.
 *
 * Created by thomass on 17.07.15.
 */
public class ByteArrayInputStream {
    public static String NULLPHRASE = "n u l l0";

    NativeByteBuffer buf;
    int pos = 0;

    /**
     *
     */
    public ByteArrayInputStream(NativeByteBuffer buf) {
        this.buf = buf;
    }

    public int readUShort() {
        int v = buf.readUShort( pos);
        pos += 2;
        return v;
    }
    
    public int readUInt() {
        long v = buf.readUInt( pos);
        pos += 4;
        return (int) v;
    }

    /**
     * 4-byte int
     *
     * @return
     */
    public int readInt() {
        int v = (int) buf.readUInt( pos);
        pos += 4;
        return v;
    }

    public int readByte() {
        byte b = buf.getByte(pos);
        pos++;
        return Util.byte2int(b);
    }

    public int readShort() {
        int v = buf.readUShort( pos);
        pos += 2;
        return v;
    }

    public float readFloat() {
        float f = Platform.getInstance().getFloat(buf.getBuffer(), pos);
        pos += 4;
        return f;
    }
    
    public double readDouble() {
        double d = Platform.getInstance().getDouble(buf.getBuffer(), pos);
        pos += 8;
        return d;
    }

    public void skip(int skip) {
        pos += skip;
    }

    public void reset() {
        pos=0;
    }
    
    public int remaining() {
        return buf.getSize() - pos;
    }

    public ByteArrayInputStream readSubbuffer(int len) {
        //logger.debug("offset="+offset+",i="+len);
        NativeByteBuffer newbuf = Platform.getInstance().buildByteBuffer(len);
        //byte[] newbuf = new byte[len];
        Util.arraycopy(buf.getBuffer(), pos, newbuf.getBuffer(), 0, len);
        pos += len;
        return new ByteArrayInputStream(newbuf);
    }
    
    /**
     * Z.B. für die Binary Reader wie "3DS".
     * 11.10.18: HowHowHow, was ein Driss. TODO
     * @return
     */
    public String readString() {
        String s = "";
        int i = 0;
        while (true) {
            byte b = buf.getBuffer()[pos + i++];
            if (b == 0) {
                break;
            }
            s += (char) b;
        }
        pos += i;
        if (s.equals(NULLPHRASE)) {
            return null;
        }
        return s.toString();
    }

    public byte[] getBuffer() {
        return buf.getBuffer();
    }

    public int getSize() {
        return buf.getSize();
    }
}
