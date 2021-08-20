package de.yard.threed.core;

import de.yard.threed.core.platform.NativeStringHelper;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 15.02.16.
 * <p/>
 * Analog zu Apache Commons
 */
public class StringUtils {
    static NativeStringHelper sh = Platform.getInstance().buildStringHelper();
    
    public static String substringBefore(String str, String separator) {
        if (separator == null)
            return str;
        if (str == null)
            return null;
        int pos = indexOf(str,separator);
        if (pos == -1) {
            return str;
        }
        return substring(str,0, pos );
    }

    public static String substringBeforeLast(String str, String separator) {
        if (separator == null)
            return str;
        if (str == null)
            return null;
        int pos = lastIndexOf(str,separator);
        if (pos == -1) {
            return str;
        }
        return substring(str,0, pos );
    }

    public static String substringAfter(String str, String separator) {
        if (separator == null)
            return str;
        if (str == null)
            return null;
        int pos = indexOf(str,separator);
        if (pos == -1) {
            return "";
        }
        return substring(str,pos + length(separator));
    }

    public static String substringAfterLast(String str, String separator) {
        if (separator == null)
            return str;
        if (str == null)
            return null;
        int pos = lastIndexOf(str,separator);
        if (pos == -1) {
            return "";
        }
        return substring(str,pos + length(separator));
    }
    
    public static String trim(String s){
        return sh.trim(s);
    }

    public static String substring(String s,int index){
        return sh.substring(s,index);
    }

    public static String substring(String s,int index,int end){
        return sh.substring(s,index,end);
    }

    public static int length(String s){
        return sh.length(s);
    }

    public static char charAt(String s, int i) {
        return sh.charAt(s,i);
    }

    /**
     * 16.6.21: Should the name be "blank"? Complies to C++?
     *
     * @return
     */
    public static boolean empty(String s) {
        return s == null || length(trim(s)) == 0;
    }

    public static String[] split(String str, String s) {
        return sh.split(str,s);
    }

    /**
     * mehrere whitespaces werden wie eins behandelt, dh. "a   b" führt zu {"a","b"}
     * Der Regex "\\s+" koennte musste sollte auch bei C# gehen. Sonst geht dort auch 
     * myStr.Split(null, StringSplitOptions.RemoveEmptyEntries)
     * 
     * 14.9.16: Geht aber nicht.
     * @param str
     * @return
     */
    public static String[] splitByWhitespace(String str) {
        return sh.splitByWhitespace(str);
    }

    public static int indexOf(String s, char c) {
        return sh.indexOf(s,c);
    }

    public static int indexOf(String s, String c) {
        return sh.indexOf(s,c);
    }
    
    public static int lastIndexOf(String s, String sub) {
        return sh.lastIndexOf(s,sub);
    }

    public static boolean equalsIgnoreCase(String s1, String s2) {
        return sh.equalsIgnoreCase(s1,s2);
    }

    public static List<String> asList(String[] sa) {
        List<String> l = new ArrayList<String>();
        for (String s:sa){
            l.add(s);            
        }
        return l;
    }

    public static String replaceAll(String input, String s, String s1) {
        // own implementation for Unity compatibility
        while (indexOf (input, s) >= 0) {
            input = input.replace (s, s1);
        }
        return input;
        //return sh.replaceAll(input,s,s1);
    }

    public static String buildString(byte[] buf) {
        return sh.buildString(buf);
    }

    public static byte[] getBytes(String s) {
        return sh.getBytes(s);
    }

    public static boolean contains(String s, String c) {
        return sh.indexOf(s,c) >= 0;
    }

    public static boolean endsWith(String s, String sf) {
        int index = length(s) - length(sf);
        if (index < 0)
            return false;
        return substring(s,index).equals(sf);
    }
    
    public static String toLowerCase(String s){
        return sh.toLowerCase(s);
    }

    public static String toUpperCase(String s){
        return sh.toUpperCase(s);
    }

    public static boolean startsWith(String s, String sf) {
        int len = length(sf);
        if (len > length(s))
            return false;
        return substring(s,0,len).equals(sf);
    }
}
