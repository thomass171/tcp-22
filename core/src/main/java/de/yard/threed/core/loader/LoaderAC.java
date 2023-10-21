package de.yard.threed.core.loader;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thschonh on 14.07.2015.
 * 18.10.23: No longer used inside platform but only in tools for converting. TODO move to tools.
 */
public class LoaderAC extends AsciiLoader {
    static public int flagSurfaceTypePolygon = 0,
            flagSurfaceTypeLineLoop = 1,
            flagSurfaceTypeLineStrip = 2,
            flagSurfaceShaded = 16,//1<<4,
            flagSurfaceTwoSided = 32;//1<<5 0x20
    // 27.12.17: Blender ignoriert beim import "world". Das sehe ich auch mal vor.
    boolean ignoreacworld = false;

    List<String> materialnamebyindex = new ArrayList<String>();

    Log logger = Platform.getInstance().getLog(LoaderAC.class);
    // a simple twosided unshaded rectangle (white):
    public static final String sampleac = "AC3Db\n" +
            "MATERIAL \"\" rgb 1 1 1  amb 0.2 0.2 0.2  emis 0 0 0  spec 0.5 0.5 0.5  shi 10  trans 0\n" +
            "OBJECT world\n" +
            "kids 1\n" +
            "OBJECT poly\n" +
            "lod_far=1500.000000\n" +
            "lod_far=1500.000000\n" +
            "blend=-1.000000\n" +
            "hard_surf=\n" +
            "deck=0\n" +
            "showname 1\n" +
            "name \"rect\"\n" +
            "loc 1 0.5 0\n" +
            "rot 0 0 0\n" +
            "numvert 4\n" +
            "-1 0.5 0\n" +
            "1 0.5 0\n" +
            "1 -0.5 0\n" +
            "-1 -0.5 0\n" +
            "numsurf 1\n" +
            "SURF 0x20\n" +
            "mat 0\n" +
            "refs 4\n" +
            "3 0 0\n" +
            "2 1 0\n" +
            "1 1 1\n" +
            "0 0 1\n" +
            "kids 0\n";

    // Skizze 9. 21.12.16: Textures rausgenommen, weils dauer keinen ResourcePAth gibt.
    public static final String lndspots = "AC3Db\n" +
            "MATERIAL \"emit\" rgb 1 1 1  amb 1 1 1  emis 1 1 1  spec 0 0 0  shi 32  trans 0.6\n" +
            "OBJECT world\n" +
            "kids 2\n" +
            "OBJECT poly\n" +
            "name \"LHLND.spot\"\n" +
            //"texture \"light_spot.png\"\n" +
            "crease 80.000000\n" +
            "numvert 4\n" +
            "-64.8986 -4.70375 46.2053\n" +
            "-0.1408 -4.70375 16.2053\n" +
            "-0.1408 -4.70375 0.2053\n" +
            "-64.8986 -4.70375 10.2053\n" +
            "numsurf 1\n" +
            "SURF 0x20\n" +
            "mat 0\n" +
            "refs 4\n" +
            "3 0 1\n" +
            "2 1 1\n" +
            "1 1 0\n" +
            "0 0 0\n" +
            "kids 0\n" +
            "OBJECT poly\n" +
            "name \"RHLND.spot\"\n" +
            //"texture \"light_spot.png\"\n" +
            "crease 80.000000\n" +
            "numvert 4\n" +
            "-64.8986 -4.70375 -46.2053\n" +
            "-0.1408 -4.70375 -16.2053\n" +
            "-0.1408 -4.70375 0.2053\n" +
            "-64.8986 -4.70375 -10.2053\n" +
            "numsurf 1\n" +
            "SURF 0x20\n" +
            "mat 0\n" +
            "refs 4\n" +
            "3 0 1\n" +
            "2 1 1\n" +
            "1 1 0\n" +
            "0 0 0\n" +
            "kids 0";

    // ein twosided viereck, dass sich in y Richtung erstreckt.
    public static final String sampleac2 = "AC3Db\n" +
            "MATERIAL \"\" rgb 1 1 1  amb 0.2 0.2 0.2  emis 0 0 0  spec 0.5 0.5 0.5  shi 10  trans 0\n" +
            "OBJECT world\n" +
            "kids 1\n" +
            "OBJECT poly\n" +
            "name \"rect\"\n" +
            "loc 1 0.5 0\n" +
            "numvert 4\n" +
            "-1 12 3\n" +
            "1 12 3\n" +
            "1 -12 3\n" +
            "-1 -12 3\n" +
            "numsurf 1\n" +
            "SURF 0x20\n" +
            "mat 0\n" +
            "refs 4\n" +
            "3 0 0\n" +
            "2 1 0\n" +
            "1 1 1\n" +
            "0 0 1\n" +
            "kids 0\n";

