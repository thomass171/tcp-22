package de.yard.threed.platform.webgl;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.JavaStringHelper;

public class WebGlJavaStringHelper extends JavaStringHelper {
    @Override
    public String buildString(byte[] buf) throws CharsetException {
        // GWT throws exception on error
        try {
            return new String(buf);
        } catch (Exception e) {
            throw new CharsetException();
        }
    }
}
