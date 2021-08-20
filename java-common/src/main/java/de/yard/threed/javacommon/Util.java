package de.yard.threed.javacommon;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by thomass on 08.02.16.
 */
public class Util {
    public static float getFloat(byte[] buf, int offset) {
        ByteBuffer bytebufr = ByteBuffer.wrap(buf, offset, 4);//MA31 LoaderBTG.sizeof_float);
        bytebufr.order(ByteOrder.LITTLE_ENDIAN);
        float f = bytebufr.getFloat();
        //System.out.println("f=" + f);
        return f;
    }

    public static void setFloat(byte[] buf, int offset,float f) {
        ByteBuffer bytebufr = ByteBuffer.wrap(buf, offset, 4);//MA31 LoaderBTG.sizeof_float);
        bytebufr.order(ByteOrder.LITTLE_ENDIAN);
        bytebufr.putFloat(f);
    }
    
    public static double getDouble(byte[] buf, int offset) {
        ByteBuffer bytebufr = ByteBuffer.wrap(buf, offset, 8);//MA31 LoaderBTG.sizeof_double);
        bytebufr.order(ByteOrder.LITTLE_ENDIAN);
        double d = bytebufr.getDouble();
        return d;
    }

}