    // Der tower erfordert vertexduplizierung wegen UV. Dies Beispiel enthaelt unnoetige Vertices.
    public static final String egkk_tower = "AC3Db\n" +
            "MATERIAL \"DefaultWhite\" rgb 1 1 1  amb 1 1 1  emis 0 0 0  spec 0.5 0.5 0.5  shi 64  trans 0\n" +
            "MATERIAL \"hazard2Xmat\" rgb 1 1 1 amb 0 0 0 emis 1 1 1 spec 0 0 0 shi 0 trans 0\n" +
            "MATERIAL \"hazardXmat\" rgb 1 1 1 amb 0 0 0 emis 1 1 1 spec 0 0 0 shi 0 trans 0\n" +
            "MATERIAL \"Material\" rgb 0.982307 0.982307 0.982307 amb 0.5 0.5 0.5 emis 0 0 0 spec 1 1 1 shi 11 trans 0\n" +
            "OBJECT world\n" +
            "kids 1\n" +
            "OBJECT poly\n" +
            "name \"tower\"\n" +
            "data 8\n" +
            "Cube.002\n" +
            "texture \"egkk_tower.png\"\n" +
            "numvert 16\n" +
            "6.541071 0.012573 -21.77634\n" +
            "6.541071 0.012573 -15.257427\n" +
            "-6.523499 0.012573 -15.257427\n" +
            "-6.523497 0.012573 -21.77634\n" +
            "6.541074 3.257071 -21.776339\n" +
            "6.541066 4.320834 -15.257423\n" +
            "-6.523499 4.320834 -15.257427\n" +
            "-6.523499 3.257071 -21.77634\n" +
            "24.199236 0.010515 -10.192159\n" +
            "24.199236 0.010515 9.679972\n" +
            "9.988953 0.010515 9.679968\n" +
            "9.988955 0.010515 -10.192163\n" +
            "24.19924 8.575923 -10.192154\n" +
            "24.199232 8.575923 9.679977\n" +
            "9.988951 8.575923 9.679964\n" +
            "9.988954 8.575923 -10.192161\n" +
            "numsurf 4\n" +
            "SURF 0x00\n" +
            "mat 3\n" +
            "refs 4\n" +
            "12 0.286635369062 0.194236695766\n" +
            "15 0.286765187979 0.407084643841\n" +
            "14 0.137938424945 0.407447636127\n" +
            "13 0.137808486819 0.194599747658\n" +
            "SURF 0x00\n" +
            "mat 3\n" +
            "refs 4\n" +
            "8 0.286077708006 0.411397904158\n" +
            "12 0.286077588797 0.539694547653\n" +
            "13 0.137250706553 0.539694547653\n" +
            "9 0.137250766158 0.411397904158\n" +
            "SURF 0x00\n" +
            "mat 3\n" +
            "refs 4\n" +
            "9 0.243484541774 0.547395586967\n" +
            "13 0.243484482169 0.67569231987\n" +
            "14 0.137060388923 0.67569231987\n" +
            "10 0.137060388923 0.547395586967\n" +
            "SURF 0x00\n" +
            "mat 3\n" +
            "refs 4\n" +
            "10 0.137250766158 0.411397904158\n" +
            "14 0.137250855565 0.539694547653\n" +
            "15 0.286077708006 0.539694547653\n" +
            "11 0.286077708006 0.411397904158\n" +
            "kids 0";

    private StringReader ins;

    public LoaderAC(StringReader ins, boolean ignoreacworld) throws InvalidDataException {
        this.ins = ins;
        this.ignoreacworld = ignoreacworld;
        load();
        //loadedfile.preProcess();
    }

    public LoaderAC(StringReader ins, BundleResource filename) throws InvalidDataException {
        this.ins = ins;
        load();
        //loadedfile.preProcess();
        fixWorldName(filename);
    }

