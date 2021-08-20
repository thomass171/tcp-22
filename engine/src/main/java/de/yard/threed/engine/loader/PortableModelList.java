package de.yard.threed.engine.loader;

import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeGeometry;
import de.yard.threed.core.platform.NativeMaterial;


import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.*;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Serialisierbar zum Cachen.
 * MA19: Das wird jetzt die Hauptklasse zur Anbindung von GLTF (statt serialisieren).
 * Koennte deswegen vielleicht umbenannt werden. Ach, fuer GLTF gibts einen eigenen Loader. Das passt besser.
 * 11.12.17: Das preprocess Resultat ausgelagert nach PreprocessObject/File. Kann man später nochmal umbenennen.
 * <p/>
 * 7.6.18: PreprocessedLoadedFile->PortableModelList
 * Alles was hier drinsteht muss per GLTF imp/exportierbar sein!
 * 30.12.18: Vielleich die build* mal in eine Factory auslagern.
 *
 * 06.03.21: Das ist doch ein universelles Zwischenformat
 * <p>
 * Created by thomass on 10.06.16.
 */
public class PortableModelList {
    //voruebergehende BTG Kruecke
    public List<GeoMat> gml = null;
    Log logger = Platform.getInstance().getLog(PortableModelList.class);
    int MAGIC = 38;
    int VERSION = 1;
    public List<PortableMaterial> materials = new ArrayList<PortableMaterial>();
    //30.12.18: Muesste es nicht eher "models" heissen? Objekte sind es ja noch nicht.
    public List<PortableModelDefinition> objects = new ArrayList<PortableModelDefinition>();
    // Material, das verwendet wird, wenn das eigentlich definierte nicht bekannt ist     
    private NativeMaterial dummyMaterial;
    //TODO besser double?? Noch wichtiger: Das ist BTG spezifisch und hat hier deswegen und wegen GLTF nichts zu suchen
    public /*SGVec3d*/ Vector3 gbs_center;
    // Die Texturen werden dort erwartet, also in dem ResourcePAth, wo auch das Model liegt.
    // wird nicht mit serialisiert. Ist NativeResoure um zu erkennen, ob bundled. Auch nicht schoen.
    // 30.12.18: Und eine Bundleinformation ist nicht unbedingt eine Eigenschaft der Models, nur ein Pfad, wo Texturen zu finden sind.
    public ResourcePath defaulttexturebasepath;
    //public Bundle bundle;
    //ResourcePath rpath;
    public Vector3 btgcenter;
    // um einen definierten Einstieg zu haben, z.B. für AC world, aber auch GLTF. NeeNee, das ist doch Quatsch. 
    // Es entsteht erst beim Model bauen eine root Node mit file name als Container. Aber einen name gibt es.
    //public PreprocessedLoadedObject root=null;
    // Ein uebergeordneter Name, ohne funktionale Bedeutung, wird aber fuer rootnode verwendet Z.B. eine Herkunftsangabe (source).
    private String name = null;
    public int loaddurationms;
    //Just an indicator for testing
    public boolean dummymaterialused;

    public PortableModelList(ResourcePath texturebasepath) {
        this.defaulttexturebasepath = texturebasepath;
    }

    /**
     * Wiederherstellen aus serialisiertem Objekt
     * acpp reader? deprecated?
     *
    */
    /*3.5.19public PortableModelList(/*NativeResource file,* / ByteArrayInputStream ins, ResourcePath texturebasepath) throws InvalidDataException {
        try {
            int magic = ins.readInt();
            if (Config.loaderdebuglog) {
                logger.debug("magic=" + magic);
            }
            int version = ins.readInt();
            if (Config.loaderdebuglog) {
                logger.debug("version=" + version);
            }
            int cnt = ins.readInt();
            if (Config.loaderdebuglog) {
                logger.debug("material.cnt=" + cnt);
            }
            for (int i = 0; i < cnt; i++) {
                materials.add(new PortableMaterial(ins));
            }
            cnt = ins.readInt();
            for (int i = 0; i < cnt; i++) {
                objects.add(new PortableModelDefinition(ins));
            }
            this.defaulttexturebasepath = texturebasepath;
        } catch (java.lang.Exception e) {
            String msg = "reading LoadedFile from pp failed : " + e.getMessage() + " for file " /*+ file.getFullName()* /;
            logger.error(msg, e);
            throw new InvalidDataException(msg, e);
        }
    }*/

