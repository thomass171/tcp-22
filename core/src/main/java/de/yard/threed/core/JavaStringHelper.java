package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeStringHelper;
import de.yard.threed.core.platform.Platform;

/**
 * 9.5.20: Vorsicht beim Verschieben. Ist auch für GWT. Nur nicht für C# (wird bei Konvertierung ausgenommen).
 * Jetzt aus engine nach core verschoben. Warum? Wohl wegen Tests. Aber so ganz sauber ist das nicht.
 *
 * Created by thomass on 01.04.16.
 */
public class JavaStringHelper implements NativeStringHelper {
    Log logger = Platform.getInstance().getLog(JavaStringHelper.class);

    @Override
    public String trim(String s) {
        return s.trim();
    }

    @Override
    public int length(String s) {
        return s.length();
    }

    @Override
    public String substring(String s, int index) {
        return s.substring(index);
    }

    @Override
    public String substring(String s, int index, int end) {
        return s.substring(index, end);
    }

    @Override
    public char charAt(String s, int i) {
        return s.charAt(i);
    }

    @Override
    public String[] split(String str, String s) {
        return str.split(s);
    }

    public String[] splitByWhitespace(String str) {
        return str.split("\\s+");
    }

    @Override
    public int indexOf(String s, char c) {
        return s.indexOf(c);
    }

    @Override
    public int indexOf(String s, String c) {
        return s.indexOf(c);
    }

    @Override
    public int lastIndexOf(String s, String sub) {
        return s.lastIndexOf(sub);
    }

    @Override
    public boolean equalsIgnoreCase(String s1, String s2) {
        return s1.equalsIgnoreCase(s2);
    }

    @Override
    public String buildString(byte[] buf) {
        String s;
       
        try {
            s = new String(buf);
        } catch (java.lang.Exception e) {
            // eg. Encoding/CHARSetExcpetion
            logger.error("buildString:" + e.getMessage());
            throw e;
        }
        return s;
    }

    @Override
    public byte[] getBytes(String s) {
        return s.getBytes();
    }

    @Override
    public String toLowerCase(String s) {
        return s.toLowerCase();
    }

    @Override
    public String toUpperCase(String s) {
        return s.toUpperCase();
    }
}
