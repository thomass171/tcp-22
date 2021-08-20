package de.yard.threed.core.buffer;

/**
 * Useful for tools.
 *
 * Created by thomass on 13.06.16.
 */
public interface NativeOutputStream {
    void writeInt(int i);
    void writeFloat(float f);
    //void writeChars(String s);
    //29.12.17: Deprecated wegen des 0 bytes.
    @Deprecated
    void writeString(String s);
    void writeByte(byte b);

    void close();
}
