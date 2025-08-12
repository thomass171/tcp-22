package de.yard.threed.core.loader;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.FloatHolder;
import de.yard.threed.core.GeneralParameterHandler;
import de.yard.threed.core.ModelBuildDelegate;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.*;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.Vector2Array;
import de.yard.threed.core.Vector3Array;
import de.yard.threed.core.Color;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.resource.ResourceLoader;
import de.yard.threed.core.resource.ResourcePath;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.resource.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AsciiLoader just because the origin is ASCII. Needs a JSON Parser and lineno likely cannot be used.
 * Used inside platforms that have no own GLTF reader or from an app that cannot use an platform internal
 * loader, eg. because external material definition is used (eg. FG scenery).
 * Resides in core to be available in tools(?).
 * The 'bin' file is split into Vector3Array and int[].
 * 13.02.24: The name of the bin file might be part of the GLTF. So it might not be known until parsing. This leads to resource
 * fetching during parsing considering an original path, probably also for textures. And all this could be async.
 * So use a more flexible solution via ResourceLoader. It also means:
 * - no longer InvalidDataException is thrown
 * - no longer extend AsciiLoader
 * <p>
 * Created by thschonh on 08.12.17.
 */
public class LoaderGLTF {

    static Log logger = Platform.getInstance().getLog(LoaderGLTF.class);
    private NativeJsonObject gltfo;
    private NativeByteBuffer binbuffer;
    // 13.2.24: Again, just the path, eg.: "engine:cesiumbox"
    private ResourcePath texturebasepath;
    //private PortableModelList ppfile;
    // TODO boese static Kruecke wegen Zugriff aus Unterklassen in C#
    static NativeJsonArray textures, images, samplers;
    // source is important for setting node name
    private String source;
    boolean flipy = false;
    String json;
    //
    // 
    // 
    // BundleResource file;
    public static final int REPEAT = 10497;
    public static String GLTF_ROOT = "gltfroot";

    /**
     * Read a GLTF. json and 'bin' are parameter to be independent from bundle loading.
     * Textures are loaded async on the fly.
     * 24.8.24: Don't handle error here by (quite useless) exception handling here but inform delegate about failure.
     */
    public LoaderGLTF(String json, NativeByteBuffer binbuffer, ResourcePath texturebasepath, String source) /*throws InvalidDataException*/ {
        this.json = json;
        this.binbuffer = binbuffer;
        this.source = source;

        this.texturebasepath = texturebasepath;
        if (binbuffer == null) {
            // 6.3.21: Das macht doch keinen Sinn, oder?
            logger.warn("no bin. Intended?");
        }
    }

    /**
     * Helper, um den Loader fuer eine BundleResource zu bauen.
     * 14.2.24: Only for tools,
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

    /**
     * New async trigger point.
     * Only reads the model without building a 3D Object.
     * 24.8.24: Don't handle error here by (quite useless) exception handling here but inform delegate about failure.
     */
    public static void load(ResourceLoader resourceLoader, GeneralParameterHandler<PortableModel/*List*/> delegate) {
        logger.debug("Launching async gltf load of " + resourceLoader.nativeResource.getFullQualifiedName());
        resourceLoader.loadResource(new AsyncJobDelegate<AsyncHttpResponse>() {
            @Override
            public void completed(AsyncHttpResponse response) {
                String json;
                logger.debug("got gltf");
                // try {
                try {
                    json = response.getContentAsString();
                } catch (CharsetException e) {
                    logger.error(e.getMessage());
                    delegate.handle(null);
                    return;
                }
                //NativeJsonValue gltf = Platform.getInstance().parseJson(json);
                if (json == null) {
                    logger.warn("no gltf data for " + resourceLoader.nativeResource.getFullQualifiedName());
                    //throw new InvalidDataException("no gltf data for " + resourceLoader.nativeResource.getFullQualifiedName());
                    delegate.handle(null);
                }
                //gltfo = gltf.isObject();*/
                // TODO get bin file name from gltf
                //BundleResource binres = LoaderGLTF.getBinResource(file);
                logger.debug("Launching async bin load");
                ResourceLoader binLoader = resourceLoader.fromReference(LoaderGLTF.getBinResource(resourceLoader.getUrl()));
                binLoader.loadResource(new AsyncJobDelegate<AsyncHttpResponse>() {
                    @Override
                    public void completed(AsyncHttpResponse response) {
                        NativeByteBuffer binbuffer = response.getContent();
                        if (binbuffer != null) {
                            logger.debug("got bin with size " + binbuffer.getSize());
                        } else {
                            logger.error("no bin found from " + binLoader.getUrl());
                        }

                            /* if (file.bundle.exists(binres)) {
                                binbuffer = binres.bundle.getResource(binres).b;
                            }*/
                        try {

                            LoaderGLTF loaderGLTF = new LoaderGLTF(json, binbuffer, resourceLoader.getUrl().getPath(),
                                    // source is important for setting node name
                                    resourceLoader.nativeResource.getFullName());
                            // 24.8.24: ploadedfile might be null in case of error (already logged)
                            PortableModel/*List */ploadedfile = loaderGLTF.doload();
                            delegate.handle(ploadedfile);
                        } catch (InvalidDataException e) {
                            // problem was already logged
                            delegate.handle(null);
                        }
                    }
                });

                /*24.8.24} catch (Exception e) {
                    //TODO throw new InvalidDataException("CharsetException not found");
                    e.printStackTrace();
                }*/
            }
        });
    }

