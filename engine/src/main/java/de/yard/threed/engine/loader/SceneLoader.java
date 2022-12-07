package de.yard.threed.engine.loader;

import de.yard.threed.core.Color;
import de.yard.threed.core.ParsingHelper;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector2Array;
import de.yard.threed.core.Vector3Array;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeJsonArray;
import de.yard.threed.core.platform.NativeJsonObject;
import de.yard.threed.core.platform.NativeJsonString;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.engine.geometry.Primitives;
import de.yard.threed.engine.platform.common.SimpleGeometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        PortableModelList ppfile = new PortableModelList(null);
        ploadedfile = ppfile;
        ppfile.setName(source);
        //4.1.17: hier mal keine Exception fangen, weil das schon der Modelloader macht. Andererseits passt es hier aber gut hin. Hmm
        try {
            //int nodecnt = gltfo.get("nodes").isArray().size();
            for (int i = 0; i < objects.size(); i++) {
                addObject(objects.get(i).isObject(), ppfile.materials, ppfile);
            }

        } catch (Exception e) {
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
    public PortableModelList preProcess() {
        return ploadedfile;
    }

    @Override
    protected Log getLog() {
        return logger;
    }

    private void addObject(NativeJsonObject object, List<PortableMaterial> materials, PortableModelList ppfile) throws InvalidDataException {
        PortableModelDefinition pmd = new PortableModelDefinition();

        NativeJsonValue v = object.get("name");
        if (v != null) {
            pmd.name = v.isString().stringValue();
        }

        PortableMaterial material = buildMaterial(object.get("material").isString());
        materials.add(material);
        buildGeometry(object.get("geometry").isString(), pmd, material.getName());

        v = object.get("position");
        if (v != null) {
            pmd.translation = ParsingHelper.getVector3(v.isString().stringValue());
        }
        //TODO rotation
        v = object.get("scale");
        if (v != null) {
            pmd.scale = ParsingHelper.getVector3(v.isString().stringValue());
        }

        v = object.get("parent");
        if (v != null) {
            ppfile.addModel(pmd, v.isString().stringValue());
        } else {
            ppfile.addModel(pmd);
        }
    }

    private void buildGeometry(NativeJsonString jgeometry, PortableModelDefinition pmd, String materialname) throws InvalidDataException {
        String geometry = StringUtils.replaceAll(jgeometry.stringValue(), " ", "");
        if (!geometry.equals("primitive:box")) {
            throw new InvalidDataException("unsupported geometry:" + geometry);
        }
        SimpleGeometry geo = Primitives.buildBox(1.0, 1.0, 1.0);
        pmd.addGeoMat(geo, materialname);
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
