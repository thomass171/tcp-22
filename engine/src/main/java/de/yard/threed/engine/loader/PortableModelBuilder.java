package de.yard.threed.engine.loader;

import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.platform.NativeGeometry;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleRegistry;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;

import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.resource.URL;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.PerspectiveCamera;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Texture;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.ResourceLoaderFromBundle;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 06.12.22: A builder for universal model specification.
 */
public class PortableModelBuilder {

    static Log logger = Platform.getInstance().getLog(PortableModelBuilder.class);
    // Material, das verwendet wird, wenn das eigentlich definierte nicht bekannt ist
    private NativeMaterial dummyMaterial;

    //ResourcePath defaulttexturebasepath;
    PortableModelList pml;
    //Just an indicator for testing
    public boolean dummymaterialused;

    public PortableModelBuilder(PortableModelList pml) {
        this.pml = pml;
    }



    /*public PortableModelBuilder(ResourcePath texturebasepath, List<GeoMat> gml) {
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
        }* /
    }*/

    /**
     * ResourceLoader is needed in builder for loading textures.
     * <p>
     * alttexturepath might be null. Then texturebasepath is used.
     *
     * @return
     */
    public SceneNode buildModel(ResourceLoader resourceLoader) {
        return buildModel(resourceLoader, null);
    }

    public SceneNode buildModel(ResourceLoader resourceLoader, ResourcePath alttexturepath) {
        SceneNode rootnode = new SceneNode();
        if (pml.getName() != null) {
            rootnode.setName(pml.getName());
        }
        for (int i = 0; i < pml.getObjectCount(); i++) {
            SceneNode newModel = buildModel(resourceLoader, pml.getObject(i), alttexturepath);
            String parent = pml.getParent(i);
            SceneNode destinationNode = rootnode;
            if (parent != null) {
                logger.debug("looking for parent " + parent);
                NativeCamera camera = AbstractSceneRunner.getInstance().findCameraByName(parent);
                PerspectiveCamera perspectiveCamera = new PerspectiveCamera(camera);
                destinationNode = perspectiveCamera.getCarrier();
                // attach to carrier will propagate layer. newModel.getTransform().setLayer(perspectiveCamera.getLayer());
                logger.debug("found parent camera with layer " + perspectiveCamera.getLayer());
            }
            destinationNode.attach(newModel);
        }
        return rootnode;
    }

    private SceneNode buildModel(ResourceLoader resourceLoader, PortableModelDefinition obj, ResourcePath alttexturepath/*, boolean dummywegensignatureindeutigkeit*/) {
        //this.bundle = bundle;
        //this.rpath = rpath;
        ResourcePath nr = pml.defaulttexturebasepath;
        if (alttexturepath != null) {
            nr = alttexturepath;
        }
        return buildObject(resourceLoader, obj /*, null/*, matlist*/, nr);
    }

