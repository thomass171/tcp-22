package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.Util;

/**
 * Created by thomass on 17.07.15.
 */
public class WebGlCommon {
    public static void alert(String msg) {
        alertNative(msg);
    }

    public static float getFloat(byte[] buf, int offset) {
        // JsArrayInteger jai = new JsArrayInteger();
        //Float32ArrayNative float32Array = Float32ArrayNative.create(1); 
        //float32Array.;
     /*ArrayBuffer abuf = ArrayBufferNative.create(4);
     JsArrayInteger a = JsArrayUtils.readOnlyJsArray(buf);     
     Float32ArrayNative.create(buf, 0, 1).get(0);
        return 0 ;*/
        //4.3.16: Das geht schlicht mit dem Buffergedöhn nicht!
        //Little Endian
        int v = (Util.byte2int(buf[offset + 3]) << 24) + (Util.byte2int(buf[offset + 2]) << 16) + (Util.byte2int(buf[offset + 1]) << 8) + (Util.byte2int(buf[offset + 0]));
        float f = Float.intBitsToFloat(v);
        return f;
    }

    /**
     * 16.10.18: Das ist Driss
     * @param buf
     * @param offset
     * @param f
     */
    public static void setFloat(byte[] buf, int offset,float f) {
      Util.notyet();
        int v = (Util.byte2int(buf[offset + 3]) << 24) + (Util.byte2int(buf[offset + 2]) << 16) + (Util.byte2int(buf[offset + 1]) << 8) + (Util.byte2int(buf[offset + 0]));
        int  i = Float.floatToIntBits(f);
        //return f;
    }
    
    private static native JavaScriptObject alertNative(String msg)  /*-{
        alert(msg);
    }-*/;

    /**
     *
     * @param b64
     * @return
     */
    private static native String atob(String b64)  /*-{
        return atob(b64);
    }-*/;

    /**
     * JS hat keine echte Entsprechung zum Java getClass()
     * 
     * @param object3d
     * @return
     */
    static native String getClassname(JavaScriptObject object3d)  /*-{
        return object3d.constructor.name;
    }-*/;
}
