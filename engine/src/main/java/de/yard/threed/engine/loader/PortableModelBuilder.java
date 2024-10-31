package de.yard.threed.engine.loader;

import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.TreeNode;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.loader.PreparedModel;
import de.yard.threed.core.loader.PreparedObject;
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
import java.util.Map;

/**
 * 06.12.22: A builder for universal model specification.
 * // 2.5.19: matpool: aknn man sicher sein, dass es wirklich dasselbe ist? Z.B (un/flat)shaded. Eigentlich ist es doch ein zugrosses Risiko im Vergleich zum Nutzen, oder?
 * // mal weglassen
 */
public class PortableModelBuilder {

    static Log logger = Platform.getInstance().getLog(PortableModelBuilder.class);
    // Material, das verwendet wird, wenn das eigentlich definierte nicht bekannt ist
    private NativeMaterial dummyMaterial;

    //ResourcePath defaulttexturebasepath;
    //PortableModelList pml;
    //Just an indicator for testing
    public static List<String> dummyMaterialReasons = new ArrayList<String>();

    /*public PortableModelBuilder(PortableModel pml) {
        this.pml = pml;
    }*/

    /**
     * ResourceLoader is needed in builder for loading textures.
     * <p>
     * alttexturepath might be null. Then texturebasepath is used.
     *
     * @return
     */
    public static SceneNode buildModel(PortableModel pml, ResourceLoader resourceLoader) {
        //return buildModel(pml, resourceLoader, null);
        return buildModel(prepareModel(pml, resourceLoader, null));
    }

    public static PreparedModel prepareModel(PortableModel pml, ResourceLoader resourceLoader) {
        //return buildModel(pml, resourceLoader, null);
        return prepareModel(pml, resourceLoader, null);
    }

    /**
     * 21.8.24: Only create geometries and materials but no objects yet. Useful for shared model.
     * resourceLoader is used for async texture load (with internal delegate).
     */
    public static PreparedModel prepareModel(PortableModel pml, ResourceLoader resourceLoader, ResourcePath alttexturepath) {
        PreparedModel rootnode = new PreparedModel(pml.getName());
        /*if (pml.getName() != null) {
            rootnode.setName(pml.getName());
        }*/

        TreeNode<PreparedObject> newModel = prepareModel(pml, resourceLoader, pml.getRoot()/*getObject(i)*/, alttexturepath, pml.defaulttexturebasepath);
        String parent = pml.getRoot().parent;
        //SceneNode destinationNode = rootnode;
        if (parent != null) {
            // 26.7.24: What is/was happening here? What is the use case? eg. SceneLoader.java
            logger.debug("looking for parent " + parent);
            rootnode.parent = parent;
        }
        //  destinationNode.attach(newModel);
        rootnode.root = newModel;

        return rootnode;
    }

    public static SceneNode buildModel(PreparedModel pml) {
        SceneNode rootnode = new SceneNode();
        if (pml.getName() != null) {
            rootnode.setName(pml.getName());
        }

        SceneNode newModel = buildModel(pml.getRoot()/*getObject(i)*/);
        String parent = pml./*getRoot().*/parent;
        SceneNode destinationNode = rootnode;
        if (parent != null) {
            // 26.7.24: What is/was happening here? What is the use case? eg. SceneLoader.java
            logger.debug("looking for parent " + parent);
            NativeCamera camera = AbstractSceneRunner.getInstance().findCameraByName(parent);
            PerspectiveCamera perspectiveCamera = new PerspectiveCamera(camera);
            destinationNode = perspectiveCamera.getCarrier();
            // attach to carrier will propagate layer. newModel.getTransform().setLayer(perspectiveCamera.getLayer());
            logger.debug("found parent camera with layer " + perspectiveCamera.getLayer());
        }
        destinationNode.attach(newModel);
        pml.useCounter++;
        return rootnode;
    }

    private static TreeNode<PreparedObject> prepareModel(PortableModel pml, ResourceLoader resourceLoader, PortableModelDefinition obj, ResourcePath alttexturepath, ResourcePath defaulttexturebasepath) {
        ResourcePath nr = defaulttexturebasepath;
        if (alttexturepath != null) {
            nr = alttexturepath;
        }
        return prepareObject(pml, resourceLoader, obj /*, null/*, matlist*/, nr);
    }

