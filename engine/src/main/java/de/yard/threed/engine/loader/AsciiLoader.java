package de.yard.threed.engine.loader;

import de.yard.threed.core.Util;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

import de.yard.threed.core.Color;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 12.02.16.
 */
public abstract class AsciiLoader extends AbstractLoader {
    protected int lineno = 1;

    public static int getIntValue(AcToken token) throws InvalidDataException {
        if (token.stringvalue != null)
            throw new InvalidDataException("int value expected but found string " + token.stringvalue);
        if (token.isfloat)
            throw new InvalidDataException("int value expected but found float " + token.floatvalue);
        return token.intvalue;
    }

    

    /**
     * Die Verwendung von String.split geht nicht, weil es bei "ac" Zeichenketten in Anfuehrungszeichen geben kann, die dann, wenn
     * sie ein Blank enthalten, zerstoert werden.
     */
    public AcToken[] parseLine(String ins, boolean commentallowed, int inrefs, Tokenizer knowntokenizer) throws InvalidDataException {
        int c;
        boolean gotcr, possibleInt = false, possibleFloat = false;
        ArrayList<AcToken> token = new ArrayList<AcToken>();
        StringBuffer currenttoken = null;
        boolean possibleObjTupel = false;
        Tokenizer tokenizer = null;
        if (ins == null) {
            return null;
        }

        // Leerzeilen ignorieren (kommt wohl schon mal vor, z.B. swan.ac)
        if (StringUtils.length(ins) == 0) {
            return new AcToken[0];
        }

        // 2.1.18: Fix fuer krude Exoten in AC. Ein Wahnsinn, was es an Properties gibt. Einfach alle ignorieren. TODO lesen, erkennen? Andere Ascii Loader betroffen?
        if (StringUtils.startsWith(ins, "lod_near=") || StringUtils.startsWith(ins, "lod_far=") ||
                StringUtils.startsWith(ins, "blend=")|| StringUtils.startsWith(ins, "hard_surf=") ||
                StringUtils.startsWith(ins, "deck=")) {
            ins = StringUtils.replaceAll(ins, "=", " ");
        }
        if (StringUtils.contains(ins,"=")){
            return new AcToken[0];
        }
        c = StringUtils.charAt(ins, 0);
        //OBJ hat # als Kommentar
        if (commentallowed && c == '#') {
            return new AcToken[0];
        }

        if (knowntokenizer != null) {
            tokenizer = knowntokenizer;
        } else {
            String type = null;
            if (Util.isLetter((char) c)) {
                type = StringUtils.substringBefore(ins, " ");
                tokenizer = getTokenizer(type);
                ins = StringUtils.substringAfter(ins, " ");
            } else {
                switch (inrefs) {
                    case 1:
                        tokenizer = new NumericIntFloat2Tokenizer(null);
                        break;
                    case 0:
                        tokenizer = new NumericFloat3Tokenizer(null);
                        break;
                    case 2:
                        tokenizer = new NumericFloat2Tokenizer(null);
                        break;
                }
            }
        }

        if (StringUtils.length(StringUtils.trim(ins)) == 0) {
            // Evtlnur fuer AC Header erforderlich
            return new AcToken[0];
        }
        if (tokenizer == null){
            getLog().warn("no tokenizer found for line; ignored:"+ins);
            return new AcToken[0];
        }
        return tokenizer.tokenize(ins);

     /*
        int index = 0;

        while (index < in.length()) {
            c = in.charAt(index);
            //OBJ hat # als Kommentar
            if (index == 0 && commentallowed && c == '#') {
                return new AcToken[0];
            }
            if (index == 0 && c == 'o') {
                // Ein OBJ Objektname
                return new AcToken[]{new AcToken("o"), new AcToken(in.substring(2))};
            }
            index++;
            if (c == ' ') {
                // Evtl. Token abschliessen, ansonsten einfach weiterlesen
                if (currenttoken != null) {
                    //System.out.println("Building token from string:"+currenttoken+
                    //        " possibleInt="+possibleInt+" possibleFloat="+possibleFloat);
                    token.add(buildToken(currenttoken, possibleInt, possibleFloat, possibleObjTupel));
                    possibleFloat = possibleInt = true;
                    currenttoken = null;
                }
            } else {
                if (c >= 32) {
                    // regulaeres Zeichen gefunden
                    if (currenttoken == null) {
                        currenttoken = new StringBuffer();
                        possibleInt = possibleFloat = true;
                    }
                    char newchar = (char) c;
                    // Dass eine Ziffe kommt, ist ja die Regel. Daher gibt es die Sonderbehandlung für Nicht Ziffern
                    if (!Character.isDigit(newchar)) {
                        if (newchar == '-') {
                            // nur als Vorzeichen (auch des Exponenten) oder im String
                            if (currenttoken.length() > 0 && Character.toUpperCase(currenttoken.charAt(currenttoken.length() - 1)) != 'E')
                                throw new InvalidAcDataException("invalid '-' at this position in line " + lineno);

                        } else if (newchar == 'x') {
                            if (!currenttoken.toString().equals("0")) {
                                // dann kann es kein hexwert sein.
                                possibleFloat = possibleInt = false;
                            }
                        } else if (Character.toUpperCase(newchar) == 'E') {
                            // Einmal darf ein e vorkommen, damit es ein float bleiben kann. Ein int kann es damit aber
                            // nicht mehr sein.
                            possibleInt = false;
                            if (currenttoken.indexOf("e") != -1) {
                                possibleFloat = false;
                            }
                        } else {
                            if (newchar == '.') {
                                possibleInt = false;

                            } else {
                                // weder Ziffer, noch Punkt noch minus
                                possibleFloat = false;
                                possibleInt = false;
                                if (newchar == '"') {
                                    while (index < in.length()) {
                                        newchar = (char) in.charAt(index++);
                                        if (newchar == '"') {
                                            break;
                                        }
                                        currenttoken.append(newchar);
                                    }
                                } else {
                                    if (newchar == '/' && possibleInt) {
                                        //  ein OBJ x/x/x Tupel. Token abschliessen, ansonsten einfach weiterlesen
                                        if (currenttoken != null) {
                                            //System.out.println("Building token from string:"+currenttoken+
                                            //        " possibleInt="+possibleInt+" possibleFloat="+possibleFloat);
                                            token.add(buildToken(currenttoken, possibleInt, possibleFloat, true));
                                            possibleFloat = possibleInt = true;
                                            currenttoken = null;
                                        }

                                    } else {
                                        possibleInt = false;
                                        // Identifier oder ein OBJ x/x/x Tupel
                                        if (!Character.isLetter(newchar) && newchar != '_' /*&& newchar != '/'* /) {
                                            throw new InvalidAcDataException("invalid char " + newchar + " in line: " + in);
                                        }
                                        /*if (newchar == '/'){
                                            possibleObjTupel = true;
                                        }* /
                                    }
                                }
                            }
                        }
                    }
                    if (newchar != '"') {
                        currenttoken.append(newchar);
                    }
                }
            }
        }
        if (currenttoken != null) {
            token.add(buildToken(currenttoken, possibleInt, possibleFloat, possibleObjTupel));
        }

        return token.toArray(new AcToken[0]);*/
    }

