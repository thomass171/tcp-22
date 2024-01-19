package de.yard.threed.core.platform;

import de.yard.threed.core.CharsetException;

/**
 * Created by thomass on 01.04.16.
 */
public interface NativeStringHelper {
    String trim(String s);

    int length(String s);

    public String substring(String s, int index);

    public String substring(String s, int index,int end);

    char charAt(String s, int i);

    String[] split(String str, String s);

    public String[] splitByWhitespace(String str) ;

    int indexOf(String s, char c);

    int indexOf(String s, String c);

    int lastIndexOf(String s, String sub);

    boolean equalsIgnoreCase(String s1, String s2);

    String buildString(byte[] buf) throws CharsetException;

    byte[] getBytes(String s);
    
    String toLowerCase(String s);
    
    String toUpperCase(String s);
}
