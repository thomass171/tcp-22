package de.yard.threed.core;

import de.yard.threed.core.platform.Log;

import de.yard.threed.core.platform.Platform;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Date: 01.04.14
 */
public class Util {
    //3.5.23: This class is also used during setup when a platform is not yet available.
    //static Log logger = Platform.getInstance().getLog(Util.class);


    /**
     * Eine Liste von floats formatiert in Form von label=value mit Komma getrennt liefern.
     * Wenn ein Label null ist, wird auch keine "=" ausgegeben.
     * 29.7.14: Mit 3 nakos. Gerade bei MAtrizen sind 2 doch etwas ungenau.
     *
     * @param label
     * @return
     */
    public static String formatFloats(String[] label, double f0, double f1, double f2, double f3) {
        String res = "";
        //if (label != null && label.length != f.length)
        //    throw new RuntimeException("label.length!=f.length");
       /* for (int i = 0; i < f.length; i++) {
            if (label == null || label[i] == null)
                res += ((i > 0) ? ", " : "") + Util.format("%s", buildArray(new Float(f[i])));
            else
                res += ((i > 0) ? ", " : "") + Util.format("%s=%s", new Object[]{label[i], new Float(f[i])});
        }*/
        res += formatFloats(label, f0, f1, f2) + ", ";
        res += formatFloat(label[3], f3);
        return res;
    }

    public static String formatFloats(String[] label, double f0, double f1, double f2) {
        String res = "";
        res += formatFloats(label, f0, f1) + ", ";
        res += formatFloat(label[2], f2);
        return res;
    }

    public static String formatFloats(String[] label, double f0, double f1) {
        String res = "";
        res += formatFloat(label[0], f0) + ", ";
        res += formatFloat(label[1], f1);
        return res;
    }

    public static String formatFloat(String label, double f) {
        if (label == null)
            return Util.format("%s", buildArray(f));
        else
            return Util.format("%s=%s", new Object[]{label, f});
    }

    public static String format(String f, int i0) {
        return format(f, buildArray(i0));
    }

    public static String format(String f, int i0, int i1) {
        return format(f, new Object[]{i0, i1});
    }

    public static String format(String format, Object[] args) {
        int index = 0;
        int pos;
        while ((pos = StringUtils.indexOf(format, '%')) != -1) {
            FormatClassifier fc = new FormatClassifier(format, pos);
            switch (fc.formatc) {
                case 's':
                    format = StringUtils.substring(format, 0, pos) + args[index++] + StringUtils.substring(format, pos + 2);
                    break;
                case 'c':
                    format = StringUtils.substring(format, 0, pos) + fc.format((Character) args[index++]) + StringUtils.substring(format, pos + fc.len/*2*/);
                    break;
                case 'd':
                    format = StringUtils.substring(format, 0, pos) + fc.format((Integer) args[index++]) + StringUtils.substring(format, pos + fc.len/*2*/);
                    break;
                case 'f':
                    format = StringUtils.substring(format, 0, pos) + args[index++] + StringUtils.substring(format, pos + 2);
                    break;
                case 'x':
                    format = StringUtils.substring(format, 0, pos) + Integer.toHexString(Integer.parseInt(args[index++].toString())) + StringUtils.substring(format, pos + 2);
                    break;
                default:
                    return "invalid format:" + fc.formatc;
            }
        }
        return format;
    }

    public static String format(double value, int total, int precision) {
        String s = "" + value;
        String[] parts = StringUtils.split(s, "\\.");
        if (parts.length == 1) {
            return s;
        }
        s = parts[1];
        if (precision == 0) {
            return parts[0];
        }
        while (s.length() > precision) {
            s = StringUtils.substring(s, 0, s.length() - 1);
        }
        return parts[0] + "." + s;
    }

    public static Object[] buildArray(Object o) {
        return new Object[]{o};
    }

    /**
     * No "..." due to C#
     * Braucht C# "static List<T> buildList<T>(T o0, T o1)"
     *
     * @return
     */
    public static <T> List<T> buildList(T o0, T o1) {
        List<T> list = new ArrayList<T>();
        list.add(o0);
        list.add(o1);
        return list;
    }

    public static <T> List<T> buildList(T o0) {
        List<T> list = new ArrayList<T>();
        list.add(o0);
        return list;
    }

    /*public static <T>T[] buildArrayFromList(List<T> o) {
    //public static Object[] buildArrayFromList(List o) {
       return new T[]{};
    }*/