    /**
     * format z.B. "ddsf"
     *
     * @param ins
     * @param format
     * @return
     * @throws InvalidDataException
     */
    public static AcToken[] parseLine(String ins, String format) throws InvalidDataException {
        AcToken[] token = new AcToken[StringUtils.length(format)];
        String[] parts = StringUtils.splitByWhitespace(ins);
        for (int i = 0; i < StringUtils.length(format); i++) {
            char c = StringUtils.charAt(format, i);
            switch (c) {
                case 'd':
                    token[i] = new AcToken(Integer.parseInt(parts[i]));
                    break;
                case 'f':
                    token[i] = new AcToken(Float.parseFloat(parts[i]));
                    break;
                case 's':
                    token[i] = new AcToken(parts[i]);
                    break;
                default:
                    throw new InvalidDataException("invalid format " + c);
            }
        }
        return token;
    }

    private Tokenizer getTokenizer(String firsttoken) throws InvalidDataException {
        if (StringUtils.equalsIgnoreCase(firsttoken, "OBJECT") || StringUtils.equalsIgnoreCase(firsttoken, "name") ||
                StringUtils.equalsIgnoreCase(firsttoken, "o") || StringUtils.equalsIgnoreCase(firsttoken, "g") ||
                StringUtils.equalsIgnoreCase(firsttoken, "usemtl") || StringUtils.equalsIgnoreCase(firsttoken, "mtllib") ||
                StringUtils.equalsIgnoreCase(firsttoken, "s") || StringUtils.equalsIgnoreCase(firsttoken, "OBJECT_BASE") ||
                StringUtils.equalsIgnoreCase(firsttoken, "texture") || StringUtils.equalsIgnoreCase(firsttoken, "AC3Db") ||
                StringUtils.equalsIgnoreCase(firsttoken, "AC3DbS")) {
            return new IdentifierTokenizer(firsttoken);
        }
        if (StringUtils.equalsIgnoreCase(firsttoken, "kids") || StringUtils.equalsIgnoreCase(firsttoken, "numvert") ||
                StringUtils.equalsIgnoreCase(firsttoken, "numsurf") || StringUtils.equalsIgnoreCase(firsttoken, "surf") ||
                StringUtils.equalsIgnoreCase(firsttoken, "mat") || StringUtils.equalsIgnoreCase(firsttoken, "refs") ||
                StringUtils.equalsIgnoreCase(firsttoken, "index") | StringUtils.equalsIgnoreCase(firsttoken, "data") ||
                StringUtils.equalsIgnoreCase(firsttoken, "points")) {
            return new NumericIntTokenizer(firsttoken);
        }
        if (StringUtils.equalsIgnoreCase(firsttoken, "OBJECT_SHARED")) {
            return new ObjectSharedTokenizer(firsttoken);
        }
        if (StringUtils.equalsIgnoreCase(firsttoken, "OBJECT_SIGN")) {
            return new ObjectSignTokenizer(firsttoken);
        }
        if (StringUtils.equalsIgnoreCase(firsttoken, "crease") ||
                StringUtils.equalsIgnoreCase(firsttoken, "width") || StringUtils.equalsIgnoreCase(firsttoken, "lod_near") ||
                StringUtils.equalsIgnoreCase(firsttoken, "lod_far") || StringUtils.equalsIgnoreCase(firsttoken, "blend")|| 
                StringUtils.equalsIgnoreCase(firsttoken, "hard_surf") || StringUtils.equalsIgnoreCase(firsttoken, "deck") ||
                StringUtils.equalsIgnoreCase(firsttoken, "showname")) {
            return new NumericFloatTokenizer(firsttoken);
        }
        if (StringUtils.equalsIgnoreCase(firsttoken, "f")) {
            return new ObjFaceTokenizer(firsttoken);
        }
        if (StringUtils.equalsIgnoreCase(firsttoken, "material")) {
            return new MaterialTokenizer(firsttoken);
        }
        if (StringUtils.equalsIgnoreCase(firsttoken, "texrep")) {
            return new NumericFloat3Tokenizer(firsttoken);
        }
        if (StringUtils.equalsIgnoreCase(firsttoken, "texoff")) {
            return new NumericFloat2Tokenizer(firsttoken);
        }
        if (StringUtils.equalsIgnoreCase(firsttoken, "loc") || StringUtils.equalsIgnoreCase(firsttoken, "v") ||
                StringUtils.equalsIgnoreCase(firsttoken, "vn") || StringUtils.equalsIgnoreCase(firsttoken, "rot")) {
            return new NumericFloat3Tokenizer(firsttoken);
        }
        // Only reached for nondigit lines. So the keyword must be known. There cannot be a default Tokenizer
        // 3.1.18: there are invalid ACs out htere, eg. Objects/e000n50/e008n50/EDGS_AirAllianceHangar.ac with contains a line "lightcone". So return null and ignore line.
        //throw new InvalidDataException("invalid keyword " + firsttoken);
        return null;
        //return new NumericFloat3Tokenizer(firsttoken);
    }

