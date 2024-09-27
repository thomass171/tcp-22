package de.yard.threed.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.loader.PortableModelDefinition;
import de.yard.threed.core.loader.PortableModel;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.Vector2Array;

import de.yard.threed.core.Vector3Array;
import de.yard.threed.core.Color;
import de.yard.threed.core.geometry.SimpleGeometry;
import de.yard.threed.javacommon.JAOutputStream;

import java.io.ByteArrayOutputStream;

/**
 * Build a gltf like blender export does, eg. using similar namings.
 */
public class GltfBuilder {
    // use Platform for logging? platform might bee needed for libs.
    Log logger = Platform.getInstance().getLog(GltfBuilder.class);
    private JsonArray accessors, bufferViews, nodes, meshes, materials, scenes, textures, images, samplers, extensionsUsed;
    private boolean isfirstobject = true;
    private int objindex = 0;
    // The byte stream resulting in the 'bin' file.
    private JAOutputStream binarystream;
    private ByteArrayOutputStream bos;
    private PortableModel ppfile;

    public GltfBuilder() {

    }

    public GltfBuilderResult process(PortableModel ppfile) {
        this.ppfile = ppfile;
        JsonObject gltf = new JsonObject();
        accessors = new JsonArray();
        gltf.add("accessors", accessors);

        JsonObject asset = new JsonObject();
        asset.add("version", new JsonPrimitive("2.0"));
        asset.add("generator", new JsonPrimitive("GltfBuilder.java"));
        gltf.add("asset", asset);

        bufferViews = new JsonArray();
        gltf.add("bufferViews", bufferViews);
        meshes = new JsonArray();
        gltf.add("meshes", meshes);
        nodes = new JsonArray();
        gltf.add("nodes", nodes);
        materials = new JsonArray();
        gltf.add("materials", materials);
        scenes = new JsonArray();
        gltf.add("scenes", scenes);
        textures = new JsonArray();
        images = new JsonArray();
        samplers = new JsonArray();
        extensionsUsed = new JsonArray();

        bos = new ByteArrayOutputStream();
        binarystream = new JAOutputStream(bos);
        buildScene();

        for (PortableMaterial mat : ppfile.materials) {
            processMaterial(mat);
        }
        // ob es eine AC world gibt oder nicht, spielt hier keine Rolle. Hier wird das ppfile konvertiert, egal was/wie drin ist.
        //for (PortableModelDefinition obj : ppfile.objects) {
        for (int i = 0; i < 1/*ppfile.getObjectCount()*/; i++) {
            processObject(ppfile.getRoot()/*Object(i)*/);
        }

        // Add not before now to GLTF as empty entities are not allowed
        if (textures.size() > 0) {
            gltf.add("textures", textures);
        }
        if (images.size() > 0) {
            gltf.add("images", images);
        }
        if (samplers.size() > 0) {
            gltf.add("samplers", samplers);
        }
        if (extensionsUsed.size() > 0) {
            gltf.add("extensionsUsed", extensionsUsed);
        }


        byte[] binContent = bos.toByteArray();
        // add buffers at end when bin file is complete.
        gltf.add("buffers", buildBuffers(binContent.length, ppfile.getName()));

        Gson gson = new Gson();
        gson = new GsonBuilder().setPrettyPrinting().create();
        String result = gson.toJson(gltf);
        //System.out.println(result);
        return new GltfBuilderResult(result, binContent);
    }