    public static Object notyet(String msg) {
        if (true)
            throw new RuntimeException("not yet: " + msg);
        return null;
    }

    public static Object notyet() {
        if (true)
            throw new RuntimeException("not yet");
        return null;
    }

    public static Object nomore() {
        if (true)
            throw new RuntimeException("no more");
        return null;
    }

    /**
     * GWT hat kein Character.isLetter
     *
     * @param c
     * @return
     */
    public static boolean isLetter(char c) {
        int val = (int) c;

        return inRange(val, 65, 90) || inRange(val, 97, 122);
    }

    /**
     * GWT hat kein Character.isLetter
     *
     * @param c
     * @return
     */
    public static boolean isDigit(char c) {
        int val = (int) c;

        return inRange(val, 48, 57);
    }

    /**
     * Checks if an int value is in a range.
     *
     * @param value value to check
     * @param min   min value
     * @param max   max value
     * @return whether value is in the range, inclusively.
     */
    public static boolean inRange(int value, int min, int max) {
        return (value <= max) & (value >= min);
    }

    public static int byte2int(byte b) {
        int i = b;
        if (b < 0) {
            i += 256;
        }
        //logger.debug("byte2int:"+i);
        return i;
    }


    int byte2int(byte[] buf, int pos) {
        return (buf[pos] << 24) + (buf[pos + 1] << 16) + (buf[pos] << 8) + (buf[pos] << 0);
    }


    public static void arraycopy(byte[] buf, int offset, byte[] newbuf, int newoffset, int len) {
        for (int i = 0; i < len; i++) {
            newbuf[newoffset + i] = buf[offset + i];
        }
    }

    /**
     * In einer Menge von int Werten denjenigen liefern, der refi am naechsten liegt.
     * Das koennte man bestimmt noch durch sortieren optimieren.
     *
     * @param integers
     * @param refi
     * @return
     */
    public static int findClosestIntInSet(Collection<Integer> integers, int refi) {
        int bestdiff = Integer.MAX_VALUE;
        int bestint = 0;
        for (int v : integers) {
            int diff = Math.abs(refi - v);
            if (diff < bestdiff) {
                bestdiff = diff;
                bestint = v;
            }
        }
        return bestint;
    }

    public static double parseDouble(String s) {
        return java.lang.Double.parseDouble(s);
    }

    public static float parseFloat(String s) {
        return java.lang.Float.parseFloat(s);
    }

    /**
     * Might be used with isNumeric().
     */
    public static int parseInt(String s) {
        return java.lang.Integer.parseInt(s);
    }

    public static long parseLong(String s) {
        return java.lang.Long.parseLong(s);
    }

    public static boolean parseBoolean(String s) {
        if (s == null) {
            return false;
        }
        return StringUtils.toLowerCase(s).equals("true");
    }

    public static int atoi(String data) {
        return (int) atol(data);
    }

    public static Point parsePoint(String s) {
        String[] parts = StringUtils.split(s, ",");
        if (parts.length != 2) {
            return null;
        }
        return new Point(parseInt(parts[0]), parseInt(parts[1]));
    }

    /**
     * etwas halbherziger atoi Nachbau.
     *
     * @param data
     * @return
     */
    public static long atol(String data) {
        int len = StringUtils.length(data);
        if (len == 0)
            return 0;
        char first = StringUtils.charAt(data, 0);
        if (!Util.isDigit(first) && first != '-') {
            return 0;
        }
        int index = 1;
        while (index < len && Util.isDigit(StringUtils.charAt(data, index))) {
            index++;
        }
        return Long.parseLong(StringUtils.substring(data, 0, index));
    }

    public static boolean isNumeric(String data) {
        int len = StringUtils.length(data);
        if (len == 0)
            return false;
        char first = StringUtils.charAt(data, 0);
        if (!Util.isDigit(first) && first != '-') {
            return false;
        }
        int index = 1;
        while (index < len) {
            if (!Util.isDigit(StringUtils.charAt(data, index))){
                return false;
            }
            index++;
        }
        return true;
    }

    /**
     * etwas halbherziger atof Nachbau.
     *
     * @param data
     * @return
     */
    public static float atof(String data) {
        return (float) atod(data);
    }

