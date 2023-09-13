package de.yard.threed.core.loader;

/**
 * 26.10.18: Wofuer ist das?
 * Created by thomass on 22.02.16.
 */
/*17.6.21 public class LoaderSTG extends AsciiLoader {
    Log logger = Platform.getInstance().getLog(LoaderSTG.class);
    private StringReader ins;
    public List<String> objectbase = new ArrayList<String>();

    public LoaderSTG(StringReader ins) throws InvalidDataException {
        this.ins = ins;
        load();
    }

    @Override
    protected void doload() throws InvalidDataException {

        AcToken[] token;
        AcObject currentobject = null;
        AcSurface currentsurface = null;
        boolean invertices = false;
        boolean inrefs = false;
        int surfaceindex = 0;
        AcObject parent = null;

        while ((token = parseLine(ins.readLine(), false, inrefs?1:0,null)) != null) {
            //dumpen
            /*System.out.print("line " + lines + "(" + token.length + " token):");
            for (AcToken tk : token) {
                System.out.print(tk + " ");
            }
            System.out.println();* /

            if (token.length > 0) {
                if (token[0].isIdent("OBJECT_BASE")) {
                    objectbase.add(token[1].stringvalue);
                } else {
                    if (token[0].isIdent("OBJECT_SHARED")) {
                    } else {
                        if (token[0].isIdent("OBJECT_SIGN")) {
                        } else {
                            if (token[0].isIdent("OBJECT")) {
                            } else {
                                throw new InvalidDataException("unknown getFirst token " + token[0] + ". number of tokens:" + token.length);
                            }
                        }
                    }
                }
            }
            lineno++;
        }
    }

    @Override
    protected Log getLog() {
        return logger;
    }



}*/