    private SceneNode buildObject(ResourceLoader resourceLoader, PortableModelDefinition obj, /*MaterialPool matpool,*/ ResourcePath texturebasepath) {
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
        List<PortableMaterial/*NativeMaterial*/> matlist = buildMatlist(/*13.2.24 bundle,*/ obj, /*matpool,*/ texturebasepath);
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
                SceneNode m = buildSceneNode(resourceLoader, obj.geolist, matlist, false, false, texturebasepath/*3.5.19, obj.texture*/);
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
                NativeMaterial nmat = buildMaterialFromPortableMaterial(resourceLoader, mate, texturebasepath, /*3.5.19obj.texture, */obj.geolist.get(0).getNormals() != null);


                Mesh mesh = new Mesh(new GenericGeometry(obj.geolist.get(0)).getNativeGeometry(), nmat/*mate*/, false, false);
                model.setMesh(mesh);
            }
        }

        for (int i = 0; i < obj.kids.size(); i++) {
            model.attach(buildObject(resourceLoader, obj.kids.get(i)/*, matpool/*, matlist*/, texturebasepath));
        }
        if (Config.modelloaddebuglog) {
            logger.debug("buildObject complete " + name);
        }
        return model;
    }

    NativeMaterial buildMaterialFromPortableMaterial(ResourceLoader resourceLoader, /*Bundle bundle,*/ PortableMaterial portableMaterial, ResourcePath texturebasepath/*3.5.19, String objtexture*/, boolean hasnormals) {
        NativeMaterial nmat;
        if (portableMaterial != null) {
            //Auf AC zugeschnitten, denn nur die(?) haben die Textur am Object.
            //22.12.17: Darum jetzt bevorzugt den Texturename aus dem Material.
            Material ma = /*PortableMaterial.*/buildMaterial(/*bundle*/resourceLoader, portableMaterial, /*3.5.19(portableMaterial.texture != null) ? * 14.2.24 /portableMaterial.texture/*3.5.19 : objtexture*/ texturebasepath, hasnormals);
            if (ma == null) {
                logger.warn("No material. Using dummy material.");
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
    public SceneNode buildSceneNode(ResourceLoader resourceLoader, /*Bundle bundle,*/ List<SimpleGeometry> geolist, List<PortableMaterial> material, boolean castShadow, boolean receiveShadow, ResourcePath texturebasepath/*3.5.19, String objtexture*/) {
        //this();
        SceneNode mainsn = new SceneNode();
        int i = 0;
        for (SimpleGeometry geo : geolist) {
            NativeGeometry ng = EngineHelper.buildGeometry(geo.getVertices(), /*new SmartArrayList<Face3List>(*/geo.getIndices(), geo.getUvs(), geo.getNormals());
            //2.5.19: Neu an dieser Stelle
            NativeMaterial nmat = buildMaterialFromPortableMaterial(resourceLoader, material.get(i), texturebasepath, /*3.5.19objtexture,*/ geo.getNormals() != null);

            Mesh submesh = new Mesh(ng, nmat/*material.get(i)*/, castShadow, receiveShadow);
            // add(new SceneNode(submesh));
            SceneNode sn = new SceneNode(submesh);
            sn.nativescenenode.getTransform().setParent(mainsn.nativescenenode.getTransform());
            i++;
        }
        return mainsn;
    }

    /**
     * 27.12.17: public static, um allgemeingueltig aus einem LoadedMaterial ein Material zu machen. War frueher in LoadedFile.
     * 30.12.18: For loading a texture, the model origin (eg. a bundle) and an optional different path is needed. The texture name is taken from the material.
     * Also an absolute path in texture name like "bundle:/xx/yy/zz.png" is possible. Then it can be loaded without further information.
     * <p>
     * Returns mull, wenn bei Texturen Bundle oder Path fehlen. Aufrufer kann DummyMaterial verwenden oder auch keins (wird dann wireframe).
     * 13.2.24: Shouldn't need a bundle any more.
     *
     * @param mat
     * @param texturebasepath for bundle and HTTP. Might only be null with absolute texturename. Is relative to resourceloader or absolute path (eg. "engine:cesiumbox").
     * @return
     */
    public /*10.4.17*/ static /*Native*/Material buildMaterial(ResourceLoader resourceLoader, PortableMaterial mat, ResourcePath texturebasepath, boolean hasnormals) {
        NativeMaterial nmat;
        //SHADED ist der Defasult
        HashMap<NumericType, NumericValue> parameters = new HashMap<NumericType, NumericValue>();
        if (!mat.shaded) {
            parameters.put(NumericType.SHADING, new NumericValue(NumericValue.UNSHADED));
        } else {
            if (!hasnormals) {
                parameters.put(NumericType.SHADING, new NumericValue(NumericValue.FLAT));
            }
        }

        if (mat.texture != null) {
            String texturename = mat.texture;
            /*21.12.16 nicht mehr noetig wegen ResourcePath if (texturebasepath == null) {
                texturebasepath = ".";
            }*/
            if (StringUtils.contains(texturename, ":")) {
                // use case isn't gltf but manually build model definitions. Is a kind of absolute texture path.
                int index = StringUtils.indexOf(texturename, ":");
                texturebasepath = null;
                // texturename in resourceloader will be replaced later anyway, so just "" is ok.
                Bundle bundle = BundleRegistry.getBundle(StringUtils.substring(texturename, 0, index));
                if (bundle == null) {
                    logger.warn("bundle not found:" + StringUtils.substring(texturename, 0, index));
                }
                resourceLoader = new ResourceLoaderFromBundle(new BundleResource(bundle, null, ""));
                texturename = StringUtils.substring(texturename, index + 1);
            } else {
                if (texturebasepath == null) {
                    logger.warn("no texturebasepath. Not building material.");
                    return null;
                }
            }
            Texture texture;
            HashMap<String, NativeTexture> map = new HashMap<String, NativeTexture>();

            if (resourceLoader != null) {
                //BundleResource br = new BundleResource(texturebasepath, texturename);
                //br.bundle = bundle;
                URL br = resourceLoader.fromRootReference(texturebasepath, texturename).getUrl();
                texture = new Texture/*.buildBundleTexture*/(br, mat.wraps, mat.wrapt);
                if (texture.texture == null) {
                    // 13.9.23: Better to log this
                    logger.warn("failed to build texture from " + texturename + " at " + texturebasepath);
                    texturename = texturename;
                }
                map.put("basetex", texture.texture);

            } else {
                // 26.4.17: resourceLoader muat exist
                logger.error("bundle not set");

            }
            //map.put("normalmap",normalmap.texture);
            //TODO die anderen Materialparameter
            nmat = Platform.getInstance().buildMaterial(null, null, map, parameters, null);
        } else {
            HashMap<ColorType, Color> color = new HashMap<ColorType, Color>();
            color.put(ColorType.MAIN, mat.color);
            //TODO die restlichen colors
            // 25.4.19 unshaded wird oben zwar schon eingetragen, aber nicht immer. "shaded" ist eh etwas unklar. Auf jeden Fall bleibt ein Material mit Color in JME sonst schwarz.
            //Darum erstmal immer setzen, bis klar ist, was mit Property "shaded" ist. 28.4.19: Das ist aber doof, nur weil JME die combination shaded/ambientLight schwarz darstellt.
            //Evtl. wegen Normale?
            //parameters.put(NumericType.UNSHADED, new NumericValue(1));
            nmat = Platform.getInstance().buildMaterial(null, color, null, parameters, null);
        }
        return new Material(nmat);
    }

    /**
     * Eine Liste der Materialien EINES Objects anlegen.
     * Nur die in den Facelisten des Objekts wirklich verwendeten Materialien anlegen. Sonst stimmt
     * die Zuordnung FaceList->Material ueber denselben Index nicht.
     * Das Bundle wird zum Finden von Texturen gebraucht.
     * 2.5.19: Spaetestens seit Flat Shading ist es noch nicht sicher, wie das Material konkret angelegt werden muss. Darum
     * die Matlist nur mit MaterialDefinitionen anlegen
     */
    private List<PortableMaterial/*NativeMaterial*/> buildMatlist(/*13.2.24 Bundle bundle,*/ PortableModelDefinition obj, /*MaterialPool matpool,*/ ResourcePath texturebasepath) {
        List<PortableMaterial> matlist = new ArrayList<PortableMaterial>();
        int index = 0;
        for (String matname : obj.geolistmaterial) {
            PortableMaterial mat = pml.findMaterial(matname);
            //das kann auch null sein. Dann wird später ein Dummy angelegt.
            matlist.add(mat);

        }
        return matlist;
    }


    /**
     * Material, das verwendet wird, wenn das eigentlich definierte nicht bekannt ist.
     * Einfach schlicht blau.
     * Better white to make more clear its not intended.
     */
    private NativeMaterial getDummyMaterial() {
        dummymaterialused = true;
        if (dummyMaterial == null) {
            HashMap<ColorType, Color> color = new HashMap<ColorType, Color>();
            color.put(ColorType.MAIN, Color.WHITE);
            dummyMaterial = (Platform.getInstance()).buildMaterial(null, color, null, null, null);
        }
        return dummyMaterial;
    }


}
