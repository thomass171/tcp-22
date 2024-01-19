package de.yard.threed.core.loader;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.FloatHolder;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.Vector2Array;
import de.yard.threed.core.Vector3Array;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.Color;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AsciiLoader just because the origin is ASCII. Needs a JSON Parser and lineno likely cannot be used.
 * Used inside platforms that have no own GLTF reader or from an app that cannot use an platform internal
 * loader, eg. because external material definition is used (eg. FG scenery).
 * The 'bin' file is split into Vector3Array and int[].
 * <p>
 * Created by thschonh on 08.12.17.
 */
public class LoaderGLTF extends AsciiLoader {

    static Log logger = Platform.getInstance().getLog(LoaderGLTF.class);
    private NativeJsonObject gltfo;
    private NativeByteBuffer binbuffer;
    ResourcePath texturebasepath;
    private PortableModelList ppfile;
    // TODO boese static Kruecke wegen Zugriff aus Unterklassen in C#
    static NativeJsonArray textures, images, samplers;
    String source;
    boolean flipy = false;
    //
    // 
    // 
    // BundleResource file;
    public static final int REPEAT = 10497;

    /**
     * Read a GLTF. json and 'bin' are parameter to be independent from bundle loading.
     * Makes using easier, eg. in WebGL
     * and with async delayed loaded Bundle data.
     */
    public LoaderGLTF(String json, NativeByteBuffer binbuffer, ResourcePath texturebasepath, String source) throws InvalidDataException {
        this.binbuffer = binbuffer;
        this.source = source;

        NativeJsonValue gltf = Platform.getInstance().parseJson(json);
        if (gltf == null) {
            throw new InvalidDataException("parsing json failed:" + json);
        }
        gltfo = gltf.isObject();
        this.texturebasepath = texturebasepath;
        if (binbuffer == null) {
            // 6.3.21: Das macht doch keinen Sinn, oder?
            logger.warn("no bin. Intended?");
        }
        load();
    }

    /**
     * Helper, um den Loader fuer eine BundleResource zu bauen.
     */
    public static LoaderGLTF buildLoader(BundleResource file, ResourcePath texturebasepath) throws InvalidDataException {
        BundleData bd = file.bundle.getResource(file);
        if (bd == null) {
            throw new InvalidDataException(file.getName() + " not found in bundle " + file);
        }
        BundleResource binres = LoaderGLTF.getBinResource(file);
        NativeByteBuffer binbuffer = null;
        if (file.bundle.exists(binres)) {
            binbuffer = binres.bundle.getResource(binres).b;
        }
        String s;
        try {
            s = bd.getContentAsString();
        } catch (CharsetException e) {
            throw new InvalidDataException("CharsetException not found in bundle " + file);
        }
        return new LoaderGLTF(s, binbuffer, texturebasepath, file.getFullName());
    }

