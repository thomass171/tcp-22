package de.yard.threed.tools;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.loader.AcToken;
import de.yard.threed.core.loader.AsciiLoader;
import de.yard.threed.core.platform.Log;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoadedObject;
import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.loader.StringReader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 12.02.16.
 */
public class LoaderOBJ extends AsciiLoader {
    Log logger = Platform.getInstance().getLog(LoaderOBJ.class);
    // Sample aus Wikipedia
    public static final String sampleobj = "# Blender v2.71 (sub 0) OBJ File: \n" +
            "# www.blender.org\n" +
            "mtllib Cube.mtl\n" +
            "o Cube\n" +
            "v 1.000000 -1.000000 -1.000000\n" +
            "v 1.000000 -1.000000 1.000000\n" +
            "v -1.000000 -1.000000 1.000000\n" +
            "v -1.000000 -1.000000 -1.000000\n" +
            "v 1.000000 1.000000 -0.999999\n" +
            "v 0.999999 1.000000 1.000001\n" +
            "v -1.000000 1.000000 1.000000\n" +
            "v -1.000000 1.000000 -1.000000\n" +
            "vn 0.000000 -1.000000 0.000000\n" +
            "vn 0.000000 1.000000 0.000000\n" +
            "vn 1.000000 -0.000000 0.000000\n" +
            "vn -0.000000 -0.000000 1.000000\n" +
            "vn -1.000000 -0.000000 -0.000000\n" +
            "vn 0.000000 0.000000 -1.000000\n" +
            "g Cube_Cube_Material\n" +
            "usemtl Material\n" +
            "s off\n" +
            "f 2//1 3//1 4//1\n" +
            "f 8//2 7//2 6//2\n" +
            "f 1//3 5//3 6//3\n" +
            "f 2//4 6//4 7//4\n" +
            "f 7//5 8//5 4//5\n" +
            "f 1//6 4//6 8//6\n" +
            "f 1//1 2//1 4//1\n" +
            "f 5//2 8//2 6//2\n" +
            "f 2//3 1//3 6//3\n" +
            "f 3//4 2//4 7//4\n" +
            "f 3//5 7//5 4//5\n" +
            "f 5//6 1//6 8//6";

    private StringReader ins;

    public LoaderOBJ(StringReader ins) throws InvalidDataException {
        this.ins = ins;
        load();
    }

    @Override
    protected void doload() throws InvalidDataException {

        AcToken[] token;
        ObjObject currentobject = null;
        ObjSurface currentsurface = null;
        boolean invertices = false;
        boolean inrefs = false;
        int surfaceindex = 0;

        while ((token = parseLine(ins.readLine(), true,  inrefs?1:0,null)) != null) {
            //dumpen
            //System.out.print("line " + lines + "(" + token.length + " token):");
            for (AcToken tk : token) {
                // System.out.print(tk + " ");
            }
            //System.out.println();

            if (token.length > 0) {
                if (token[0].isMtlLib()) {
                    readMtlLib();
                } else {
                    if (token[0].isUsemtl()) {
                        //TODO  material.add(new AcMaterial(token));
                    } else {
                        if (token[0].isO()) {
                            currentobject = new ObjObject(token[1]);
                            loadedfile.objects.add(currentobject);
                            // Hier gibt es nur eine Facelist. Darum sofort anlegen
                            currentobject.addFacelist();
                        } else {
                            if (token[0].isL()) {
                                //TODO
                            } else {
                                if (token[0].isS()) {
                                } else {
                                    if (token[0].isVT()) {
                                    } else {
                                        if (token[0].isVN()) {
                                            addVector3N(currentobject.normals, token);
                                        } else {
                                            if (token[0].isG()) {
                                            } else {
                                                if (token[0].isV()) {
                                                    addVector3(currentobject.vertices, token);

                                                } else {
                                                    if (token[0].isF()) {
                                                        currentobject.addFace(token);
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
            lineno++;
        }
        //TODO close stream
    }

    @Override
    protected Log getLog() {
        return logger;
    }


    private void readMtlLib() {

    }


}

class ObjObject extends LoadedObject {
    //die Normals muessen nur temporaer gespeichert werden, weil sie nachher in die Faces
    //kommen
    public List<Vector3> normals = new ArrayList<Vector3>();
    //int vcount = 0;
    //public String name;
    //public AcVector3 location;
    public String texture;
    public ObjSurface[] surface;

    public ObjObject(AcToken token) {
        surface = new ObjSurface[1];
        surface[0] = new ObjSurface(0);
    }


    public void addFace(AcToken[] token) throws InvalidDataException {
        //erstes Token ist "f". Dann brauche ich 3 Einheiten
        /*if ((token.length -1) % 3 != 0){
            throw new InvalidAcDataException("number of tupel no multiple of 3");
        }
        int units = (token.length -1) / 3;
        int stride = units;
       Face3 face = addFace(token[1].intvalue,token[1+stride].intvalue,token[1+2*stride].intvalue);
        switch (units){
            case 1:
                // vertex only
                break;
            case 2:
                break;
            case 3:
                for (int i=0;i<3;i++) {
                    face.normals.add(normals.get(token[3+i*stride].intvalue));
                }
        }*/
        int t = 1;
        Face3 face = addFace(token[t].vi[0] - 1, token[t].vi[1] - 1, token[t].vi[2] - 1);
        if (token[t].ni != null) {
            // 13.7.16: OBJ kann tatsaechlich pro Faceindex eine Normale definieren. Da ich nur eine Normale am Face habe, keine nehmen.
            // Die  muessten wahrscheinlich für Smoothing genutzt werden. Ueberhaupt: Wie haben sich die Formatdesigner das denn vorgestellt.
            // Oder einfach alles in die Normalenliste ueberbehmen. Dann ist es vielleicht relativ einfach TODO
            /*face.normals = new ArrayList<Vector3>();
            for (int i = 0; i < 3; i++) {
                face.normals.add(normals.get(token[t].ni[i] - 1));
            }
            */
        }
    }
}

class ObjSurface {
    int type;
    //V2[] uv = null;
    List<int[]> faces = new ArrayList<int[]>();
    public int mat;

    public ObjSurface(int type) {
        this.type = type;
    }

    public void addFace3(AcToken[] indexes) {
        faces.add(new int[]{indexes[0].intvalue, indexes[1].intvalue, indexes[2].intvalue});
    }
}

