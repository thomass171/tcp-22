package de.yard.threed.core;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeStringHelper;
import de.yard.threed.core.platform.Platform;

/**
 * 9.5.20: Also for GWT, but not C# (excluded from conversion).
 *
 * 16.1.24: Made abstract to have builder from byte[] platform dependent.
 *
 * Created by thomass on 01.04.16.
 */
public abstract class JavaStringHelper implements NativeStringHelper {

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

    /**
     * The behavior of the Java constructor when the given bytes are not valid in the default charset is unspecified.
     * If the array cannot be converted, an exception is thrown to avoid hidden malfunctioning.
     */
    @Override
    public abstract String buildString(byte[] buf) throws CharsetException;

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