    @Override
    protected void doload() throws InvalidDataException {
        textures = (gltfo.get("textures") != null) ? gltfo.get("textures").isArray() : null;
        images = (gltfo.get("images") != null) ? gltfo.get("images").isArray() : null;
        samplers = (gltfo.get("samplers") != null) ? gltfo.get("samplers").isArray() : null;
        NativeJsonObject asset = (gltfo.get("asset") != null) ? gltfo.get("asset").isObject() : null;
        if (asset != null) {
            String generator = asset.getString("generator");
            //Zum Thema Flipy gibts eine aktulle Diskussion bei GLTF, z.B. https://github.com/KhronosGroup/glTF-WebGL-PBR/issues/16
            //das ist wohl noch nicht rund. Der WebGL Loader hat damit aber wohl kein Problem, obwohl er anscheinend KEIN flip macht. 
            if (StringUtils.startsWith(generator, "Khronos Blender")) {
                flipy = true;
            }
        }

        ppfile = new PortableModelList(texturebasepath);
        ploadedfile = ppfile;
        //ppfile.root = new PreprocessedLoadedObject();
        //ppfile.root.geolist = new ArrayList<SimpleGeometry>();
        //ppfile.name = file.getBasename();
        ppfile.setName(source);
        //4.1.17: hier mal keine Exception fangen, weil das schon der Modelloader macht. Andererseits passt es hier aber gut hin. Hmm
        try {
            int cnt;
            if (gltfo.get("materials") != null) {
                NativeJsonArray materials = gltfo.get("materials").isArray();
                cnt = gltfo.get("materials").isArray().size();
                for (int i = 0; i < cnt; i++) {
                    GltfMaterial gltfmaterial = new GltfMaterial(materials.get(i).isObject());
                    ppfile.materials.add(buildMaterial(gltfmaterial, i));
                }
            }
            // alle Nodes erst seqeuntiell lesen und danach die children rausdröseln.
            NativeJsonArray nodes = gltfo.get("nodes").isArray();
            List<PortableModelDefinition> nodelist = new ArrayList<PortableModelDefinition>();
            int nodecnt = gltfo.get("nodes").isArray().size();
            for (int i = 0; i < nodecnt; i++) {
                nodelist.add(loadObject(nodes.get(i).isObject(), ppfile.materials));
            }
            //23.3.18:Manchmal gabs hier NPE, z.B. SceneryViewer :
            //ERROR:ModelLoader loader threw InvalidDataException error in line 1: : for file Objects/e000n50/e007n50/moffett-hangar-n-211.gltf
            //passiert auch in c172p.gltf
            //5.4.18: passiert, wenn children aus schon umgehangenen umgehangen werden. Da ist der Eintrag in nodelist schon null. Darum die umgehangenen merken
            Map<Integer, PortableModelDefinition> moved = new HashMap<Integer, PortableModelDefinition>();
            if (Config.loaderdebuglog) {
                logger.debug("organizing children of " + nodecnt + " nodes");
            }
            for (int i = 0; i < nodecnt; i++) {
                GltfNode gltfnode = new GltfNode(nodes.get(i).isObject());
                int[] children = gltfnode.getChildren();
                if (Config.loaderdebuglog) {
                    logger.debug("found " + children.length + " children at node index " + i);
                }
                for (int j = 0; j < children.length; j++) {
                    int childindex = children[j];
                    if (Config.loaderdebuglog) {
                        logger.debug("moving childindex " + childindex + " to index " + i + " and setting childindex to null");
                    }
                    PortableModelDefinition target = nodelist.get(i);
                    PortableModelDefinition child = nodelist.get(childindex);
                    if (target == null) {
                        // parent node no longer in nodelist. Moved before.
                        target = moved.get(i);
                        if (Config.loaderdebuglog) {
                            logger.debug("target node already moved at node " + i + ". target now=" + target);
                        }
                        if (target == null) {
                            int h = 9;
                        }
                    } /*27.12.18 else*/
                    moved.put(childindex, child);

                    target.kids.add(child);
                    nodelist.set(childindex, null);
                }
            }
            // wer nicht als Child untergebracht wurde, kommt in die main list.
            for (int i = 0; i < nodecnt; i++) {
                PortableModelDefinition n = nodelist.get(i);
                if (n != null) {
                    ppfile.addModel(n);
                }
            }
        } catch (java.lang.Exception e) {
            //Alle Exceptions catchen, weil auch mal Array/Null etc. vorkommen koenen. 
            String msg = "reading GLTF failed from " + source + ":" + ((e.getMessage() != null) ? e.getMessage() : e.toString());
            if (e instanceof InvalidDataException) {
                // die ist von uns (z.B. kein bin), daher kein Stacktrace.
                logger.error(msg);
            } else {
                logger.error(msg, e);
            }
            throw new InvalidDataException(msg, e);
        }
        ppfile = ppfile;
    }

    /**
     * Der Loader laedt schon quasi preprocessed. Da ist nichts mehr zu tun.
     *
     * @return
     */
    @Override
    public PortableModelList preProcess() {
        return ploadedfile;
    }

    @Override
    protected Log getLog() {
        return logger;
    }

    public PortableModelList getPortableModelList() {
        return ppfile;
    }

