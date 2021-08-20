package de.yard.threed.platform.webgl;

/**
 * Created by thomass on 01.08.16.
 */
/*public class WebGlVector3Array implements Vector3Array {
    static Log logger = Platform.getInstance().getLog(WebGlVector3Array.class);
    JsArray arr;
    Float32Array arr32;

    public WebGlVector3Array(int size){
        arr = (JsArray) JavaScriptObject.createArray(size);
        int byteLen = size * 3 * Float32Array.BYTES_PER_ELEMENT;
        ArrayBuffer buf = TypedArrays.createArrayBuffer(byteLen);
        arr32 = TypedArrays.createFloat32Array(buf);
    }

    public WebGlVector3Array(WebGlByteBuffer basedata, int byteOffset, int sizeinvec3) {
        arr=null;
        arr32 = TypedArrays.createFloat32Array(basedata.buffer,byteOffset,sizeinvec3/**4* /);
    }
    
    @Override
    public void setElement(int index, float x, float y, float z) {
        logger.debug("setelement");
        if (arr!=null) {
            arr.set(index, new WebGlVector3(x, y, z).vector3);
        }
        int pos = 3 * index;
        arr32.set(pos,x);
        arr32.set(pos+1,y);
        arr32.set(pos+2,z);
    }

    @Override
    public int size() {
        if (arr32!=null){
            return arr32.length()/3;
        }
        return arr.length();
    }

    @Override
    public Vector3 getElement(int i) {
        return new WebGlVector3(arr.get(i));
    }

    @Override
    public void setElement(int index, Vector3 nv) {
        arr.set(index,((WebGlVector3) nv).vector3);
        setElement(index,nv.getX(),nv.getY(),nv.getZ());
    }
}*/
