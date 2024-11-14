package de.yard.threed.engine.loader;

import de.yard.threed.core.Color;
import de.yard.threed.core.ParsingHelper;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.loader.AsciiLoader;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCamera;
import de.yard.threed.core.platform.NativeJsonArray;
import de.yard.threed.core.platform.NativeJsonObject;
import de.yard.threed.core.platform.NativeJsonString;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.geometry.Primitives;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.engine.PerspectiveCamera;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;

import java.util.List;

/**
 * AsciiLoader just because the origin is ASCII. Needs a JSON Parser and lineno likely cannot be used.
 * <p>
 * Created by thschonh on 08.12.17.
 */
public class SceneLoader extends AsciiLoader {

    static Log logger = Platform.getInstance().getLog(SceneLoader.class);
    private NativeJsonObject top;
    String source;
    int materialCount = 0;
    PortableModel ploadedfile = null;//30.12.18new PortableModelList();


    /**
     * Read a scene description. Just gets the json, independent from bundle reading.
     */
    public SceneLoader(String json, String source) throws InvalidDataException {
        this.source = source;

        NativeJsonValue content = Platform.getInstance().parseJson(json);
        if (content == null) {
            throw new InvalidDataException("parsing json failed:" + json);
        }
        top = content.isObject();
        load();
    }

    @Override
    protected void doload() throws InvalidDataException {
        NativeJsonArray objects = (top.get("objects") != null) ? top.get("objects").isArray() : null;

        PortableModelDefinition sceneroot = new PortableModelDefinition();

        PortableModel/*List*/ ppfile = new PortableModel(sceneroot,null);
        ploadedfile = ppfile;
        ppfile.setName(source);
        //4.1.17: hier mal keine Exception fangen, weil das schon der Modelloader macht. Andererseits passt es hier aber gut hin. Hmm
        try {
            //int nodecnt = gltfo.get("nodes").isArray().size();
            for (int i = 0; i < objects.size(); i++) {
                addObject(objects.get(i).isObject()/*.materials*/, ppfile);
            }

        } catch (java.lang.Exception e) {
            //Alle Exceptions catchen, weil auch mal Array/Null etc. vorkommen koenen. 
            String msg = "reading scene failed from " + source + ":" + ((e.getMessage() != null) ? e.getMessage() : e.toString());
            if (e instanceof InvalidDataException) {
                // die ist von uns (z.B. kein bin), daher kein Stacktrace.
                logger.error(msg);
            } else {
                logger.error(msg, e);
            }
            throw new InvalidDataException(msg, e);
        }

    }

    /**
     * Loader loaded preprocessed. Nothing to do here.
     */
    @Override
    public PortableModel/*List*/ buildPortableModel() {
        return ploadedfile;
    }

    @Override
    protected Log getLog() {
        return logger;
    }

    private void addObject(NativeJsonObject obj,/* List<PortableMaterial> materials,*/ PortableModel/*List*/ ppfile) throws InvalidDataException {
        //PortableModelDefinition pmd = new PortableModelDefinition();


        PortableMaterial material = buildMaterial(obj.get("material").isString());
        ppfile.addMaterial(material);
        PortableModelDefinition pmd = buildGeometry(obj.get("geometry").isString(), material.getName());
        NativeJsonValue v = obj.get("name");
        if (v != null) {
            pmd.name = v.isString().stringValue();
        }

        v = obj.get("position");
        if (v != null) {
            pmd.translation = ParsingHelper.getVector3(v.isString().stringValue());
        }
        //TODO rotation
        v = obj.get("scale");
        if (v != null) {
            pmd.scale = ParsingHelper.getVector3(v.isString().stringValue());
        }

        v = obj.get("parent");
        if (v != null) {
            pmd.parent = v.isString().stringValue();
            ppfile.getRoot().addModel(pmd/*, v.isString().stringValue()*/);
        } else {
            ppfile.getRoot().addModel(pmd);
        }
    }

    private PortableModelDefinition buildGeometry(NativeJsonString jgeometry, String materialname) throws InvalidDataException {
        String geometry = StringUtils.replaceAll(jgeometry.stringValue(), " ", "");
        if (!geometry.equals("primitive:box")) {
            throw new InvalidDataException("unsupported geometry:" + geometry);
        }
        SimpleGeometry geo = Primitives.buildBox(1.0, 1.0, 1.0);
        return new PortableModelDefinition(geo, materialname);
    }

    private PortableMaterial buildMaterial(NativeJsonString jmaterial) throws InvalidDataException {
        String material = StringUtils.replaceAll(jmaterial.stringValue(), " ", "");
        if (!material.equals("color:red")) {
            throw new InvalidDataException("unsupported material:" + material);
        }
        String materialname = "material" + materialCount++;
        PortableMaterial pm = new PortableMaterial(materialname, Color.RED);
        return pm;
    }
}