    private PortableModelDefinition loadObject(NativeJsonObject node, List<PortableMaterial> materials) throws InvalidDataException {
        PortableModelDefinition lo = new PortableModelDefinition();
        GltfNode gltfnode = new GltfNode(node);
        lo.name = node.getString("name");
        lo.translation = gltfnode.getTranslation();//3.5.19  != null) ? gltfnode.getTranslation() : new Vector3(0, 0, 0);
        lo.rotation = gltfnode.getRoation();
        if (Config.loaderdebuglog) {
            logger.debug("Loading object from gltf: " + lo.name + "(" + lo.name + ")");
        }
        // meshes gibt es wohl maximal eins. Es kann dann aber mehrere primitives enthalten.
        GltfMesh mesh = gltfnode.getMesh(gltfo);
        if (mesh != null) {
            int geocnt = mesh.primitives.size();

            if (Config.loaderdebuglog) {
                logger.debug("Loading mesh from gltf: primitives.size=" + geocnt);
            }
            lo.geolist = new ArrayList<SimpleGeometry>();
            for (int k = 0; k < geocnt; k++) {
                GltfAccessor accessor = mesh.getPrimitive(k, "POSITION", gltfo, binbuffer, false);
                if (accessor.vec3array == null) {
                    throw new RuntimeException("no vertices");
                }
                Vector3Array lvertices = accessor.vec3array;
                if (Config.loaderdebuglog) {
                    logger.debug("found " + lvertices.size() + " vertices in mesh " + k);
                }
                //3.1.19: normals are be optional. These are not expected to be part of a GLTF model.
                accessor = mesh.getPrimitive(k, "NORMAL", gltfo, binbuffer, false);
                Vector3Array lnormals = null;
                if (accessor != null) {
                    if (accessor.vec3array != null) {
                        lnormals = accessor.vec3array;
                        //das ist Quatsch, weil nicht allgemeingültig. Bestenfalls zur analyse. logger.warn("overwriting normals with default");
                    /*for (int ii=0;ii<lnormals.size();ii++){
                        lnormals.setElement(ii,0,0,1);
                    }*/
                        if (Config.loaderdebuglog) {
                            logger.debug("found " + lnormals.size() + " normals in mesh " + k);
                        }
                    /*for (int ii=0;ii<lnormals.size();ii++){
                        logger.debug("normal: "+lnormals.getElement(ii));
                    }*/
                    }
                }
                accessor = mesh.getPrimitive(k, "indices", gltfo, binbuffer, false);
                if (accessor.intarray == null) {
                    throw new RuntimeException("no indices");
                }
                int[] indices = accessor.intarray;
                if (Config.loaderdebuglog) {
                    logger.debug("found " + indices.length + " indices in mesh " + k);
                }
                accessor = mesh.getPrimitive(k, "TEXCOORD_0", gltfo, binbuffer, flipy);
                Vector2Array uvs = null;
                if (accessor.vec2array != null) {
                    uvs = accessor.vec2array;
                    if (Config.loaderdebuglog) {
                        logger.debug("found " + uvs.size() + " uvs in mesh " + k);
                    }
                }
                lo.geolist.add(new SimpleGeometry(lvertices, indices, uvs, lnormals));
                // Fuer GLTF wird das facelistmaterial als mesh->material map verwendet. Als Name einfach der Index
                // der name könnte auch mal null sein, z.B. bei genie. 22.1.18: NeeNee, material name ist der echte
                String materialname = mesh.getMaterialName(k, materials);
                lo.geolistmaterial.add(materialname);
            }

        }
        return lo;
    }

    private PortableMaterial buildMaterial(GltfMaterial gltfmaterial, int index) {
        PortableMaterial lm = new PortableMaterial();
        lm.color = gltfmaterial.color;
        lm.emis = gltfmaterial.emis;
        // der Name des Materials ist eigentlich uninteressant. Den Index eintragen, denn darüber wird es
        //referenziert. 22.1.18: Der Name wird aber fuer (un)shaded gebraucht. Ist auch konsistenter. Dann muss per Index referenziert werden.
        lm.name = gltfmaterial.name;
        //lm.name = "" + index;
        lm.texture = gltfmaterial.texname;
        lm.wraps = gltfmaterial.getWrap("S") == REPEAT;
        lm.wrapt = gltfmaterial.getWrap("T") == REPEAT;
        if (gltfmaterial.shininess != null) {
            lm.shininess = new FloatHolder((float) gltfmaterial.shininess);
        }
        //Eine Kruecke, die nur bei AC geht. 3.1.19: Aber besser umgekehrte Logik, um shaded als default nicht unintended abzuschalten
        //2.5.19: Umgestellt auf die passende GLTF Extension.
        //if (StringUtils.startsWith(lm.name, "unshaded")) {
        lm.shaded = gltfmaterial.shaded;
        return lm;

    }