    public PortableModelList(ResourcePath texturebasepath, List<GeoMat> gml) {
        this.defaulttexturebasepath = texturebasepath;
        this.gml = gml;
        /*TODO erstmal material in gltf klaeren for (GeoMat gm : gml){
            PreprocessedLoadedObject ppo = new PreprocessedLoadedObject();
            ppo.geolist=new ArrayList<SimpleGeometry>();
            ppo.geolist.add(gm.geo);
            objects.add(ppo);
            LoadedMaterial ppm = new LoadedMaterial();
            ppm.
            materials.add(ppm);
        }*/
    }

    public void resetAfterDeserialize() {
        Log logger = Platform.getInstance().getLog(PortableModelList.class);
        dummyMaterial = null;
    }

    public String dumpObject(String offset, LoadedObject obj, String separator) {
        String s = offset + "OBJECT:";
        s += offset + "name=" + obj.name + separator;
        if (obj instanceof AcObject) {
            s += offset + "type=" + ((AcObject) obj).type + separator;
            s += offset + "location=" + ((AcObject) obj).location + separator;
            s += offset + "texture=" + ((AcObject) obj).texture + separator;
            s += offset + "kids=" + ((AcObject) obj).kids.size() + separator;

        }
        s += offset + "vertices (" + obj.vertices.size() + "):";
        for (int i = 0; obj.vertices != null && i < obj.vertices.size(); i++) {
            //zu viel Output
            // s +=n(offset + obj.vertices[i]);
        }
        s += separator;
        s += offset + "face lists (" + obj.faces.size() + "):" + separator;
        int index = 0;
        if (obj.faces != null) {
            for (FaceList surface : obj.getFaceLists()) {
                s += offset + "faces (" + surface.faces.size() + " with material " + obj.facelistmaterial + "):" + separator;
                //zu viel Output
                // s +=(offset + "type=" + surface.type);
                // s +=(offset + "mat=" + surface.mat);
                //s +=(offset + "refs:");
                //for (int i = 0; i < surface.uv.length; i++) {
                //  s +=(offset + "  " + surface.vref[i] + "," + surface.uv[i]);
                //}
            }
            index++;
        }
        for (int i = 0; i < obj.kids.size(); i++) {
            s += dumpObject("  " + offset, obj.kids.get(i), separator);
        }
        return s;
    }

    public String dumpMaterial(String separator) {
        String s = "MATERIAL:" + separator;
        for (PortableMaterial mat : materials) {
            s += " name=" + mat.name + separator;
            s += " color/diffuse=" + mat.color + separator;
            s += " ambient=" + mat.ambient + separator;
            s += " emis=" + mat.emis + separator;
            s += " spec=" + mat.specular + separator;
            s += " shi=" + mat.getShininess() + separator;
            s += " trans=" + mat.getTransparencypercent() + separator;
        }
        return s;
    }