    public void addVector3N(List<Vector3> target, AcToken[] token) throws InvalidDataException {
        AcToken[] sublist = new AcToken[3];
        sublist[0] = token[1];
        sublist[1] = token[2];
        sublist[2] = token[3];
        if (isTupel3(sublist)) {
            target.add(buildVector3(sublist));
        } else {
            throw new InvalidDataException("unknown getFirst token " + token[0] + ". number of tokens:" + token.length);
        }
    }

    public void addVector3(List<Vector3> target, AcToken[] token) throws InvalidDataException {
        AcToken[] sublist = new AcToken[3];
        sublist[0] = token[1];
        sublist[1] = token[2];
        sublist[2] = token[3];
        if (isTupel3(sublist)) {
            target.add(buildVector3(sublist));
        } else {
            throw new InvalidDataException("unknown getFirst token " + token[0] + ". number of tokens:" + token.length);
        }
    }
    
    private AcToken buildToken(StringBuffer currenttoken, boolean possibleInt, boolean possibleFloat, boolean objTupel) {
       /*if (possibleObjTupel){

       }else {
*/
        AcToken token;
        String s = currenttoken.toString();
        if (possibleInt) {
            if (StringUtils.startsWith(currenttoken.toString(), "0x"))
                token = new AcToken(Integer.parseInt(StringUtils.substring(s, 2), 16));
            else
                token = new AcToken(Integer.parseInt(s));
        } else {
            if (possibleFloat) {
                token = new AcToken(Float.parseFloat(s));
            } else {
                token = new AcToken(currenttoken.toString());
            }
        }
        token.objTupel = objTupel;
        return token;
    }

