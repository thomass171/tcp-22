package de.yard.threed.engine.loader;

/**
 * Created by thomass on 08.03.16.
 */

import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.Shape;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.platform.common.StringReader;

import java.util.HashMap;

public class LoaderAnimShape extends AsciiLoader {
    Log logger = Platform.getInstance().getLog(LoaderAnimShape.class);
    // der key bei width ist 10mal
    public HashMap<Integer, HashMap<Integer, Shape>> shapelists = new HashMap<Integer, HashMap<Integer, Shape>>();
    private StringReader ins;

    public LoaderAnimShape(StringReader ins) throws InvalidDataException {
        this.ins = ins;
        load();
    }

    @Override
    protected void doload() throws InvalidDataException {
        AcToken[] token;
        HashMap<Integer, Shape> currentshapelist = new HashMap<Integer, Shape>();
        Shape currentshape = null;
       
        while ((token = parseLine(ins.readLine(), false,2,null)) != null) {
            //dumpen
           /* System.out.print("line " + lines + "(" + token.length + " token):");
            for (AcToken tk : token) {
                System.out.print(tk + " ");
            }
            System.out.println();*/

            if (token.length > 0) {

                if (token[0].isIdent("points")) {
                    int points = token[1].intvalue;
                    currentshapelist = new HashMap<Integer, Shape>();
                    shapelists.put(points,currentshapelist);
                } else {
                    if (token[0].isIdent("width")) {
                        int width = (int)Math.round(token[1].getValueAsFloat() * 100);
                        currentshapelist = new HashMap<Integer, Shape>();
                        shapelists.put(width,currentshapelist);
                    } else {
                        if (token[0].isIdent("index")) {
                            int index = token[1].intvalue;
                            currentshape = new Shape();
                            currentshapelist.put(index,currentshape);
                        } else {
                            if (isTupel2(token)) {
                                currentshape.addPoint(buildVector2(token));
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

}


