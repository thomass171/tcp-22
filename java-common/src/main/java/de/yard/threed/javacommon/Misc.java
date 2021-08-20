package de.yard.threed.javacommon;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by thomass on 10.06.16.
 */
public class Misc{
    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream fos = null;

        try {
            fos = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(fos);
            o.writeObject(obj);
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
            System.err.println(e);
        }

        return fos.toByteArray();
    }

    public static Object deserialize(byte[] obj) {
        ObjectInputStream o = null;
        try {
            o = new ObjectInputStream(new ByteArrayInputStream(obj));
            return o.readObject();
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
        return null;
    }

}