    /**
     * etwas unklar, wie es enden darf. Aber der echte atod liest ohne Fehler bis zum ersten ungueltigen Zeichen.
     * 29.12.16: FG scheint "voll" mit unsauberen Werten, Z.B. "0-000162". Warum von hinten pruefen? Besser bei next weitermachen. Booaah.
     * 05.01.17:  Jetzt ganz anders. Solange Exceptions auftreten hinten abschneiden.
     *
     * @param data
     * @return
     */
    public static double atod(String data) {
        int len;
        do {
            try {
                double d = Util.parseDouble(data);
                return d;
                // allgemeine Exception wegen C# und GWT. TODO atod/atol Platform?
            } catch (java.lang.Exception e) {
                // kei stacktrace, das gibt zviel muell
                //4.4.17 logger.warn("atod failed for "+data);
            }
            len = StringUtils.length(data);
            if (len <= 1) {
                return 0;
            }
            data = StringUtils.substring(data, 0, len - 1);
        } while (len > 1);
        return 0;
        
        /*char getFirst = StringUtils.charAt(data, 0);
        if (!Util.isDigit(getFirst) && getFirst != '-') {
            return 0;
        }
        char last = StringUtils.charAt(data, len - 1 );
        while (!Util.isDigit(last) && last != '.' && last != 'f') {
            logger.warn("atod truncating " + data);
            data = StringUtils.substring(data, 0, len - 1);
            len--;
            if (len <= 0) {
                logger.warn("atod len underflow. possible problem");
                return 0;
            }
            last = StringUtils.charAt(data, len - 1);
        }
        return Util.parseDouble(data);*/
    }
    
    /*public static String itoa(int i) {
        return Integer.toString(i);
    }*/


    /**
     * Manchmal will man explizit auf den Primitive casten.
     * <p/>
     * Zur Vereinfachung der C# Konvertierung.
     *
     * @param iv
     * @return
     */
    public static int intValue(Integer iv) {
        return (int) iv;
    }

    /**
     * 22.11.16: Obs da was besseres gibt?
     *
     * @param list
     * @return
     */
    /*16.6.21 public int[] toArrayInt(List<Integer> list) {
        int[] a = new int[list.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = (int) list.get(i);
        }
        return a;
    }*/

    /**
     * 22.11.16: Obs da was besseres gibt?
     *
     * @param
     * @return
     */
    /*16.6.21 public Vector2[] toArrayVector2(List<Vector2> list) {
        Vector2[] a = new Vector2[list.size()];
        for (int i = 0; i < a.length; i++) {
            a[i] = list.get(i);
        }
        return a;
    }*/
    public static double toRadians(double angle) {
        return ((angle * Math.PI) / 180);
    }

    /**
     * Einen Rotation im Einheitskreis durchführen. Winkel 0 zeigt nach (1,0).
     * Es werden immer segments+1 Punkte erzeugt.
     * 12.11.18: Koennte durch CircleRotator deprecvated sein.
     *
     * @return
     */
    public static void buildRotation(int segments, double startangle, double spanangle, RotateDelegate rotateDelegate) {
        double stepangle = spanangle / segments;

        for (int i = 0; i <= segments; i++) {
            double angle = startangle + i * stepangle;
            //float u = angle / MathUtil2.PI2;
            double x = (Math.cos(angle));
            double y = (Math.sin(angle));
            rotateDelegate.point(i, x, y);
        }
    }

    public static boolean isTrue(String s) {
        if (s == null) {
            return false;
        }
        return s.equals("1") || StringUtils.equalsIgnoreCase(s, "true");
    }

    public static boolean isFalse(String s) {
        if (s == null) {
            return false;
        }
        return s.equals("0") || StringUtils.equalsIgnoreCase(s, "false");
    }


    /**
     * Einen double auf precision Nachkommastellen runden.
     * Das geht prinzipbedingt gar nicht immer. Darum gibts die Methode
     * wohl auch nicht offiziell.
     */
    public static double roundDouble(double value, int precision) {
        double factor = 1;
        for (int i = 0; i < precision; i++) {
            factor *= 10;
        }
        return Math.round(value * factor) / factor;
    }

    public static double roundDouble(double value) {
        return Math.round(value);
    }