    private static SceneNode buildModel(TreeNode<PreparedObject>/*PortableModelDefinition*/ obj) {
        return buildObject(obj);
    }

    private static TreeNode<PreparedObject> prepareObject(PortableModel pml, ResourceLoader resourceLoader, PortableModelDefinition obj, ResourcePath texturebasepath) {
        // Eine Liste der Materialien DIESES Objects anlegen.
        // Nur die in den Facelisten des Objekts wirklich verwendeten Materialien anlegen. Sonst stimmt
        // die Zuordnung nicht. 2.5.19: Spaetestens seit Flat Shading ist es noch nicht sicher, wie das Material konkret angelegt werden muss. Darum
        // die Matlist nur mit MaterialDefinitionen anlegen.
        List<PortableMaterial/*NativeMaterial*/> matlist = buildMatlist(pml, obj, texturebasepath);
        boolean wireframe = false;
        if (wireframe) {
            for (int i = 0; i < matlist.size(); i++) {
                matlist.set(i, null);
            }
        }

        PreparedObject model = new PreparedObject(obj.name);
        TreeNode<PreparedObject> node = new TreeNode<PreparedObject>(model);

        if (obj.translation != null) {
            model.setPosition((obj.translation));
        }
        if (obj.rotation != null) {
            model.setRotation((obj.rotation));
        }
        // Keine leeren Objekte anlegen (z.B. AC World). Als Node aber schon. Die AC kids Hierarchie soll erhalten bleiben.
        if (obj.geo/*list*/ != null /*&& obj.geolist.size() > 0*/) {
            /*27.7.24 if (obj.geolist.size() > 1/*faces.size() > 1 && matlist.size() > 1* /) {
                //List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(obj.vertices, obj.faces, /*matlist,* / null, true, obj.crease);
                //2.5.19: Gibt es das seit GLTF ueberhauot noch? Ja, eine Node kann doch mehrere Meshes haben, oder?
                SceneNode m = buildSceneNode(resourceLoader, obj.geolist, matlist, false, false, texturebasepath/*3.5.19, obj.texture* /);
                m.setName(obj.name);
                model.attach(m);
            } else*/
            {
                //List<SimpleGeometry> geolist = GeometryHelper.prepareGeometry(obj.vertices, obj.faces, /*matlist,*/ null, false, obj.crease);
                //if (geolist.size() > 1) {
                //  throw new RuntimeException("unexpected multiple geos");
                //}
                /*27.7.24 if (obj.geolist.size() == 0) {
                    logger.error("no geo list");
                }*/
                PortableMaterial/*NativeMaterial*/ mate = null;
                if (matlist.size() == 0) {
                    logger.error("no matlist in " + obj.name);
                } else {
                    mate = matlist.get(0);
                }
                //2.5.19: Neu an dieser Stelle
                NativeMaterial nmat = buildMaterialFromPortableMaterial(resourceLoader, mate, texturebasepath, /*3.5.19obj.texture, */obj.geo/*list.get(0)*/.getNormals() != null, new DefaultMaterialFactory());
                model.setMaterial(nmat);

                //Mesh mesh = new Mesh(new GenericGeometry(obj.geo/*list.get(0)*/).getNativeGeometry(), nmat/*mate*/, false, false);
                model.setGeometry(new GenericGeometry(obj.geo/*list.get(0)*/).getNativeGeometry());
            }
        }

        for (int i = 0; i < obj.kids.size(); i++) {
            node.addChild(prepareObject(pml, resourceLoader, obj.kids.get(i), texturebasepath));
        }
        if (Config.modelloaddebuglog) {
            logger.debug("prepareObject complete " + model.name);
        }
        return node;
    }

