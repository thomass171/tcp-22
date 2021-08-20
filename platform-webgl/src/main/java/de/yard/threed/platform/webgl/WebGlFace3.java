package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;


/**
 * Created by thomass on 18.08.15.
 *
 * Auch hier erstmal keine setter
 */
public class WebGlFace3  {
    JavaScriptObject face3;

    public WebGlFace3(int i0, int i1, int i2) {
        face3 = buildNativeFace3(i0,i1,i2);
    }

    WebGlFace3(JavaScriptObject face3) {
        this.face3 = face3;
    }


    private static native JavaScriptObject buildNativeFace3(int i0, int i1, int i2)  /*-{
        var f = new $wnd.THREE.Face3( i0,i1,i2 );
        return f;
    }-*/;
}