    /**
     * Mal in der Annahme dass die Endung "bin" ist.
     *
     * @param gltfresource
     * @return
     */
    public static BundleResource getBinResource(BundleResource gltfresource) {
        return new BundleResource(gltfresource.bundle, gltfresource.getPath(), gltfresource.getBasename() + ".bin");
    }
}

class GltfNode {
    NativeJsonObject node;

    GltfNode(NativeJsonObject node) {
        this.node = node;
    }


    GltfMesh getMesh(NativeJsonObject gltfo) {
        NativeJsonValue mesh = node.get("mesh");
        if (mesh == null) {
            return null;
        }
        int index = ((NativeJsonNumber) mesh.isNumber()).intValue();
        NativeJsonArray meshes = gltfo.get("meshes").isArray();
        return new GltfMesh(meshes.get(index).isObject());
    }

    public int[] getChildren() {
        NativeJsonValue children = node.get("children");
        if (children == null) {
            return new int[]{};
        }
        NativeJsonArray a = children.isArray();
        int[] c = new int[a.size()];
        for (int i = 0; i < c.length; i++) {
            c[i] = a.get(i).isNumber().intValue();
        }
        return c;
    }

    public Vector3 getTranslation() {
        NativeJsonValue n = node.get("translation");
        if (n == null) {
            return null;
        }
        NativeJsonArray a = n.isArray();
        if (a == null) {
            return null;
        }
        Vector3 t = new Vector3(a.get(0).isNumber().doubleValue(), a.get(1).isNumber().doubleValue(), a.get(2).isNumber().doubleValue());
        return t;
    }

    public Quaternion getRoation() {
        NativeJsonValue n = node.get("rotation");
        if (n == null) {
            return null;
        }
        NativeJsonArray a = n.isArray();
        if (a == null) {
            return null;
        }
        return new Quaternion(a.get(0).isNumber().doubleValue(), a.get(1).isNumber().doubleValue(), a.get(2).isNumber().doubleValue(), a.get(3).isNumber().doubleValue());
    }
}

class GltfMesh {
    NativeJsonObject node;
    NativeJsonArray primitives;

    GltfMesh(NativeJsonObject node) {
        this.node = node;
        primitives = node.get("primitives").isArray();
    }

    String getName() {
        return "";
    }

    GltfAccessor getAccessor(int index, NativeJsonObject gltfo) throws InvalidDataException {
        // int index = ((NativeJsonNumber) node.get("mesh").isNumber()).intValue();
        if (gltfo.get("accessors") == null) {
            throw new InvalidDataException("no accessors found");
        }
        NativeJsonArray accessors = gltfo.get("accessors").isArray();
        return new GltfAccessor(accessors.get(index).isObject());
    }

    GltfAccessor getPrimitive(int pos, String tag, NativeJsonObject gltfo, NativeByteBuffer binbuffer, boolean flipy) throws InvalidDataException {
        NativeJsonObject primitive = primitives.get(pos).isObject();
        int index = 0;
        if (tag.equals("indices")) {
            index = primitive.get(tag).isNumber().intValue();
        } else {
            NativeJsonObject attributes = primitive.get("attributes").isObject();
            if (tag.equals("NORMAL") && attributes.get(tag) == null) {
                //no normal found
                return null;
            }
            index = attributes.get(tag).isNumber().intValue();
        }
        //logger.debug("loading accessor " + index + " for tag " + tag);
        GltfAccessor accessor = getAccessor(index, gltfo);
        GltfBufferView gltfbufferview = accessor.getBufferView(gltfo);
        accessor.fillArray(gltfo, binbuffer, flipy);
        return accessor;
    }