    public static AcToken parseInt(String s) {
        AcToken token;
        if (StringUtils.startsWith(s.toString(), "0x") || StringUtils.startsWith(s.toString(), "0X"))
            token = new AcToken(Integer.parseInt(StringUtils.substring(s, 2), 16));
        else
            token = new AcToken(Integer.parseInt(s));
        return token;
    }

    public boolean isTupel3(AcToken[] token) {
        if (token.length != 3)
            return false;
        for (int i = 0; i < 3; i++) {
            if (token[i].stringvalue != null)
                return false;
        }
        return true;
    }

    public boolean isTupel2(AcToken[] token) {
        if (token.length != 2)
            return false;
        for (int i = 0; i < 2; i++) {
            if (token[i].stringvalue != null)
                return false;
        }
        return true;
    }

    public /*7.2.18 Native*/Vector3 buildVector3(AcToken[] token) throws InvalidDataException {
        return new Vector3(token[0].getValueAsFloat(), token[1].getValueAsFloat(), token[2].getValueAsFloat());
    }

    public Vector2 buildVector2(AcToken[] token) throws InvalidDataException {
        return new Vector2(token[0].getValueAsFloat(), token[1].getValueAsFloat());
    }

    /**
     * 13.9.16: Ist das nicht kokellores? Besser splitByWhitespace(input);? TODO
     *
     * @return
     */
    /*@Deprecated
    public static List<String> splitByBlank(String input) {
        while (StringUtils.lastIndexOf(input, "  ") != -1) {
            input = StringUtils.replaceAll(input, "  ", " ");
        }
        return StringUtils.asList(StringUtils.split(input, " "));

    }*/
    @Override
    protected int getCurrentLine() {
        return lineno;
    }
}

abstract class Tokenizer {
    String firsttoken;

    public Tokenizer(String firsttoken) {
        this.firsttoken = firsttoken;
    }

    public abstract AcToken[] tokenize(String input) throws InvalidDataException;

    public AcToken[] buildTlist(AcToken[] acTokens) {
        if (firsttoken == null) {
            return acTokens;
        }
        AcToken[] t = new AcToken[acTokens.length + 1];
        t[0] = new AcToken(firsttoken);
        //System.arraycopy(acTokens, 0, t, 1, acTokens.length);
        for (int i = 0; i < acTokens.length; i++) {
            t[i + 1] = acTokens[i];
        }
        return t;
    }

    /**
     * Removes possible '"' and removes ident from passed StringBuffer.
     *
     * @param sb
     * @return
     */
    public String readIdent(StringBuffer sb) {
        String ident = "";
        boolean delimited = false;
        if (sb.length() == 0) {
            return "";
        }

        String s = sb.toString();
        if (StringUtils.charAt(s, 0) == '"') {
            delimited = true;
            ident = StringUtils.substringBefore(StringUtils.substring(s, 1), "\"");
            sb.delete(0, StringUtils.length(ident) + 3);
        } else {
            ident = StringUtils.substringBefore(s, " ");
            sb.delete(0, StringUtils.length(ident) + 1);
        }
        return ident;
    }
}

/**
 * Geht auch mit zwei floats (texrep)
 */
class NumericFloat3Tokenizer extends Tokenizer {

    public NumericFloat3Tokenizer(String firsttoken) {
        super(firsttoken);
    }