    private static SceneNode buildObject(TreeNode<PreparedObject>/*PortableModelDefinition*/ node) {

        PreparedObject/*PortableModelDefinition*/ obj = node.getElement();
        SceneNode model = new SceneNode();
        model.setName(obj.name);
        if (obj.position != null) {
            model.getTransform().setPosition((obj.position));
        }
        if (obj.rotation != null) {
            model.getTransform().setRotation((obj.rotation));
        }
        // There might be nodes without mesh
        if (obj.geometry != null) {
            Mesh mesh = new Mesh(obj.geometry, obj.material, false, false);
            model.setMesh(mesh);
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            model.attach(buildObject(node.getChild(i)));
        }
        if (Config.modelloaddebuglog) {
            logger.debug("buildObject complete " + obj.name);
        }
        return model;
    }

    static NativeMaterial buildMaterialFromPortableMaterial(ResourceLoader resourceLoader, /*Bundle bundle,*/ PortableMaterial portableMaterial, ResourcePath texturebasepath/*3.5.19, String objtexture*/, boolean hasnormals, MaterialFactory materialFactory) {
        NativeMaterial nmat;
        if (portableMaterial != null) {
            //Auf AC zugeschnitten, denn nur die(?) haben die Textur am Object.
            //22.12.17: Darum jetzt bevorzugt den Texturename aus dem Material.
            Material ma = /*PortableMaterial.*/materialFactory.buildMaterial(/*bundle*/resourceLoader, portableMaterial, /*3.5.19(portableMaterial.texture != null) ? * 14.2.24 /portableMaterial.texture/*3.5.19 : objtexture*/ texturebasepath, hasnormals);
            if (ma == null) {
                logger.warn("No material. Using dummy material.");
                nmat = getDummyMaterial();
                dummyMaterialReasons.add("no material built");
            } else {
                nmat = ma.material;
            }
        } else {
            nmat = getDummyMaterial();
            dummyMaterialReasons.add("no material");
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
    /*28.10.24 public SceneNode buildSceneNode(ResourceLoader resourceLoader, /*Bundle bundle,* / List<SimpleGeometry> geolist, List<PortableMaterial> material, boolean castShadow, boolean receiveShadow, ResourcePath texturebasepath/*3.5.19, String objtexture* /) {
        //this();
        SceneNode mainsn = new SceneNode();
        int i = 0;
        for (SimpleGeometry geo : geolist) {
            NativeGeometry ng = EngineHelper.buildGeometry(geo.getVertices(), /*new SmartArrayList<Face3List>(* /geo.getIndices(), geo.getUvs(), geo.getNormals());
            //2.5.19: Neu an dieser Stelle
            NativeMaterial nmat = buildMaterialFromPortableMaterial(resourceLoader, material.get(i), texturebasepath, /*3.5.19objtexture,* / geo.getNormals() != null);

            Mesh submesh = new Mesh(ng, nmat/*material.get(i)* /, castShadow, receiveShadow);
            // add(new SceneNode(submesh));
            SceneNode sn = new SceneNode(submesh);
            sn.nativescenenode.getTransform().setParent(mainsn.nativescenenode.getTransform());
            i++;
        }
        return mainsn;
    }*/


    /**
     * Eine Liste der Materialien EINES Objects anlegen.
     * Nur die in den Facelisten des Objekts wirklich verwendeten Materialien anlegen. Sonst stimmt
     * die Zuordnung FaceList->Material ueber denselben Index nicht.
     * Das Bundle wird zum Finden von Texturen gebraucht.
     * 2.5.19: Spaetestens seit Flat Shading ist es noch nicht sicher, wie das Material konkret angelegt werden muss. Darum
     * die Matlist nur mit MaterialDefinitionen anlegen
     */
    private static List<PortableMaterial/*NativeMaterial*/> buildMatlist(PortableModel pml, PortableModelDefinition obj, /*MaterialPool matpool,*/ ResourcePath texturebasepath) {
        List<PortableMaterial> matlist = new ArrayList<PortableMaterial>();
        int index = 0;
        for (String matname : new String[]{obj.material/*geolistmaterial*/}) {
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
     * 21.8.24: A dummy material is questionable and shouldn't be needed. So don't have effords for caching it.
     * Just register to missingMaterials.
     */
    private static NativeMaterial getDummyMaterial() {
        HashMap<ColorType, Color> color = new HashMap<ColorType, Color>();
        color.put(ColorType.MAIN, Color.WHITE);
        return (Platform.getInstance()).buildMaterial(null, color, null, null, null);
    }
}