    public PortableModel/*List*/ doload() throws InvalidDataException {
        if (json == null) {
            //throw new InvalidDataException("parsing json failed:" + json);
            logger.warn("no json");
            return null;
        }

        NativeJsonValue gltf = Platform.getInstance().parseJson(json);
        if (gltf == null) {
            //throw new InvalidDataException("parsing json failed:" + json);
            logger.warn("parsing json failed:" + json);
            return null;
        }
        gltfo = gltf.isObject();
        if (gltfo == null) {
            //throw new InvalidDataException("parsing json failed to gltfo:" + json);
            logger.warn("parsing json failed to gltfo:" + json);
            return null;
        }

        //logger.debug("Start building");
        textures = (gltfo.get("textures") != null) ? gltfo.get("textures").isArray() : null;
        images = (gltfo.get("images") != null) ? gltfo.get("images").isArray() : null;
        samplers = (gltfo.get("samplers") != null) ? gltfo.get("samplers").isArray() : null;
        NativeJsonObject asset = (gltfo.get("asset") != null) ? gltfo.get("asset").isObject() : null;
        if (asset != null) {
            String generator = asset.getString("generator");
            if (generator != null) {
                //Zum Thema Flipy gibts eine aktulle Diskussion bei GLTF, z.B. https://github.com/KhronosGroup/glTF-WebGL-PBR/issues/16
                //das ist wohl noch nicht rund. Der WebGL Loader hat damit aber wohl kein Problem, obwohl er anscheinend KEIN flip macht.
                if (StringUtils.startsWith(generator, "Khronos Blender")) {
                    flipy = true;
                }
            }
        }

        // We cannot be sure that the GLTF file has a clear tree with exactly one root. So we add a synthetic here and
        // GLTF content will end in 'kids' (with hierarchy from file)
        PortableModelDefinition gltfroot = new PortableModelDefinition();
        gltfroot.setName(GLTF_ROOT);
        PortableModel ppfile = new PortableModel(gltfroot, texturebasepath);

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
                    ppfile.addMaterial(buildMaterial(gltfmaterial, i));
                }
            }
            // read all nodes as array and later derive parent/children relations
            NativeJsonArray nodes = gltfo.get("nodes").isArray();
            List<PortableModelDefinition> nodelist = new ArrayList<PortableModelDefinition>();
            int nodecnt = gltfo.get("nodes").isArray().size();
            for (int i = 0; i < nodecnt; i++) {
                nodelist.add(loadObject(nodes.get(i).isObject(), ppfile/*.materials*/));
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
                    // ppfile.addModel(n);
                    gltfroot.kids.add(n);
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
        logger.debug("GLTF completed");
        return ppfile;
    }

    /**
     * Load a single object without considering parent/children, which is done later.
     */
    private PortableModelDefinition loadObject(NativeJsonObject node, PortableModel ppfile/*List<PortableMaterial> materials*/) throws InvalidDataException {
        PortableModelDefinition lo = new PortableModelDefinition();
        GltfNode gltfnode = new GltfNode(node);
        lo.name = node.getString("name");
        lo.translation = gltfnode.getTranslation();//3.5.19  != null) ? gltfnode.getTranslation() : new Vector3(0, 0, 0);
        lo.rotation = gltfnode.getRoation();
        if (Config.loaderdebuglog) {
            logger.debug("Loading object from gltf: " + lo.name + "(" + lo.name + ")");
        }
        GltfMesh mesh = gltfnode.getMesh(gltfo);
        if (mesh != null) {
            int geocnt = mesh.primitives.size();
            if (Config.loaderdebuglog) {
                logger.debug("Loading mesh from gltf: primitives.size=" + geocnt);
            }
            // meshes might contain a primitives array. So probably there can be multiple primitives per meshes.
            // This was also used in our pre 2024 layout with 'geolist' in objects.
            // Fuer GLTF wird das facelistmaterial als mesh->material map verwendet. Als Name einfach der Index
            // der name könnte auch mal null sein, z.B. bei genie. 22.1.18: NeeNee, material name ist der echte
            if (geocnt > 1) {
                // occurs in traditional GLTFs where primitives were created for "geolist".
                // in this case every primitive should be a subnode. TODO only has unit tests in tcp-flightgear with old GLTFs.
                for (int k = 0; k < geocnt; k++) {
                    SimpleGeometry simpleGeometry = buildGeometryFromPrimitive(mesh, k);
                    //27.7.24 lo.geolist.add(new SimpleGeometry(lvertices, indices, uvs, lnormals));
                    String materialname = mesh.getMaterialNameOfPrimitive(k, ppfile/*materials*/);
                    //27.7.24lo.geolistmaterial.add(materialname);
                    lo.addChild(new PortableModelDefinition(simpleGeometry, materialname));
                }
            } else {
                //lo.geolist = new ArrayList<SimpleGeometry>();
                lo.geo = buildGeometryFromPrimitive(mesh, 0);
                //27.7.24 lo.geolist.add(new SimpleGeometry(lvertices, indices, uvs, lnormals));
                lo.material = mesh.getMaterialNameOfPrimitive(0, ppfile/*materials*/);
                //27.7.24lo.geolistmaterial.add(materialname);
            }
        }
        return lo;
    }

    /**
     * 27.7.24: Extracted from above
     * 12.8.24: "alphamode" added
     */
    private SimpleGeometry buildGeometryFromPrimitive(GltfMesh mesh, int k) throws InvalidDataException {
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
        return new SimpleGeometry(lvertices, indices, uvs, lnormals);
    }

    private PortableMaterial buildMaterial(GltfMaterial gltfmaterial, int index) {

        // transparency is a generic material property, not only for color.
        FloatHolder transparency = null;
        if ("BLEND".equalsIgnoreCase(gltfmaterial.alphaMode)) {
            if (gltfmaterial.color != null) {
                // transparency is inverse of alpha
                transparency = new FloatHolder(1 - gltfmaterial.color.getAlpha());
            } else if (gltfmaterial.emis != null) {
                // transparency is inverse of alpha
                transparency = new FloatHolder(1 - gltfmaterial.emis.getAlpha());
            } else {
                // No idea how to use BLEND is this case. See AlphaBlendModeTest.gltf.
                logger.warn("Using default transparency value for alphamode BLEND");
                transparency = new FloatHolder(0.6f);
            }
        }

        // keep it simple.
        PortableMaterial lm = null;
        // der Name des Materials ist eigentlich uninteressant. Den Index eintragen, denn darüber wird es
        //lm.name = "" + index;
        //referenziert. 22.1.18: Der Name wird aber fuer (un)shaded gebraucht. Ist auch konsistenter. Dann muss per Index referenziert werden.

        // texture has priority to be caompatible with pre2024 built GLTFs. And just because we
        // keep it simple. If we have a texture, it will provide the base color.
        if (gltfmaterial.texname == null) {
            if (transparency != null) {
                // is valueless until now. Need to retrieve value from alpha. But revert it!
                transparency.value = 1.0f - gltfmaterial.color.getAlpha();
            }
            lm = new PortableMaterial(gltfmaterial.name, gltfmaterial.color);

        } else {
            lm = new PortableMaterial(gltfmaterial.name, gltfmaterial.texname, gltfmaterial.getWrap("S") == REPEAT,
                    gltfmaterial.getWrap("T") == REPEAT);
        }
        lm.setTransparency(transparency);

        if (gltfmaterial.shininess != null) {
            lm.setShininess(new FloatHolder((float) gltfmaterial.shininess));
        }
        if (gltfmaterial.emis != null) {
            lm.setEmis(gltfmaterial.emis);
        }
        // Flag 'shaded' adopted from AC. For GLTF we use extension "KHR_materials_unlit". See also README.md for smooth/flat shading.
        lm.setShaded(gltfmaterial.shaded);

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

    /**
     * For now the convention is that both have the same base name.
     */
    public static String getBinResource(URL gltfresource) {
        return /*new URL*/(gltfresource.getBasename() + ".bin");
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
     * 13.11.24: No longer an external material name (eg. land class). We shouldn't rely on material having a name at all.
     * But if it exists, we assume it is unique.
     * Otherwise use index as name.
     *
     * @return material name if it has a name, otherwise the index
     */
    public String getMaterialNameOfPrimitive(int primitiveIndex, PortableModel ppfile/*List<PortableMaterial> materials*/) {
        NativeJsonObject primitive = primitives.get(primitiveIndex).isObject();
        NativeJsonValue primitiveMaterial = primitive.get("material");
        if (primitiveMaterial == null) {
            //no material defined for primitive -> wireframe or black or whatever.
            return null;
        }
        // No longer an external material name (eg. land class). Always an index and the land class might be the material name.
        NativeJsonNumber n = primitiveMaterial.isNumber();
        if (n != null) {
            int matindex = n.intValue();
            PortableMaterial mat = ppfile.getMaterialByIndex(matindex);
            if (mat.getName() == null) {
                return "" + matindex;
            }
            return mat.getName();
        }
        //TODO improve error handling
        throw new RuntimeException("mat is no number:" + primitiveMaterial);
        /*String index = primitive.getString("material");
        return "" + index;*/
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
    String name, alphaMode;
    //No default for Color?
    // 12.8.24: But according to description of 'factor' baseColorFactor seems to assume there is a default color (1,1,1)
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
        alphaMode = material.getString("alphaMode");

        //25.4.19: What a strange/specific name: "pbrMetallicRoughness"?
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
