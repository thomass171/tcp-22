package de.yard.threed.platform.webgl;

/**
 * 12.10.18: 
 * <p>
 * Created by thomass on 01.08.16.
 */
/*public class WebGlVector2Array implements NativeVector2Array {
    static Log logger = Platform.getInstance().getLog(WebGlVector2Array.class);
    Vector2[] arr;
    Float32Array arr32;

    public WebGlVector2Array(int size) {
        arr = new Vector2[size];
        int byteLen = size * 2 * Float32Array.BYTES_PER_ELEMENT;
        ArrayBuffer buf = TypedArrays.createArrayBuffer(byteLen);
        arr32 = TypedArrays.createFloat32Array(buf);
    }

    public WebGlVector2Array(WebGlByteBuffer basedata, int byteOffset, int sizeinvec2) {
        arr = null;
        arr32 = TypedArrays.createFloat32Array(basedata.buffer, byteOffset, sizeinvec2/**4* /);
    }

    @Override
    public void setElement(int index, float x, float y) {
        logger.debug("setelement");
        if (arr != null) {
            arr[index] = new Vector2(x, y);
        }
        int pos = 2 * index;
        arr32.set(pos, x);
        arr32.set(pos + 1, y);

    }

    @Override
    public void setElement(int index, Vector2 v) {
        setElement(index, v.getX(), v.getY());
    }

    @Override
    public int size() {
        if (arr32!=null){
            return arr32.length()/2;
        }
        return arr.length;
    }

    @Override
    public Vector2 getElement(int i) {
        if (arr == null) {
            return new Vector2(arr32.get(2*i),arr32.get(2*i+1));
        }
        return arr[i];

    }
}*/