    /**
     * See https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html (eg. material 'gold' example)
     * GLTF has eg.
     * - 'baseColor' (beyond 'pbrMetallicRoughness')
     * - 'emissive' (not! beyond 'pbrMetallicRoughness')
     * GLTF often adds the phrase "Factor".
     * And for textures the phrase 'Texture' is added.
     * <p>
     * According to BoxTextured.gltf a simple textured material uses pbrMetallicRoughness.baseColorTexture
     * with metallicFactor 0.0.
     */
    private void processMaterial(PortableMaterial mat) {
        JsonObject material = new JsonObject();
        boolean unshaded = false;

        if (mat.getName() != null) {
            material.add("name", new JsonPrimitive(mat.getName()));
            if (mat.getName().startsWith("unshaded")) {
                //fuer AC
                unshaded = true;
            }
        }
        boolean hadTransparency = false;
        if (mat.getTransparency() != null) {
            // not possible to set the value. That must be derived from some alpha, eg. from color
            material.add("alphaMode", new JsonPrimitive("BLEND"));
            hadTransparency = true;
        }

        if (mat.getEmis() != null) {
            JsonArray emissiveFactor = buildColorArray(mat.getEmis(), false);
            material.add("emissiveFactor", emissiveFactor);
        }
        JsonObject pbrMetallicRoughness = new JsonObject();
        if (mat.getColor() != null) {
            Color c = mat.getColor();
            if (hadTransparency) {
                c = c.transparency((int) ((1.0f - mat.getTransparency().floatValue()) * 255));
            }
            JsonArray baseColorFactor = buildColorArray(c, true);
            pbrMetallicRoughness.add("baseColorFactor", baseColorFactor);
        }
        if (mat.getShininess() != null) {
            // assume this to be metallic
            pbrMetallicRoughness.add("metallicFactor", new JsonPrimitive(mat.getShininess().value));
        }

        if (mat.getTexture() != null) {

            JsonObject image = new JsonObject();
            image.add("uri", new JsonPrimitive(mat.getTexture()));
            images.add(image);

            JsonObject sampler = new JsonObject();
            if (mat.getWraps()) {
                sampler.add("wrapS", new JsonPrimitive(LoaderGLTF.REPEAT));
            }
            if (mat.getWrapt()) {
                sampler.add("wrapT", new JsonPrimitive(LoaderGLTF.REPEAT));
            }
            samplers.add(sampler);

            JsonObject texture = new JsonObject();
            texture.add("source", new JsonPrimitive(images.size() - 1));
            texture.add("sampler", new JsonPrimitive(samplers.size() - 1));
            textures.add(texture);


            JsonObject baseColorTexture = new JsonObject();
            baseColorTexture.add("index", new JsonPrimitive(textures.size() - 1));
            // texCoord isType the key suffix in key in attributes "TEXCOORD_.."
            baseColorTexture.add("texCoord", new JsonPrimitive(0));
            pbrMetallicRoughness.add("baseColorTexture", baseColorTexture);
        }

        material.add("pbrMetallicRoughness", pbrMetallicRoughness);

        if (unshaded) {
            JsonObject extensions = new JsonObject();
            JsonObject ea = new JsonObject();
            // See https://www.khronos.org/assets/uploads/developers/presentations/gltf20-reference-guide.pdf
            // for valid extensions
            extensions.add("KHR_materials_unlit", ea);
            material.add("extensions", extensions);
            if (!extensionsUsed.contains(new JsonPrimitive("KHR_materials_unlit"))) {
                extensionsUsed.add(new JsonPrimitive("KHR_materials_unlit"));
            }
        }
        materials.add(material);

    }

    private JsonArray buildColorArray(Color color, boolean withalpha) {
        JsonArray a = new JsonArray();
        a.add(color.getR());
        a.add(color.getG());
        a.add(color.getB());
        if (withalpha) {
            a.add(color.getAlpha());
        }
        return a;
    }

    /**
     * meshes is an array of primitives array. So probably there can be multiple meshes. We currently only consider one.
     * <p>
     * Returns node index.
     */
    private int processObject(PortableModelDefinition obj) {
        int meshindex = -1;
        if (obj.geo != null/*obj.geolist.size() > 0*/) {
            JsonObject mesh = new JsonObject();
            if (obj.name != null) {
                //1.8.24 meshes shouldn't have a name
                //mesh.add("name", new JsonPrimitive(obj.name));
            }
            mesh.add("primitives", buildPrimitives(obj));
            meshes.add(mesh);
            meshindex = meshes.size() - 1;
        }

        JsonObject node = new JsonObject();
        if (obj.name != null) {
            node.add("name", new JsonPrimitive(obj.name));
        }
        if (obj.translation != null) {
            JsonArray translation = new JsonArray();
            translation.add(new JsonPrimitive(obj.translation.getX()));
            translation.add(new JsonPrimitive(obj.translation.getY()));
            translation.add(new JsonPrimitive(obj.translation.getZ()));
            node.add("translation", translation);
        }
        if (obj.rotation != null) {
            JsonArray rotation = new JsonArray();
            rotation.add(new JsonPrimitive(obj.rotation.getX()));
            rotation.add(new JsonPrimitive(obj.rotation.getY()));
            rotation.add(new JsonPrimitive(obj.rotation.getZ()));
            rotation.add(new JsonPrimitive(obj.rotation.getW()));
            node.add("rotation", rotation);
        }
        if (meshindex != -1) {
            node.add("mesh", new JsonPrimitive(meshindex));
        }
        nodes.add(node);
        int nodeindex = nodes.size() - 1;

        isfirstobject = false;
        objindex++;

        JsonArray children = new JsonArray();
        for (PortableModelDefinition o : obj.kids) {
            int ni = processObject(o);
            children.add(new JsonPrimitive(ni));
        }
        if (children.size() > 0) {
            node.add("children", children);
        }
        return nodeindex;
    }

