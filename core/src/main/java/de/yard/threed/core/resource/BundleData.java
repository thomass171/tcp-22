package de.yard.threed.core.resource;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.buffer.NativeByteBuffer;

/**
 * 19.8.23: use case for binary data is eg. loading GLTF binary models. Textures, sound, etc should be loaded directly by the platform (depending on the platform?).
 * Isn't GLTF loaded by the platform? So again: what is the purpose of binary data from bundles? Was it btg files once?
 * 16.1.24: GLTF isn't loaded by the platform by default because of potentially required external material. So bin files are really a use case.
 * But in general, data is always a byte array. A Java string is always the result of a conversion assuming a charset. Also GWT can just guess about the charset
 * and might fail if its not UTF-8.
 * String conversion result could be cached here, but is there any use case for multiple usages?
 * <p>
 * Created by thomass on 21.04.17.
 */
public class BundleData {
    public NativeByteBuffer b;
    // the flag is just an assumption
    private boolean istext;

    public BundleData(NativeByteBuffer b, boolean isText) {
        this.b = b;
        this.istext = isText;
    }

    /**
     * return raw size in bytes.
     *
     * @return
     */
    public int getSize() {
        return b.getSize();
    }

    /**
     */
    public String getContentAsString() throws CharsetException {
        String s = StringUtils.buildString(b.getBuffer());
        return s;
    }

    /**
     * Only used once. Set in constructor.
     *
     * @return
     */
    public boolean isText() {
        return istext;
    }
}
