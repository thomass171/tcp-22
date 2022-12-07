package de.yard.threed.engine.loader;

import de.yard.threed.core.Color;
import de.yard.threed.core.ColorType;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeGeometry;
import de.yard.threed.core.platform.NativeMaterial;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.GenericGeometry;
import de.yard.threed.engine.Material;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.engine.platform.common.SimpleGeometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 06.12.22: A builder for universal model specification.
 *
 */
public class PortableModelBuilder {

    Log logger = Platform.getInstance().getLog(PortableModelBuilder.class);
    // Material, das verwendet wird, wenn das eigentlich definierte nicht bekannt ist
    private NativeMaterial dummyMaterial;

    //ResourcePath defaulttexturebasepath;
    PortableModelList pml;
    //Just an indicator for testing
    public boolean dummymaterialused;

    public PortableModelBuilder( PortableModelList pml) {
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
     * Bundle is needed in builder, eg. for loading textures.
     * <p>
     * alttexturepath might be null. Then texturebasepath is used.
     *
     * @return
     */
    public SceneNode buildModel(Bundle bundle) {
        return buildModel(bundle, null);
    }

    public SceneNode buildModel(Bundle bundle, ResourcePath alttexturepath) {
        SceneNode rootnode = new SceneNode();
        if (pml.getName() != null) {
            rootnode.setName(pml.getName());
        }
        for (int i = 0; i < pml.objects.size(); i++) {
            rootnode.attach(buildModel(bundle, pml.objects.get(i), alttexturepath));
        }
        return rootnode;
    }

    private SceneNode buildModel(Bundle bundle, /*ResourcePath rpath,*/ PortableModelDefinition obj, ResourcePath alttexturepath/*, boolean dummywegensignatureindeutigkeit*/) {
        //this.bundle = bundle;
        //this.rpath = rpath;
        ResourcePath nr = pml.defaulttexturebasepath;
        if (alttexturepath != null) {
            nr = alttexturepath;
        }
        return buildObject(bundle, obj /*, null/*, matlist*/, nr);
    }

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
    public SceneNode buildSceneNode(Bundle bundle, List<SimpleGeometry> geolist, List<PortableMaterial> material, boolean castShadow, boolean receiveShadow, ResourcePath texturebasepath/*3.5.19, String objtexture*/) {
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
            dummyMaterial = ( Platform.getInstance()).buildMaterial(null, color, null, null, null);
        }
        return dummyMaterial;
    }


}
