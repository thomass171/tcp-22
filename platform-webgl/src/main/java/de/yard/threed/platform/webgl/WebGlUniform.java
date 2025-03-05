package de.yard.threed.platform.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import de.yard.threed.core.platform.NativeUniform;
import de.yard.threed.core.platform.UniformType;

public abstract class WebGlUniform<T> implements NativeUniform<T> {

    JavaScriptObject uniform;

    public WebGlUniform(UniformType type) {
        uniform = buildUniform(getThreeJsType(type));
    }

    @Override
    public abstract void setValue(T value);

    /**
     * Infos: https://github.com/mrdoob/three.js/wiki/Uniforms-types
     *
     * @param type
     * @return
     */
    private static String getThreeJsType(UniformType type) {
        switch (type) {
            case FLOAT_VEC4:
                return "f";
            case SAMPLER_2D:
                return "t";
            case BOOL:
                // als integer
                //return "i";
                return "bool";
            case MATRIX3:
                return "mat3";
            case FLOAT:
                return "float";
            case FLOAT_VEC3:
                return "Vector3";
            default:
                throw new RuntimeException("unknown uniform type " + type);
                //return "unknown";
        }
    }

    public void setObject(JavaScriptObject value) {
        setObject(uniform, value);
    }

    public void setBool(boolean b) {
        setBool(uniform, b);
    }

    public void setFloat(float f) {
        setFloat(uniform, f);
    }

    public void setVector4(float x, float y, float z, float w) {
        setVector4(uniform, x, y, z, w);
    }

    private static native JavaScriptObject buildUniform(String ptype)  /*-{
        var uniform = { type: ptype, value: null };
        return uniform;
    }-*/;

    private native void setInt(JavaScriptObject uniform, int value)  /*-{
        uniform.value = value;
    }-*/;

    private native void setBool(JavaScriptObject uniform, boolean value)  /*-{
        uniform.value = value;
    }-*/;

    private native void setFloat(JavaScriptObject uniform, float value)  /*-{
        uniform.value = value;
    }-*/;

    private native void setObject(JavaScriptObject uniform, JavaScriptObject value)  /*-{
        uniform.value = value;
    }-*/;

    private native void setVector4(JavaScriptObject uniform, float x, float y, float z, float w)  /*-{
        uniform.value = new $wnd.THREE.Vector4(x, y, z, w);
    }-*/;

}