    /**
     * Also builds BufferView.
     * 5121 = uint8
     * 5123 = uint16
     * 5125 = uint32
     * 5126 = float32;
     *
     * @param cnt
     */
    private int buildAccessor(int cnt, int buffertype) {
        JsonObject accessor = new JsonObject();
        JsonPrimitive componentType, type;
        // itemsize in bytes
        int itemsize;
        switch (buffertype) {
            /*case 0:
                componentType = new JsonPrimitive(5121);
                type = new JsonPrimitive("SCALAR");
                break;*/
            case 1:
                //indices
                componentType = new JsonPrimitive(5125);
                type = new JsonPrimitive("SCALAR");
                itemsize = 4;
                break;
            /*case 0:
                componentType = new JsonPrimitive(5126);
                type = new JsonPrimitive("VEC4");
                break;*/
            case 0:
                // vertices,normals
                componentType = new JsonPrimitive(5126);
                type = new JsonPrimitive("VEC3");
                itemsize = 3/*vectors*/ * 4/*bytes*/;
                break;
            case 2:
                componentType = new JsonPrimitive(5126);
                type = new JsonPrimitive("VEC2");
                itemsize = 2/*vectors*/ * 4/*bytes*/;
                break;
            default:
                throw new RuntimeException("unknown buffertype");
        }


        JsonObject bufferView = new JsonObject();
        bufferView.add("buffer", new JsonPrimitive(0));
        bufferView.add("byteLength", new JsonPrimitive(cnt * itemsize));
        bufferView.add("byteOffset", new JsonPrimitive(binarystream.size()));
        //bufferView.add("target",null);
        bufferViews.add(bufferView);

        accessor.add("bufferView", new JsonPrimitive(bufferViews.size() - 1));
        //accessor.add("byteOffset",null);
        accessor.add("componentType", componentType);
        accessor.add("type", type);
        accessor.add("count", new JsonPrimitive(cnt));
        //??accessor.add("min", buildMin());
        accessors.add(accessor);
        return accessors.size() - 1;
    }

    /**
     * 27.7.24: We no longer have multple primitives.
     * We had these for "geolist" once.
     */
    private JsonArray buildPrimitives(PortableModelDefinition obj) {
        JsonArray primitives = new JsonArray();

        //for (int i = 0; i < obj.geolist.size(); i++) {
        //Nicht jede geo hat immer ein Material, z.B bei Genie. Dann wird es ein MEsh ohne Material ->wireframe.
        String mat = null;
        if (obj.material != null/*geolistmaterial.size() > i*/) {
            mat = obj.material;//geolistmaterial.get(i);
        }
        JsonObject primitive = buildPrimitive(obj, obj.geo/*list.get(i)*/, mat);
        primitives.add(primitive);
        //}
        return primitives;
    }

    private JsonObject buildPrimitive(PortableModelDefinition obj, SimpleGeometry geo, String materialname) {

        int cnt = geo.getVertices().size();
        Vector3Array lvertices = geo.getVertices();
        Vector3Array lnormals = geo.getNormals();

        JsonObject primitive = new JsonObject();
        JsonObject attributes = new JsonObject();
        attributes.add("POSITION", new JsonPrimitive(buildAccessor(cnt, 0)));
        for (int i = 0; i < cnt; i++) {
            Vector3 v = lvertices.getElement(i);
            binarystream.writeFloat((float) v.getX());
            binarystream.writeFloat((float) v.getY());
            binarystream.writeFloat((float) v.getZ());
        }
        //2.5.19: Normale sind nicht mandatory
        if (lnormals != null) {
            attributes.add("NORMAL", new JsonPrimitive(buildAccessor(cnt, 0)));
            if (lnormals.size() != cnt) {
                throw new RuntimeException("normals mismatch. Expected " + cnt + " but found " + lnormals.size());
            }
            for (int i = 0; i < cnt; i++) {
                Vector3 v = lnormals.getElement(i);
                binarystream.writeFloat((float) v.getX());
                binarystream.writeFloat((float) v.getY());
                binarystream.writeFloat((float) v.getZ());
            }
        }

        Vector2Array uvs = geo.getUvs();
        attributes.add("TEXCOORD_0", new JsonPrimitive(buildAccessor(uvs.size(), 2)));
        for (int i = 0; i < uvs.size(); i++) {
            binarystream.writeFloat((float) uvs.getElement(i).getX());
            binarystream.writeFloat((float) uvs.getElement(i).getY());
        }
        //attributes.add("TANGENT", new JsonPrimitive(0));
        primitive.add("attributes", attributes);

        int[] indices = geo.getIndices();
        primitive.add("indices", new JsonPrimitive(buildAccessor(indices.length, 1)));
        for (int i = 0; i < indices.length; i++) {
            binarystream.writeInt(indices[i]);
        }

        if (materialname != null) {
            int matindex = ppfile.findMaterialIndex(materialname);
            if (matindex == -1) {
                // material not know. Might eg. be a BTG landclass
                primitive.add("material", new JsonPrimitive(materialname));
            } else {
                primitive.add("material", new JsonPrimitive(matindex));
            }
        }

        return primitive;
    }


