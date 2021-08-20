package de.yard.threed.javacommon;

import de.yard.threed.core.buffer.NativeOutputStream;
import de.yard.threed.core.buffer.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Moved from commonjavaandroid here.
 *
 * Created by thomass on 13.06.16.
 */
public class JAOutputStream implements NativeOutputStream {
   public /*Object*/OutputStream os;
    FloatBuffer fb = FloatBuffer.allocate(1);
    private int cnt = 0;
    
    public JAOutputStream(String filename) throws IOException {
        // ObjectOutputStream schreibt eigene Header
        //12.4.17: Sicherheisthalber keine existierenden Dateien ueberschreiben.
        if (new File(filename).exists()){
            throw new RuntimeException("file already exists:"+filename);
        }
        os = /*new ObjectOutputStream(*/new FileOutputStream(new File(filename));
    }

    public JAOutputStream(ByteArrayOutputStream bos) {
        os = bos;
    }
    
    @Override
    public void writeInt(int i) {
        try {
            ByteBuffer bb = ByteBuffer.allocate(1 * 4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            IntBuffer fb = bb.asIntBuffer();
            fb.put(i);
            os.write(bb.array(),0,4);
            cnt+=4;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeFloat(float f) {
        try {
            //os.writeFloat(f);
            ByteBuffer bb = ByteBuffer.allocate(1 * 4);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            FloatBuffer fb = bb.asFloatBuffer();
            fb.put(f);
            os.write(bb.array(),0,4);
            cnt+=4;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void writeByte(byte b) {
        try {
            os.write(b);
            cnt++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Mit 0-terminating byte damit ueber Simplebuffer gelesen werden kann.
     * 
     * @param s
     */
    @Override
    public void writeString(String s) {
        try {
            if (s == null){
                // bloede Kruecke zur Abbildung von nulls.
                s = ByteArrayInputStream.NULLPHRASE;
            }
            //os.writeInt(s.length());
            byte[] b = s.getBytes();
            os.write(b);
            // Das 0 byte muss als 1 Byte geschrieben werden, nicht als int, dann sind es 4. Oder so
            os.write(new byte[]{0});
            cnt+=b.length+1;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int size(){
        return cnt;
    }
}