    @Override
    public AcToken[] tokenize(String input) {
        String[] parts = StringUtils.splitByWhitespace(input);
        if (parts.length == 3) {
            return buildTlist(new AcToken[]{new AcToken(Float.parseFloat(parts[0])), new AcToken(Float.parseFloat(parts[1])), new AcToken(Float.parseFloat(parts[2]))});
        } else {
            //textrep 
            return buildTlist(new AcToken[]{new AcToken(Float.parseFloat(parts[0])), new AcToken(Float.parseFloat(parts[1]))});
        }

    }
}

class NumericFloat2Tokenizer extends Tokenizer {

    public NumericFloat2Tokenizer(String firsttoken) {
        super(firsttoken);
    }

    @Override
    public AcToken[] tokenize(String input) {
        String[] parts = StringUtils.splitByWhitespace(input);
        return buildTlist(new AcToken[]{new AcToken(Float.parseFloat(parts[0])), new AcToken(Float.parseFloat(parts[1]))});
    }
}

class NumericIntTokenizer extends Tokenizer {
    public NumericIntTokenizer(String firsttoken) {
        super(firsttoken);
    }

    @Override
    public AcToken[] tokenize(String input) {
        String[] parts = StringUtils.splitByWhitespace(input);
        return buildTlist(new AcToken[]{AsciiLoader.parseInt(parts[0])});
    }
}

class IdentifierTokenizer extends Tokenizer {
    public IdentifierTokenizer(String firsttoken) {
        super(firsttoken);
    }

    @Override
    public AcToken[] tokenize(String input) {
        String ident = readIdent(new StringBuffer(input));
        return buildTlist(new AcToken[]{new AcToken(ident)});
    }
}

class ObjFaceTokenizer extends Tokenizer {

    public ObjFaceTokenizer(String firsttoken) {
        super(firsttoken);
    }

    @Override
    public AcToken[] tokenize(String input) throws InvalidDataException {
        // List<AcToken> list = new ArrayList<AcToken>();
        AcToken token = new AcToken(null);
        String[] parts = StringUtils.splitByWhitespace(input);
        int index = 0;
        token.vi = new int[3];
        if (parts.length != 3) {
            //TODO OBJ kann aber auch beliebige Faces!
            throw new InvalidDataException("not a face3: " + input);
        }
        for (String s : parts) {
            String[] s2 = StringUtils.split(s, "/");
            token.vi[index] = Integer.parseInt(s2[0]);

            switch (s2.length) {
                case 1:
                    break;
                case 3:
                    if (token.ni == null) {
                        token.ni = new int[3];
                    }
                    token.ni[index] = Integer.parseInt(s2[2]);
                    break;

            }
            index++;
        }
        return buildTlist(new AcToken[]{token});
    }
}

class MaterialTokenizer extends Tokenizer {
    public MaterialTokenizer(String firsttoken) {
        super(firsttoken);
    }