    /**
     * Die Methode ist fuer innerhalb des Modellesens nicht sinnvoll, weil Materialien nicht immer im Modelfile enthalten sind(z.B. BTG, OBJ).
     * Und dann ist es auch keine Exception, wenn das Material nicht gefunden wird. Also, keine Exception, einfach return null.
     *
     * @param name
     * @return
     */
    public PortableMaterial findMaterial(String name) {
        for (PortableMaterial m : materials) {
            if (m.name != null && m.name.equals(name)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Returns -1 if material isType not known. Might happen eg. for landclasses in BTG.
     *
     * @param name
     * @return
     */
    public int findMaterialIndex(String name) {
        for (int i = 0; i < materials.size(); i++) {
            if (materials.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Findet das erste.
     *
     * @param name
     * @return
     */
    public PortableModelDefinition findObject(String name) {
        //  for (int i = 0; i < objects.size(); i++) {
        return findObject(objects, name);

    }

    public PortableModelDefinition findObject(List<PortableModelDefinition> objs, String name) {
        for (int i = 0; i < objs.size(); i++) {
            PortableModelDefinition obj = objs.get(i);

            if (obj.name != null && obj.name.equals(name)) {
                return obj;
            }
            PortableModelDefinition o = findObject(obj.kids, name);
            if (o != null) {
                return o;
            }
        }
        return null;
    }


    /**
     *
     */
    /*30.1218 @Deprecated
    public SceneNode buildModel(Bundle bundle, PortableModelDefinition obj, String alttexturepath, boolean dummywegensignatureindeutigkeit) {
        ResourcePath nr = texturebasepath;
        if (alttexturepath != null) {
            nr = new ResourcePath(alttexturepath);//new FileSystemResource(new ResourcePath(alttexturepath), "");
        }
        return buildObject(bundle, obj, null/*, matlist* /, nr);
    }*/
    public SceneNode buildModel(Bundle bundle) {
        return buildModel(bundle, null);
    }

    public SceneNode buildModel(Bundle bundle, /*ResourcePath rpath,*/ ResourcePath alttexturepath/*, boolean dummywegensignatureindeutigkeit*/) {
        SceneNode rootnode = new SceneNode();
        if (name != null) {
            rootnode.setName(name);
        }
        for (int i = 0; i < objects.size(); i++) {
            rootnode.attach(buildModel(bundle, /*rpath,*/ objects.get(i), alttexturepath/*, dummywegensignatureindeutigkeit*/));
        }
        return rootnode;
    }

    /**
     * Das Bundle wird gebraucht, um von dort z.B. Texturen zu laden.
     * <p>
     * alttexturepath darf null sein. Dann wird texturebasepath verwendet.
     *
     * @return
     */
    private SceneNode buildModel(Bundle bundle, /*ResourcePath rpath,*/ PortableModelDefinition obj, ResourcePath alttexturepath/*, boolean dummywegensignatureindeutigkeit*/) {
        //this.bundle = bundle;
        //this.rpath = rpath;
        ResourcePath nr = defaulttexturebasepath;
        if (alttexturepath != null) {
            nr = alttexturepath;
        }
        return buildObject(bundle, obj /*, null/*, matlist*/, nr);
    }

    /*30.12.18 public SceneNode buildModel(Bundle bundle, PortableModelDefinition obj, MaterialPool matpool) {
        return buildObject(bundle, obj, matpool, null);
    }*/

    
    /*private MaterialPool buildMaterialPool(){

    }*/

    private SceneNode buildObject(Bundle bundle, PortableModelDefinition obj, /*MaterialPool matpool,*/ ResourcePath texturebasepath) {
        /*30.12.17 Es kann mit GLTF auch leere Objekte geben if (obj.geolist == null) {
            throw new RuntimeException("geo not preprocessed");
        }*/
        // Eine Liste der Materialien DIESES Objects anlegen.
        // Nur die in den Facelisten des Objekts wirklich verwendeten Materialien anlegen. Sonst stimmt
        // die Zuordnung nicht. 2.5.19: Spaetestens seit Flat Shading ist es noch nicht sicher, wie das Material konkret angelegt werden muss. Darum
        // die Matlist nur mit MaterialDefinitionen anlegen.
        // 2.5.19: matpool: aknn man sicher sein, dass es wirklich dasselbe ist? Z.B (un/flat)shaded. Eigentlich ist es doch ein zugrosses Risiko im Vergleich zum Nutzen, oder?
        // mal weglassen
        /*30.12.17: das pruef ich auch nicht mehr
        if (obj.geolistmaterial.size() == 0) {
            // Kommt wolh bei RollerCoaster.ac schon mal vor. 
            logger.warn("facelistmaterial isType empty in " + obj.name);
        }*/
        List<PortableMaterial/*NativeMaterial*/> matlist = buildMatlist(bundle, obj, /*matpool,*/ texturebasepath);
        boolean wireframe = false;
        if (wireframe) {
            for (int i = 0; i < matlist.size(); i++) {
                matlist.set(i, null);
            }
        }

        SceneNode model = new SceneNode();
        String name;
        if (StringUtils.empty(obj.name)) {
            name = "<no name>";
        } else {
            name = obj.name;
        }

        model.setName(name);
        if (obj.translation != null) {
            model.getTransform().setPosition((obj.translation));
        }
        if (obj.rotation != null) {
            model.getTransform().setRotation((obj.rotation));
        }
        // Keine leeren Objekte anlegen (z.B. AC World). Als Node aber schon. Die AC kids Hierarchie soll erhalten bleiben.
        if (obj.geolist != null && obj.geolist.size() > 0) {
            if (obj.geolist.size() > 1/*faces.size() > 1 && matlist.size() > 1*/) {
                //List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(obj.vertices, obj.faces, /*matlist,*/ null, true, obj.crease);
                //2.5.19: Gibt es das seit GLTF ueberhauot noch? Ja, eine Node kann doch mehrere Meshes haben, oder?
                SceneNode m = buildSceneNode(bundle, obj.geolist, matlist, false, false, texturebasepath/*3.5.19, obj.texture*/);
                m.setName(obj.name);
                model.attach(m);
            } else {
                //List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(obj.vertices, obj.faces, /*matlist,*/ null, false, obj.crease);
                //if (geolist.size() > 1) {
                //  throw new RuntimeException("unexpected multiple geos");
                //}
                if (obj.geolist.size() == 0) {
                    logger.error("no geo list");
                }
                PortableMaterial/*NativeMaterial*/ mate = null;
                if (matlist.size() == 0) {
                    logger.error("no matlist in " + obj.name);
                } else {
                    mate = matlist.get(0);
                }
                //2.5.19: Neu an dieser Stelle
                NativeMaterial nmat = buildMaterialFromPortableMaterial(bundle, mate, texturebasepath, /*3.5.19obj.texture, */obj.geolist.get(0).getNormals() != null);


                Mesh mesh = new Mesh(new GenericGeometry(obj.geolist.get(0)).getNativeGeometry(), nmat/*mate*/, false, false);
                model.setMesh(mesh);
            }
        }

        for (int i = 0; i < obj.kids.size(); i++) {
            model.attach(buildObject(bundle, obj.kids.get(i)/*, matpool/*, matlist*/, texturebasepath));
        }
        if (Config.modelloaddebuglog) {
            logger.debug("buildObject complete " + name);
        }
        return model;
    }

    NativeMaterial buildMaterialFromPortableMaterial(Bundle bundle, PortableMaterial portableMaterial, ResourcePath texturebasepath/*3.5.19, String objtexture*/, boolean hasnormals) {
        NativeMaterial nmat;
        if (portableMaterial != null) {
            //Auf AC zugeschnitten, denn nur die(?) haben die Textur am Object.
            //22.12.17: Darum jetzt bevorzugt den Texturename aus dem Material.
            Material ma = PortableMaterial.buildMaterial(bundle, portableMaterial, /*3.5.19(portableMaterial.texture != null) ? */portableMaterial.texture/*3.5.19 : objtexture*/, texturebasepath, hasnormals);
            if (ma == null) {
                nmat = getDummyMaterial();
            } else {
                nmat = ma.material;
            }
        } else {
            nmat = getDummyMaterial();
        }
        return nmat;
    }

    /**
     * Teil einer fruehere Platform.buildMeshG() Methode.
     * 10.3.16: Da auch WebGl und Opengl keine multiple material koennen, den extract aus JME hierauf gezogen. Auch weil es erstmal ganz gut funktioniert.
     * 02.05.16: Im Sinne der Vereinfachung generell keine multiple material vorsehen, auch wenn Unity es kann. Die Engine extrahiert jetzt Submeshes.
     * Ist in Model statt in Mesh, weil durch die Aufsplittung ja auch ein Model rauskommt. Die Methode ist jetzt auch wirklich nur fuer Multi Material gedacht.
     * 2.5.19: War mal Constructor von SceneNode. Passt da aber nicht hin.
     */
    public SceneNode buildSceneNode(Bundle bundle, List<SimpleGeometry> geolist, List<PortableMaterial/*NativeMaterial*/> material, boolean castShadow, boolean receiveShadow, ResourcePath texturebasepath/*3.5.19, String objtexture*/) {
        //this();
        SceneNode mainsn = new SceneNode();
        int i = 0;
        for (SimpleGeometry geo : geolist) {
            NativeGeometry ng = EngineHelper.buildGeometry(geo.getVertices(), /*new SmartArrayList<Face3List>(*/geo.getIndices(), geo.getUvs(), geo.getNormals());
            //2.5.19: Neu an dieser Stelle
            NativeMaterial nmat = buildMaterialFromPortableMaterial(bundle, material.get(i), texturebasepath, /*3.5.19objtexture,*/ geo.getNormals() != null);

            Mesh submesh = new Mesh(ng, nmat/*material.get(i)*/, castShadow, receiveShadow);
            // add(new SceneNode(submesh));
            SceneNode sn = new SceneNode(submesh);
            sn.nativescenenode.getTransform().setParent(mainsn.nativescenenode.getTransform());
            i++;
        }
        return mainsn;
    }

    /**
     * Eine Liste der Materialien EINES Objects anlegen.
     * Nur die in den Facelisten des Objekts wirklich verwendeten Materialien anlegen. Sonst stimmt
     * die Zuordnung FaceList->Material ueber denselben Index nicht.
     * Das Bundle wird zum Finden von Texturen gebraucht.
     * 2.5.19: Spaetestens seit Flat Shading ist es noch nicht sicher, wie das Material konkret angelegt werden muss. Darum
     * die Matlist nur mit MaterialDefinitionen anlegen
     */
    private List<PortableMaterial/*NativeMaterial*/> buildMatlist(Bundle bundle, PortableModelDefinition obj, /*MaterialPool matpool,*/ ResourcePath texturebasepath) {
        List<PortableMaterial> matlist = new ArrayList<PortableMaterial>();
        int index = 0;
        for (String matname : obj.geolistmaterial) {
            PortableMaterial mat = findMaterial(matname);
            //das kann auch null sein. Dann wird später ein Dummy angelegt.
            matlist.add(mat);
            /*NativeMaterial nmat;
            if (matpool == null) {
                PortableMaterial mat = findMaterial(matname);
                if (mat != null) {
                    //Auf AC zugeschnitten, denn nur die(?) haben die Textur am Object.
                    //22.12.17: Darum jetzt bevorzugt den Texturename aus dem Material.
                    Material ma = PortableMaterial.buildMaterial(bundle, mat, (mat.texture != null) ? mat.texture : obj.texture, texturebasepath);
                    if (ma == null) {
                        nmat = getDummyMaterial();
                    } else {
                        nmat = ma.material;
                    }
                } else {
                    // Material unbekannt. Dann mal irgendwas
                    logger.warn("unknown material " + matname + ". Using dummy material.");
                    nmat = getDummyMaterial();
                }
            } else {
                Material m = matpool.get(matname);
                if (m == null) {
                    logger.warn("material not found: " + matname);
                    if (matpool.size() > 0) {
                        // 5.8.16 Dann einfach mal das erste nehmen. besser als Dummy?
                        nmat = matpool.get(matpool.keySet().iterator().next()).material;
                    } else {
                        nmat = getDummyMaterial();
                    }
                } else {
                    nmat = m.material;
                }
            }

            matlist.add(nmat);
            index++;*/
        }
        return matlist;
    }


    /**
     * Material, das verwendet wird, wenn das eigentlich definierte nicht bekannt ist.
     * Einfach schlicht blau.
     */
    private NativeMaterial getDummyMaterial() {
        dummymaterialused = true;
        if (dummyMaterial == null) {
            HashMap<ColorType, Color> color = new HashMap<ColorType, Color>();
            color.put(ColorType.MAIN, Color.BLUE);
            dummyMaterial = ( Platform.getInstance()).buildMaterial(null, color, null, null, null);
        }
        return dummyMaterial;
    }

    public void addModel(PortableModelDefinition model) {
        objects.add(model);
    }

    public void addMaterial(PortableMaterial material) {
        materials.add(material);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
