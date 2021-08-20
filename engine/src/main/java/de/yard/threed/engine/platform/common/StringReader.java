package de.yard.threed.engine.platform.common;

import de.yard.threed.core.StringUtils;
import de.yard.threed.engine.util.CharHolder;

/**
 * Zeichenweises Lesen aus einem String. Ersatz für InputStream.
 * <p>
 * Created by thomass on 17.10.18.
 */
public class StringReader {
    String s;
    int pos = 0, len;

    public StringReader(String s) {
        this.s = s;
        len = StringUtils.length(s);
    }

    /**
     * return null on EOF
     * Character not possible due to C#
     *
     * @return
     */
    public CharHolder read() {
        if (pos < len) {
            return new CharHolder(StringUtils.charAt(s, pos++));
        }
        return null;
    }

    /**
     * Wird auch fuer andere Reader verwendet, darum static.
     * <p/>
     * Liefert null bei EOF.
     *
     * @return
     */
    public String readLine() /*18.8.16 throws InvalidDataException*/ {
        CharHolder ch;
        boolean gotcr, possibleInt = false, possibleFloat = false, instring = false;
        StringBuffer line = null;

        while ((ch = /*ins.*/read()) != null) {
            char c = ch.c;
            if (c == 13) {
                gotcr = true;
            } else {
                if (c == 10) {
                    if (line == null) {
                        line = new StringBuffer();
                    }
                    return line.toString();
                }
                if (line == null) {
                    line = new StringBuffer();
                }
                line.append((char) c);
            }
        }
        if (line == null) {
            return null;
        }
        //EOF
        return line.toString();
    }
    /**
     * TODO merge mit gleicher in AsciiLoader
     * @param ins
     * @return
     * @throws InvalidMazeException
     */
    /*aus maze private static String readLine(StringReader ins) throws InvalidMazeException {
        int i;
        boolean gotcr, possibleInt = false, possibleFloat = false, instring = false;
        StringBuffer s = new StringBuffer();
        List<GridElement> elements = new ArrayList<GridElement>();

        while ((i = ins.read()) != -1) {
            if (i == 13) {
                gotcr = true;
            }
            if (i == 10) {
                return s.toString();
            }
            char c = (char) i;
            s.append(c);
        }
        //wir könnten in der letzten Zeile stehen
        if (s.length() > 0) {
            // dann ist da wohl die letzte Zeile, bei der ein linefeed fehlt.
            return s.toString();
        }
        //EOF
        return null;

    }*/
}