    /**
     * Might be an external material name (eg. land class)
     *
     * @param pos
     * @param materials
     * @return
     */
    public String getMaterialName(int pos, List<PortableMaterial> materials) {
        NativeJsonObject primitive = primitives.get(pos).isObject();
        if (primitive.get("material") == null) {
            //dann gibt es halt kein Material->wireframe. 23.4.19: Scheint eher schwarz zu werden
            return null;
        }
        // might be a material index into material list or an external material name (eg. land class) 
        NativeJsonNumber n = primitive.get("material").isNumber();
        if (n != null) {
            int matindex = n.intValue();
            PortableMaterial mat = materials.get(matindex);
            return mat.name;
        }
        String index = primitive.getString("material");
        return "" + index;
    }
}

class GltfAccessor {
    Log logger = Platform.getInstance().getLog(GltfAccessor.class);
    NativeJsonObject accessor;
    String type;
    int componentType, count;
    /*Vector3Array*/ Vector3Array vec3array;
    /*NativeVector2Array*/ Vector2Array vec2array;
    int[] intarray;

    GltfAccessor(NativeJsonObject accessor) {
        this.accessor = accessor;
        type = accessor.get("type").isString().stringValue();
        componentType = accessor.get("componentType").isNumber().intValue();
        count = accessor.get("count").isNumber().intValue();
    }

    GltfBufferView getBufferView(NativeJsonObject gltfo) {
        int index = accessor.get("bufferView").isNumber().intValue();
        return new GltfBufferView(gltfo.get("bufferViews").isArray().get(index).isObject());
    }

    int getItemsize() {
        if (type.equals("SCALAR")) return 1;
        if (type.equals("VEC2")) return 2;
        if (type.equals("VEC3")) return 3;
        throw new RuntimeException("unsupported type");
    }

    void fillArray(NativeJsonObject gltfo, NativeByteBuffer binbuffer, boolean flipy) throws InvalidDataException {
        GltfBufferView bufferview = getBufferView(gltfo);
        if (bufferview == null) {
            throw new InvalidDataException("bufferview isType null");
        }
        if (binbuffer == null) {
            throw new InvalidDataException("no bin: binbuffer isType null");
        }
        int bytestride;

        switch (componentType) {
            case 5121:
                intarray = new int[count];
                bytestride = 1;
                for (int i = 0; i < count; i++) {
                    int ival = binbuffer.readUByte(bufferview.byteOffset + i * bytestride);
                    intarray[i] = ival;
                }
                break;
            case 5123:
                intarray = new int[count];
                bytestride = 2;
                for (int i = 0; i < count; i++) {
                    int ival = binbuffer.readUShort(bufferview.byteOffset + i * bytestride);
                    intarray[i] = ival;
                }
                break;
            case 5125:
                //uint32
                intarray = new int[count];
                bytestride = 4;
                for (int i = 0; i < count; i++) {
                    int ival = binbuffer.readUInt(bufferview.byteOffset + i * bytestride);
                    intarray[i] = ival;
                }
                break;
            case 5126:
                switch (getItemsize()) {
                    case 3:
                        bytestride = 12;

                        vec3array = new Vector3Array(binbuffer, bufferview.byteOffset, count);
                        break;
                    case 2:
                        //vec2array = Platform.getInstance().buildVector2Array(count);
                        vec2array = new Vector2Array(binbuffer, bufferview.byteOffset, count);

                        bytestride = 8;
                        if (flipy) {
                            // Dann muessen die Werte ueberschrieben werden. TODO: kann man das optimieren?
                            for (int i = 0; i < count; i++) {
                                float f0 = binbuffer.readFloat(bufferview.byteOffset + i * bytestride);
                                float f1 = binbuffer.readFloat(bufferview.byteOffset + i * bytestride + 4);
                            /*if (f0 < 0 || f0 > 1f) {
                                logger.debug("f0=" + f0);
                            }
                            if (f1 < 0 || f1 > 1f) {
                                logger.debug("f1=" + f1);
                                f1=1;
                            }*/
                                //logger.debug("uv " +i+"=" + f0 +"," +f1);
                                vec2array.setElement(i, f0, (flipy) ? (1 - f1) : f1);
                            }
                        }
                        break;
                    default:
                        throw new RuntimeException("unsupported itemsize " + getItemsize());
                }
                break;
            default:
                throw new InvalidDataException("unsupported componentType " + componentType);
        }
    }
}