    @Override
    public AcToken[] tokenize(String input) throws InvalidDataException {
        StringBuffer sb = new StringBuffer(input);
        String ident = readIdent(sb);
        List<AcToken> l = new ArrayList<AcToken>();
        input = sb.toString();
        PortableMaterial m = new PortableMaterial();
        // 31.3.17: Wrap als Defualt fuer ac setzen. texrep soll default "1 1" sein.
        m.wraps = true;
        m.wrapt = true;
        // 22.1.18: Was auch immer texrep genau ist, zu einem wrap/repeat soll das doch bestimmt nicht führen. Z.B. in den ganzen Panel. Darum auf false;
        // NeeNee, der osg AC loader setzt repeat als default. Und das wird wohl nie geaendert. Irgendwie schon komisch.
        //m.wraps = false;
        //m.wrapt = false;
        String[] pa = StringUtils.splitByWhitespace(input);//AsciiLoader.splitByBlank(input);
        List<String> parts = new ArrayList<String>();
        for (String s : pa) {
            parts.add(s);
        }
        if (parts.size() != 20) {
            throw new InvalidDataException("MATERIAL line has " + parts.size() + " token, expected 20");
        }
        m.name = ident;
        //String type = StringUtils.substringBefore(input," ");
        while (parts.size() > 1) {
            String p = parts.get(0);
            if (p.equals("rgb")) {
                //Ist das wirklich die Grundfarbe
                //19.1.18: alpha 1 statt 0.
                m.color = new Color(Float.parseFloat(parts.get(1)), Float.parseFloat(parts.get(2)), Float.parseFloat(parts.get(3)), 1.0f);
                parts = parts.subList(4, parts.size());
            } else {
                if (p.equals("amb")) {
                    m.ambient = new Color(Float.parseFloat(parts.get(1)), Float.parseFloat(parts.get(2)), Float.parseFloat(parts.get(3)), 1);
                    parts = parts.subList(4, parts.size());
                } else {
                    if (p.equals("emis")) {
                        m.emis = new /*19.1.18Ac*/Color(Float.parseFloat(parts.get(1)), Float.parseFloat(parts.get(2)), Float.parseFloat(parts.get(3)),1);
                        parts = parts.subList(4, parts.size());
                    } else {
                        if (p.equals("spec")) {
                            m.specular = new Color(Float.parseFloat(parts.get(1)), Float.parseFloat(parts.get(2)), Float.parseFloat(parts.get(3)), 1);
                            parts = parts.subList(4, parts.size());
                        } else {
                            if (p.equals("shi")) {
                                // Das ist ein int, wahrscheinlich eine Prozentangabe
                                m.shininess = new FloatHolder(0);
                                m.shininess.value = (float) Integer.parseInt(parts.get(1)) / 100f;
                                parts = parts.subList(2, parts.size());
                            } else {
                                if (p.equals("trans")) {
                                    m.transparencypercent = new FloatHolder(0);
                                    m.transparencypercent.value = Float.parseFloat(parts.get(1));
                                    parts = parts.subList(2, parts.size());
                                } else {
                                    throw new InvalidDataException("unexpected toekn " + parts.get(0));
                                }
                            }
                        }
                    }
                }
            }
        }
        AcToken token = new AcToken(input);
        token.material = m;
        return buildTlist(new AcToken[]{token});
    }


}

class NumericIntFloat2Tokenizer extends Tokenizer {

    public NumericIntFloat2Tokenizer(String firsttoken) {
        super(firsttoken);
    }

    @Override
    public AcToken[] tokenize(String input) {
        String[] parts = StringUtils.splitByWhitespace(input);
        return buildTlist(new AcToken[]{AsciiLoader.parseInt(parts[0]),
                new AcToken(Float.parseFloat(parts[1])), new AcToken(Float.parseFloat(parts[2]))});
    }
}

class NumericFloatTokenizer extends Tokenizer {
    public NumericFloatTokenizer(String firsttoken) {
        super(firsttoken);
    }

    @Override
    public AcToken[] tokenize(String input) {
        return buildTlist(new AcToken[]{new AcToken(Float.parseFloat(input))});
    }
}

class ObjectSharedTokenizer extends Tokenizer {

    public ObjectSharedTokenizer(String firsttoken) {
        super(firsttoken);
    }

    @Override
    public AcToken[] tokenize(String input) {
        String[] parts = StringUtils.splitByWhitespace(input);
        return buildTlist(new AcToken[]{new AcToken(parts[0]),
                new AcToken(Float.parseFloat(parts[1])), new AcToken(Float.parseFloat(parts[2])), new AcToken(Float.parseFloat(parts[3])), new AcToken(Float.parseFloat(parts[4]))});
    }
}

class ObjectSignTokenizer extends Tokenizer {

    public ObjectSignTokenizer(String firsttoken) {
        super(firsttoken);
    }

    @Override
    public AcToken[] tokenize(String input) {
        String[] parts = StringUtils.splitByWhitespace(input);
        return buildTlist(new AcToken[]{new AcToken(parts[0]),
                new AcToken(Float.parseFloat(parts[1])), new AcToken(Float.parseFloat(parts[2])), new AcToken(Float.parseFloat(parts[3])), new AcToken(Float.parseFloat(parts[4])), new AcToken(Float.parseFloat(parts[5]))});
    }
}

class ObjectDataTokenizer extends Tokenizer {
    String firsttoken;

    public ObjectDataTokenizer(int size) {
        super("objectdata");
        this.firsttoken = firsttoken;
    }

    @Override
    public AcToken[] tokenize(String input) {
        String[] parts = new String[]{input};
        return buildTlist(new AcToken[]{new AcToken(parts[0])});
    }
}
