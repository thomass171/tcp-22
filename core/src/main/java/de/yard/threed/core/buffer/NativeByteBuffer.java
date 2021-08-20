package de.yard.threed.core.buffer;

/**
 * Just an array(buffer) of bytes.
 * <p>
 * SG verwendet in BTG z.B. vielfach unsigned. Evtl. kann das hier als Spec übernommen werden, denn wo werden negative
 * Werte gebraucht? Gibt es da einen use case?
 * <p>
 * Manche Methoden schieben nutzen eine aktuelle Position (read?), andere nicht (get/set?).
 * Die stream Methoden kommen in den ByteArrayStream.
 * <p>
 * Created by thomass on 09.04.17.
 */
public interface NativeByteBuffer {
    /**
     * 4-byte int
     *
     * @return
     */
    //int readInt();
    byte getByte(int position);

    //13.6.17: Die Definition als int ist genau genommen falsch. Aber gibt es so grosse Werte wirklich?
    //Das als long macht den Umgang unhandlich und klärt auch nicht die Frage. Evtl. besser einen Exception bei zu grossen
    //Werten.
    int readUInt(int position);

    //String readString();
    float readFloat(int position);

    void setFloat(int position, float f);

    //double readDouble();
    //int remaining();
    int readUShort(int position);

    //void skip(int i);
    //NativeByteBuffer readSubbuffer(int i);
    //int readByte();
    int readUByte(int position);

    //void reset();
    byte[] getBuffer();

    int getSize();
    //Vector3Array readFloat32Array(int bytecount);
    /**
     * without moving a position.
     * @param byteOffset
     * @param byteLength
     */
    //sgSimpleBuffer getSubBuffer(int byteOffset, int byteLength);
}