class GltfBufferView {
    NativeJsonObject bufferview;
    int byteLength, byteOffset, target;

    GltfBufferView(NativeJsonObject bufferview) {
        this.bufferview = bufferview;
        byteLength = bufferview.get("byteLength").isNumber().intValue();
        byteOffset = bufferview.get("byteOffset").isNumber().intValue();
        //??target = bufferview.get("target").isNumber().intValue();

    }

}

class GltfMaterial {
    NativeJsonObject material;
    int byteLength, byteOffset, target;
    String name;
    //Mal keinen Defaultwert fuer Color.Das ist verfälschend.
    Color color = null;//Color.WHITE;
    Color emis = null;
    String texname = null;
    // mal annehmen, dass es nur einen Sampler pro material gibt.
    NativeJsonObject sampler = null;
    Float shininess = null;
    boolean shaded = true;

    GltfMaterial(NativeJsonObject material) {
        this.material = material;
        name = material.getString("name");

        //25.4.19: Was ist das eigentlich fuer ein merkwürdiger Name: "pbrMetallicRoughness"?
        if (material.get("pbrMetallicRoughness") != null) {
            NativeJsonObject pbrMetallicRoughness = material.get("pbrMetallicRoughness").isObject();
            if (pbrMetallicRoughness.get("baseColorFactor") != null) {
                NativeJsonArray baseColorFactor = pbrMetallicRoughness.get("baseColorFactor").isArray();
                color = getJsonColor(baseColorFactor);
            }
            if (pbrMetallicRoughness.get("metallicFactor") != null) {
                float metallicFactor = (float) pbrMetallicRoughness.get("metallicFactor").isNumber().doubleValue();
                shininess = metallicFactor;
            }
            NativeJsonObject baseColorTexture = (pbrMetallicRoughness.get("baseColorTexture") != null) ? pbrMetallicRoughness.get("baseColorTexture").isObject() : null;
            if (baseColorTexture != null) {
                int index = baseColorTexture.getInt("index");
                int texCoord = baseColorTexture.getInt("texCoord");
                if (index != -1) {
                    NativeJsonObject texture = LoaderGLTF.textures.get(index).isObject();
                    int source = texture.getInt("source");
                    int samplerindex = texture.getInt("sampler");
                    NativeJsonObject image = LoaderGLTF.images.get(source).isObject();
                    texname = image.getString("uri");
                    if (samplerindex != -1) {
                        sampler = LoaderGLTF.samplers.get(samplerindex).isObject();
                    }
                    ;
                }
            }
            //TODO metallicFactor
        } else {
            throw new RuntimeException("unsupported material");
        }
        if (material.get("emissiveFactor") != null) {
            NativeJsonArray emissiveFactor = material.get("emissiveFactor").isArray();
            emis = getJsonColor(emissiveFactor);
        }
        if (material.get("extensions") != null) {
            NativeJsonObject extensions = material.get("extensions").isObject();
            if (extensions.get("KHR_materials_unlit") != null) {
                shaded = false;
            }
        }
    }

    private Color getJsonColor(NativeJsonArray arr) {
        float r = (float) arr.get(0).isNumber().doubleValue();
        float g = (float) arr.get(1).isNumber().doubleValue();
        float b = (float) arr.get(2).isNumber().doubleValue();
        Color color;
        if (arr.size() > 3) {
            float a = (float) arr.get(3).isNumber().doubleValue();
            color = new Color(r, g, b, a);
        } else {
            color = new Color(r, g, b);
        }
        return color;
    }

    public int getWrap(String suffix) {
        if (sampler == null) {
            return 0;
        }
        return sampler.getInt("wrap" + suffix);
    }
}