    /**
     * 11.4.17: pp laden. Hier, weil btg immer noch den Loader erwartet.
     */
    /*3.5.19public LoaderAC(NativeResource file, ByteArrayInputStream buf, ResourcePath texturepath) throws InvalidDataException {
        ploadedfile = new PortableModelList(/*6.12.17 file,* / buf, texturepath);
        //23.9.17: Wenn world keinen eigenen Namen hat, den filename setzen. 
        if (ploadedfile.objects.size() > 0) {
            if (StringUtils.empty(ploadedfile.objects.get(0).name) && file instanceof BundleResource) {
                ploadedfile.objects.get(0).name = ((BundleResource) file).getBasename();
            }
        }
    }*/

    /**
     * 22.12.17: Wenn world keinen eigenen Namen hat, den filename setzen.
     */
    private void fixWorldName(BundleResource file) {
        if (loadedfile.objects.size() > 0) {
            if (StringUtils.empty(loadedfile.objects.get(0).name) && file instanceof BundleResource) {
                loadedfile.objects.get(0).name = ((BundleResource) file).getBasename();
            }
        }
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

        while ((token = parseLine(ins.readLine(), false, inrefs ? 1 : 0, null)) != null) {
            //dumpen
           /* System.out.print("line " + lines + "(" + token.length + " token):");
            for (AcToken tk : token) {
                System.out.print(tk + " ");
            }
            System.out.println();*/

            if (token.length > 0) {
                if (token[0].isAc3D()) {
                    // 13.9.16 AC3DbS gibts auch(?)
                    if (!token[0].stringvalue.equals("AC3Db") && !token[0].stringvalue.equals("AC3DbS")) {
                        throw new InvalidDataException("unexpected file format " + token[0].stringvalue);
                    }
                } else {
                    if (token[0].isMaterial()) {
                        // Material in einer shaded und unshaded variante anlegen
                        PortableMaterial lmat = buildMaterial(token);
                        PortableMaterial lmat1 = lmat.duplicate("shaded" + lmat.name);
                        loadedfile.materials.add(lmat1);
                        PortableMaterial lmat2 = lmat.duplicate("unshaded" + lmat.name);
                        lmat2.shaded = false;
                        loadedfile.materials.add(lmat2);
                        materialnamebyindex.add(lmat.name);
                    } else {
                        if (token[0].isObject()) {
                            AcObject newobj = new AcObject(token[1], loadedfile.materials.size());
                            // newobj ist "world"
                            loadObject(ins, newobj);
                            if (ignoreacworld) {
                                loadedfile.objects = newobj.kids;
                            } else {
                                // Nur "world" kommt in die Liste
                                newobj.isworld = true;
                                loadedfile.objects.add(newobj);
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

    private AcObject loadObject(StringReader ins, AcObject newobj) throws InvalidDataException {
        AcToken[] token;
        AcSurface currentsurface = null;
        //PrimitiveBin/*AcSurface*/ currentsurface = null;
        boolean invertices = false;
        boolean inrefs = false;
        int surfaceindex = 0;
        AcObject parent = null;
        AcObject currentobject = newobj;
        Tokenizer nexttokenizer = null;

        while ((token = parseLine(ins.readLine(), false, inrefs ? 1 : 0, nexttokenizer)) != null) {
            nexttokenizer = null;
            //dumpen
           /* System.out.print("line " + lines + "(" + token.length + " token):");
            for (AcToken tk : token) {
                System.out.print(tk + " ");
            }
            System.out.println();*/

            if (token.length > 0) {
                if (token[0].isObject()) {
                    currentobject = new AcObject(token[1], loadedfile.materials.size());
                    // loadObject(in, newobj);
                } else {

                    if (token[0].isName()) {
                        currentobject.name = token[1].stringvalue;
                    } else {
                        if (token[0].isLoc()) {
                            currentobject.location = new Vector3(token[1].getValueAsFloat(), token[2].getValueAsFloat(), token[3].getValueAsFloat());
                        } else {
                            if (token[0].isTexture()) {
                                //AC hat die Textur am Object, nicht am Material.
                                currentobject.texture = token[1].stringvalue;
                            } else {
                                if (token[0].isCrease()) {
                                    currentobject.setCrease(new Degree(token[1].getValueAsFloat()));
                                } else {
                                    if (token[0].isIdent("blend")) {
                                        //2.1.18:TODO
                                    } else {
                                        if (token[0].isIdent("lod_far")) {
                                            //2.1.18:TODO
                                        } else {
                                            if (token[0].isIdent("lod_near")) {
                                                //2.1.18:TODO
                                            } else {
                                                if (token[0].isIdent("hard_surf")) {
                                                    //2.1.18:TODO
                                                } else {
                                                    if (token[0].isIdent("deck")) {
                                                        //2.1.18:TODO
                                                    } else {
                                                        if (token[0].isIdent("showname")) {
                                                            //2.1.18:TODO
                                                        } else {
                                                            if (token[0].isIdent("rot")) {
                                                                //2.1.18:TODO
                                                            } else {
                                                                if (token[0].isNumvert()) {
                                                                    //currentobject.vertices = new AcVector3[getIntValue(token[1])];
                                                                    invertices = true;
                                                                } else {
                                                                    if (token[0].isNumsurf()) {
                                                                        currentobject.surface = new AcSurface[getIntValue(token[1])];
                                                                        surfaceindex = 0;
                                                                    } else {
                                                                        if (token[0].isIdent("SURF")) {
                                                                            currentobject.surface[surfaceindex] = new AcSurface(getIntValue(token[1]));
                                                                            currentsurface = currentobject.surface[surfaceindex++];
                                                                            //currentsurface = Bins.getOrCreatePrimitiveBin(getIntValue(token[1]), currentobject.getVertices(), currentobject.crease, currentobject);
                                                                            inrefs = false;
                                                                        } else {
                                                                            if (token[0].isMat()) {
                                                                                currentsurface.mat = getIntValue(token[1]);
                                                                            } else {
                                                                                if (token[0].isRefs()) {
                                                                                    currentsurface.init(getIntValue(token[1]));
                                                                                    //currentsurface.beginPrimitive(getIntValue(token[1]));
                                                                                    inrefs = true;
                                                                                    invertices = false;
                                                                                } else {
                                                                                    if (token[0].isIdent("texrep")) {
                                                                                        //Hier muss nicht unbedingt was gemacht werden, weil wrap als Default fuer ac gesetzt wird
                                                                                        float repeatx = token[1].getValueAsFloat();
                                                                                        float repeaty = token[2].getValueAsFloat();
                                                                                        // Wenn nicht wirklich "1 1" drinsteht, ist das unguenstig. Wie auch immer man das löst.Mal sehn, wie häufig das ist.
                                                                                        if (repeatx != 1 || repeaty != 1) {
                                                                                            logger.warn("unhandled texrep values " + repeatx + " " + repeaty);
                                                                                        }
                                                                                    } else {
                                                                                        if (token[0].isIdent("texoff")) {
                                                                                            // TODO gibts wohl, steht aber in keiner Doku. OSG Loader kennt es.
                                                                                            logger.warn("ignored texoff");
                                                                                        } else {
                                                                                            if (token[0].isIdent("data")) {
                                                                                                // objectdatasize
                                                                                                nexttokenizer = new ObjectDataTokenizer(getIntValue(token[1]));
                                                                                            } else {
                                                                                                if (token[0].isIdent("objectdata")) {
                                                                                                    // ignore for now                                                                
                                                                                                } else {
                                                                                                    if (token[0].isKids()) {
                                                                                                        currentobject.pendingkits = token[1].intvalue;
                                                                                                        inrefs = false;
                                                                    /*if (mainobject == null) {b
                                                                        mainobject = currentobject;
                                                                    } else {
                                                                        mainobject.addObject(currentobject);
                                                                    }
                                                                    currentobject = null;*/
                                                                                                        // 31.10.17: Es werden wohl schon mal (z.B. AI 738) mehr Kids angegeben als wirklich da sind.
                                                                                                        // Darum nur warning.
                                                                                                        for (int i = 0; i < currentobject.pendingkits; i++) {
                                                                                                            AcObject lo = loadObject(ins, null);
                                                                                                            if (lo == null) {
                                                                                                                logger.warn("loadObject returned null for kid " + i);
                                                                                                            } else {
                                                                                                                currentobject.kids.add(lo);
                                                                                                            }
                                                                                                        }
                                                                                                        //currentobject.buildNormals();
                                                                                                        return cleanup(currentobject);
                                                                                                    } else {
                                                                                                        if (isTupel3(token)) {
                                                                                                            if (invertices) {
                                                                                                                currentobject.vertices.add(/*[currentobject.vcount++] = */buildVector3(token));
                                                                                                            } else {
                                                                                                                if (inrefs) {
                                                                                                                    if (currentsurface.addRef(token)) {
                                                                                            /*if (currentobject.name.equals("cockpit_ORIGINAL")) {
                                                                                                inrefs = inrefs;
                                                                                            }*/
                                                                                                                        // Surface komplett. Wahrscheinlich immer FaceN oder Face3.
                                                                                                                        // Kommt an die Face Liste zu dem Material
                                                                                                                        // Fuer das facelistmaterial kann nicht mat als Index verwendet werden, denn das muss ja nicht fortlaufend sein (in diesem Objekt)
                                                                                                                        // Furthermore surfaces might have different SURF attributes (shaded/unshaded)
                                                                                                                        String matname = buildMaterialName(currentsurface, currentsurface.mat);
                                                                                        /*
                                                                                        while (currentobject.faces.size() <= currentsurface.mat) {
                                                                                            currentobject.addFacelist();
                                                                                        }
                                                                                        // Das ist aber schwer aufzudroeseln. Darum zunächst tatsaechlich mat als Index verwenden und leere Facelisten in Kauf nehmen.
                                                                                        while (currentobject.facelistmaterial.size() < currentobject.faces.size()) {
                                                                                            currentobject.facelistmaterial.add(null);
                                                                                        }
                                                                                        if (currentobject.facelistmaterial.get(currentsurface.mat) == null) {
                                                                                            currentobject.facelistmaterial.set(currentsurface.mat, loadedfile.materials.get(currentsurface.mat).name);
                                                                                        }
                                                                                        currentsurface.buildFace(currentobject.faces.get(currentsurface.mat));*/
                                                                                                                        if (currentobject.getFaceListByMaterialName(matname) == null) {
                                                                                                                            currentobject.facelistmaterial.add(matname);
                                                                                                                            // dann darf es die FaceListe auch noch nicht geben
                                                                                                                            currentobject.addFacelist();
                                                                                                                        }
                                                                                                                        currentsurface.buildFace(currentobject, currentobject.getFaceListByMaterialName(matname));
                                                                                                                        //currentsurface.endPrimitive();

                                                                                                                    }
                                                                                                                } else {
                                                                                                                    throw new InvalidDataException("no destination for value tupel");
                                                                                                                }
                                                                                                            }
                                                                                                        } else {
                                                                                                            throw new InvalidDataException("unknown getFirst token " + token[0] + ". number of tokens:" + token.length);
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            lineno++;
        }
        // noch einmal?
        //lineno++;

        //currentobject.surfacebin.finalize();
        //currentobject.buildNormals();
        
        /* muss man das mit den kids pruefen? 27.7.16: Ich denke nicht. if (currentobject != null)
            throw new InvalidAcDataException("OBJECT without kids tag");*/
        // System.out.println("lines found: " + lines);
        // dumpAC(this, System.out);
        if (currentobject == null)

        {
            // Das kann schon mal vorkommen, wenn es ueberraschend doch keine kids mehr gibt.
            logger.warn("currentobject isType null");
        }
        return

                cleanup(currentobject);

    }

    private String buildMaterialName(AcSurface surface, int matindex) {
        String s;
        if (surface.isShaded()) {
            s = "shaded";
        } else {
            s = "unshaded";
        }

        return s + materialnamebyindex.get(matindex);
    }

    private AcObject cleanup(AcObject obj) {
        // Die in Kauf genommenen leeren Facelisten rauswerfen
       /* if (obj.faces != null) {
            for (int i = 0; i < obj.getFaceLists().size(); i++) {
                FaceList surface = obj.getFaceLists().get(i);
                if (surface == null || surface.faces.size() == 0) {
                    // dann darf es auch kein Material dazu geben.
                    obj.getFaceLists().remove(i);
                    obj.facelistmaterial.remove(i);
                    i--;
                }
            }
        }*/
        return obj;
    }


    private PortableMaterial buildMaterial(AcToken[] token) throws InvalidDataException {
      /*  if (token.length != 22) {
            throw new InvalidAcDataException("MATERIAL line has " + token.length + " token, expected 22");
        }
        LoadedMaterial m = new LoadedMaterial();
        m.name = token[1].stringvalue;
        m.rgb = new AcColor(token[3].getValueAsFloat(), token[4].getValueAsFloat(), token[5].getValueAsFloat());
        m.amb = new AcVector3(token[7].getValueAsFloat(), token[8].getValueAsFloat(), token[9].getValueAsFloat());
        m.emis = new AcVector3(token[11].getValueAsFloat(), token[12].getValueAsFloat(), token[13].getValueAsFloat());
        m.spec = new AcVector3(token[15].getValueAsFloat(), token[16].getValueAsFloat(), token[17].getValueAsFloat());
        m.shi = token[19].getValueAsFloat();
        m.trans = token[21].getValueAsFloat();
        return m;*/
        return token[1].material;
    }
}