    private void buildScene() {
        JsonObject scene = new JsonObject();
        scene.add("name", new JsonPrimitive("Scene"));
        scene.add("nodes", new JsonArray());
        scenes.add(scene);

        // 4.9.24: Only node 0 (the root node) should be listed in the scene. All other are just children of the root node,
        ((JsonObject) scenes.get(0)).getAsJsonArray("nodes").add(new JsonPrimitive(0));
    }

    /*public NativeByteBuffer getBin() {
        return new sgSimpleBuffer(bos.toByteArray());
    }*/

    /**
     * We only use one
     */
    private JsonArray buildBuffers(int length, String source) {
        String binName = source;
        if (StringUtils.contains(binName, "/")) {
            binName = StringUtils.substringAfterLast(source, "/");
        }
        if (StringUtils.contains(binName, ".")) {
            binName = StringUtils.substringBeforeLast(binName, ".");
        }
        binName += ".bin";
        JsonArray buffers = new JsonArray();
        JsonObject buffer = new JsonObject();
        buffer.add("byteLength", new JsonPrimitive(length));
        buffer.add("uri", new JsonPrimitive(binName));
        buffers.add(buffer);
        return buffers;
    }

    /**
     * The spec says "Animation input and vertex position attribute accessors MUST have accessor.min and accessor.max defined. For all other accessors, these properties are optional."
     * So its mandatory for position vertices. TODO implement when we know how
     */
    private JsonArray buildMin() {
        JsonArray mins = new JsonArray();
        //mins.add("byteLength", new JsonPrimitive(length));
        return mins;
    }
}

/*
class GltfAccessor {
    JsonObject accessor;
    String type;
    int componentType, count;
    Vector3Array vec3array;
    NativeVector2Array vec2array;
    int[] intarray;

    GltfAccessor(NativeJsonObject accessor) {
        this.accessor = accessor;
        type = accessor.get("type").isString().stringValue();
        componentType = accessor.get("componentType").isNumber().intValue();
        count = accessor.get("count").isNumber().intValue();
    }

    GltfBufferView getBufferView() {
        int index = accessor.get("bufferView").isNumber().intValue();
        return new GltfBufferView(LoaderGLTF.gltf.get("bufferViews").isArray().get(index).isObject());
    }

    int getItemsize() {
        if (type.equals("SCALAR")) return 1;
        if (type.equals("VEC2")) return 2;
        if (type.equals("VEC3")) return 3;
        throw new RuntimeException("unsupported type");
    }

    void fillArray() {
        GltfBufferView bufferview = getBufferView();
        int bytestride;
        ;
        switch (componentType) {
            case 5121:
                intarray = new int[count];
                bytestride = 1;
                for (int i = 0; i < count; i++) {
                    int ival = LoaderGLTF.binbuffer.readUByte(bufferview.byteOffset + i * bytestride);
                    intarray[i] = ival;
                }
                break;
            case 5123:
                intarray = new int[count];
                bytestride = 2;
                for (int i = 0; i < count; i++) {
                    int ival = LoaderGLTF.binbuffer.readUShort(bufferview.byteOffset + i * bytestride);
                    intarray[i] = ival;
                }
                break;
            case 5126:
                switch (getItemsize()) {
                    case 3:
                        vec3array = Platform.getInstance().buildVector3Array(count);
                        bytestride = 12;
                        for (int i = 0; i < count; i++) {
                            float f0 = LoaderGLTF.binbuffer.readFloat(bufferview.byteOffset + i * bytestride);
                            float f1 = LoaderGLTF.binbuffer.readFloat(bufferview.byteOffset + i * bytestride + 4);
                            float f2 = LoaderGLTF.binbuffer.readFloat(bufferview.byteOffset + i * bytestride + 8);
                            vec3array.setElement(i, f0, f1, f2);
                        }
                        break;
                    case 2:
                        vec2array = Platform.getInstance().buildVector2Array(count);
                        bytestride = 8;
                        for (int i = 0; i < count; i++) {
                            float f0 = LoaderGLTF.binbuffer.readFloat(bufferview.byteOffset + i * bytestride);
                            float f1 = LoaderGLTF.binbuffer.readFloat(bufferview.byteOffset + i * bytestride + 4);
                            vec2array.setElement(i, f0, f1);
                        }
                        break;
                    default:
                        throw new RuntimeException("unsupported itemsize " + getItemsize());
                }
                break;
            default:
                // eg: 5125: Uint32Array
                throw new RuntimeException("unsupported componentType " + componentType);
        }
    }
}
*/