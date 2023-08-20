package de.yard.threed.core.resource;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.buffer.NativeByteBuffer;

/**
 * 11.10.18: Text als byte[] statt String. Das spart bei grossen Model (z.B. OHPanel.ac 32MB) echt was.
 * Dann kann ich das auch direkt zusammenführen.
 * Naja, gut und schön, bei GWT ist es kontraproduktiv, weil Javascript keine byte array hat (aber ArrayBuffer). Das macht es langsam und noch
 * speicherhungriger.
 * 
 * Evtl. mal native draus machen?
 * 19.8.23: use case for binary data is eg. loading GLTF binary models? Textures, sound, etc should be loaded directly by the platform (depending on the platform?).
 * Isn't GLTF loaded by the platform? So again: what is the purpose of binary data from bundles? Was it btg files once?
 * <p>
 * Created by thomass on 21.04.17.
 */
public class BundleData {
    //11.10.18 String nur fuer GWT. NeeNee. Auch fuer andere.
    private String s;
    private byte[]/*String*/ bs;
    public NativeByteBuffer b;
    private boolean istext;

    public BundleData(String s) {
        this.s = s;
        istext=true;
    }

    public BundleData(byte[] s,boolean isText) {
        this.bs = s;
        this.istext=isText;
    }

    public BundleData(NativeByteBuffer b,boolean isText) {
        this.b = b;
        this.istext=isText;
    }

    /**
     * return size in bytes.
     *
     * @return
     */
    public int getSize() {
        if (b != null) {
            return b.getSize();
        }
        if (s != null) {
            return StringUtils.length(s);
        }
        return bs.length;
    }

    /**
     * 11.10.18: Als Ersatz fuer direkten Zugriff auf "s". 
     * Nehm ich jetzt auch mal zur GWT Optimierung: String erst anlegen, wenn er grbraucht wird.
     *
     * @return
     */
    public String getContentAsString() {
        if (s != null) {
            return s;
        }
        if (bs!=null) {
            return StringUtils.buildString(bs);
        }
        s = StringUtils.buildString(b.getBuffer());
        return s;
    }

    /**
     * 11.10.18: Als Ersatz fuer direkten Zugriff auf "b". 
     * 
     *
     * @return
     */
    public byte[] getContentAsBytes() {
        if (b!=null){
            return b.getBuffer();
        }
        if (bs!=null) {
            return (bs);
        }
        //Das ist fuer GWT ein Alptraum
        return StringUtils.getBytes(s);
    }
    
    /**
     * Nur einmal verwendet. Flag ueber Constructor setzen.
     * @return
     */
    public boolean isText() {
        return istext;
    }
}
