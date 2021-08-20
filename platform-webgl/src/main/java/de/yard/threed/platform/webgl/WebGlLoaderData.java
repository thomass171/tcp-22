package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;

public class WebGlLoaderData extends JavaScriptObject {
    // Overlay types always have protected, zero-arg ctors
    protected WebGlLoaderData() {
    }
    
    public final native int getDelegateid() /*-{ return this.data.delegateid; }-*/;

    public final native JavaScriptObject getNode() /*-{ return this.data.scene; }-*/;

    public static native WebGlLoaderData create(JavaScriptObject jo) /*-{ return {data : jo}; }-*/;
}