    /**
     * 29.8.19 ist mir jetzt zu kompliziert generisch.
     *
     * @param size
     * @return
     */
    public static int[][] buildPermutation(int size) {
        if (size == 2) {
            return new int[][]{
                    new int[]{0, 1},
                    new int[]{1, 0},
            };
        }
        /*int[][] lower = buildPermutation(size-1);
        for (int i=0;i<size;i++){

        }*/
        if (size == 3) {
            return new int[][]{
                    new int[]{0, 1, 2},
                    new int[]{0, 2, 1},
                    new int[]{1, 0, 2},
                    new int[]{1, 2, 0},
                    new int[]{2, 1, 0},
                    new int[]{2, 0, 1},
            };
        }
        if (size == 4) {
            return new int[][]{
                    new int[]{3, 0, 1, 2},
                    new int[]{3, 0, 2, 1},
                    new int[]{3, 1, 0, 2},
                    new int[]{3, 1, 2, 0},
                    new int[]{3, 2, 1, 0},
                    new int[]{3, 2, 0, 1},
                    new int[]{0, 3, 1, 2},
                    new int[]{0, 3, 2, 1},
                    new int[]{1, 3, 0, 2},
                    new int[]{1, 3, 2, 0},
                    new int[]{2, 3, 1, 0},
                    new int[]{2, 3, 0, 1},
                    new int[]{0, 1, 3, 2},
                    new int[]{0, 2, 3, 1},
                    new int[]{1, 0, 3, 2},
                    new int[]{1, 2, 3, 0},
                    new int[]{2, 1, 3, 0},
                    new int[]{2, 0, 3, 1},
                    new int[]{0, 1, 2, 3},
                    new int[]{0, 2, 1, 3},
                    new int[]{1, 0, 2, 3},
                    new int[]{1, 2, 0, 3},
                    new int[]{2, 1, 0, 3},
                    new int[]{2, 0, 1, 3},
            };
        }
        //lieber nicht Util.notyet();
        return null;
    }

    public static int currentTimeSeconds() {
        return (int) (Platform.getInstance().currentTimeMillis() / 1000);
    }

    public static Vector3 parseVector3(String data) {
        String[] s;
        if (StringUtils.contains(data, ",")) {
            s = StringUtils.split(data, ",");
        } else {
            s = StringUtils.split(data, " ");
        }
        if (s.length != 3) {
            throw new RuntimeException("parseString: invalid vector3 data " + data);
        }
        return new Vector3(Util.parseDouble(s[0]), Util.parseDouble(s[1]), Util.parseDouble(s[2]));
    }

    public static Quaternion parseQuaternion(String data) {
        String[] s;
        if (StringUtils.contains(data, ",")) {
            s = StringUtils.split(data, ",");
        } else {
            s = StringUtils.split(data, " ");
        }
        if (s.length != 4) {
            throw new RuntimeException("parseString: invalid quaternion data " + data);
        }
        return new Quaternion(Util.parseDouble(s[0]), Util.parseDouble(s[1]), Util.parseDouble(s[2]), Util.parseDouble(s[3]));
    }

    /**
     * GeoCoordinate has its own parser.
     */
    public static LatLon parseLatLon(String data) {
        String[] s;
        s = StringUtils.split(data, ",");
        if (s.length != 2) {
            throw new RuntimeException("parseLatLon: invalid LatLon data " + data);
        }
        return new LatLon(Util.parseDegree(s[0]), Util.parseDegree(s[1]));
    }

    public static Degree parseDegree(String data) {
        return new Degree(Util.parseDouble(data));
    }

    /**
     * C# has no streams
     */
    public static <T> List<T> distinctList(List<T> l) {
        List<T> list = new ArrayList<T>();

        for (T t : l) {
            if (!list.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }
}

class FormatClassifier {
    char formatc;
    boolean fillzero = false;
    int formattedlen = -1;
    int len = 2;

    /**
     * pos ist die position des '%'.
     */
    FormatClassifier(String s, int pos) {
        pos++;
        char c = StringUtils.charAt(s, pos++);
        if (c == '0') {
            fillzero = true;
        }
        while (Util.isDigit(c)) {
            if (formattedlen == -1) {
                formattedlen = 0;
            }
            formattedlen = formattedlen * 10 + (c - '0');
            c = StringUtils.charAt(s, pos++);
            len++;
        }
        formatc = c;
    }

    public String format(Integer i) {
        String s = "" + i;
        while (formattedlen != -1 && StringUtils.length(s) < formattedlen) {
            if (fillzero) {
                s = "0" + s;
            } else {
                s = " " + s;
            }
        }
        return s;
    }

    /**
     * fillzero duerfte hier Unsinn sein.
     *
     * @param c
     * @return
     */
    public String format(Character c) {
        String s = "" + c;
        while (formattedlen != -1 && StringUtils.length(s) < formattedlen) {
            if (fillzero) {
                s = "0" + s;
            } else {
                s = " " + s;
            }
        }
        return s;
    }

    double round(double f) {
        return Math.round(f * 10) / 10.0f;
    }

}
