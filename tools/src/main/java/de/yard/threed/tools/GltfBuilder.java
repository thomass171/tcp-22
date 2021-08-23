package de.yard.threed.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.loader.*;
import de.yard.threed.core.Vector2Array;

import de.yard.threed.core.Vector3Array;
import de.yard.threed.core.Color;
import de.yard.threed.engine.platform.common.SimpleGeometry;
import de.yard.threed.javacommon.JAOutputStream;

import java.io.ByteArrayOutputStream;

/**
 * Baut das gltf analog zum Blender export, z.B. was die Nomenklatur angeht.
 */
public class GltfBuilder {
    // hier auch ueber Platform loggen?
    Log logger = Platform.getInstance().getLog(GltfBuilder.class);
    private JsonArray accessors, bufferViews, nodes, meshes, materials, scenes, textures, images, samplers;
    private boolean isfirstobject = true;
    private int objindex = 0;
    private JAOutputStream binarystream;
    private ByteArrayOutputStream bos;
    private PortableModelList ppfile;
    private boolean usematlib = false;

    public GltfBuilder() {

    }

    public GltfBuilder(boolean usematlib) {
        this.usematlib = usematlib;
    }

    /**
     * Bekommt die Resource statt pp, um selber den Loader zu ermitteln. Das koennte man auch mal auslagern.
     * 28.12.17: Jetzt hat er seinen eigenen findLoaderBySuffix und koennte generell ohne Bundle auf Filesystemebene arbeiten.
     *
     * @param file
     * @return
     */
    public GltfBuilderResult process(String/*BundleResource*/ file) throws InvalidDataException {
        //try {
        // AC world wird ignoriert.
        ppfile = GltfProcessor.findLoaderBySuffix(/*file, file.bundle.getResource(file)*/file, true, usematlib);

        if (ppfile == null) {
            // Fehler. Ist bereits gelogged.
            return null;
        }
        return process(ppfile);
    }

    public GltfBuilderResult process(PortableModelList ppfile) {
        this.ppfile = ppfile;
        JsonObject gltf = new JsonObject();
        accessors = new JsonArray();
        gltf.add("accessors", accessors);
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
        gltf.add("textures", textures);
        images = new JsonArray();
        gltf.add("images", images);
        samplers = new JsonArray();
        gltf.add("samplers", samplers);
        bos = new ByteArrayOutputStream();
        binarystream = new JAOutputStream(bos);
        buildScene();

        for (PortableMaterial mat : ppfile.materials) {
            processMaterial(mat);
        }
        // ob es eine AC world gibt oder nicht, spielt hier keine Rolle. Hier wird das ppfile konvertiert, egal was/wie drin ist.
        for (PortableModelDefinition obj : ppfile.objects) {
            processObject(obj);
        }

        Gson gson = new Gson();
        gson = new GsonBuilder().setPrettyPrinting().create();
        String result = gson.toJson(gltf);
        //System.out.println(result);
        return new GltfBuilderResult(result, bos.toByteArray());
    }

    private void processMaterial(PortableMaterial mat) {
        JsonObject material = new JsonObject();
        boolean unshaded = false;

        if (mat.name != null) {
            material.add("name", new JsonPrimitive(mat.name));
            if (mat.name.startsWith("unshaded")) {
                //fuer AC
                unshaded = true;
            }
        }
        if (mat.emis != null) {
            JsonArray emissiveFactor = buildColorArray(mat.emis, false);
            material.add("emissiveFactor", emissiveFactor);
        }
        JsonObject pbrMetallicRoughness = new JsonObject();
        if (mat.color != null) {
            JsonArray baseColorFactor = buildColorArray(mat.color, true);
            pbrMetallicRoughness.add("baseColorFactor", baseColorFactor);
        }
        if (mat.shininess != null) {
            // assume this to be metallic
            pbrMetallicRoughness.add("metallicFactor", new JsonPrimitive(mat.shininess.value));
        }

        if (mat.texture != null) {

            JsonObject image = new JsonObject();
            image.add("uri", new JsonPrimitive(mat.texture));
            images.add(image);

            JsonObject sampler = new JsonObject();
            if (mat.wraps) {
                sampler.add("wrapS", new JsonPrimitive(LoaderGLTF.REPEAT));
            }
            if (mat.wrapt) {
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
            extensions.add("KHR_materials_unlit", ea);
            material.add("extensions", extensions);
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
     * GLTF kennt wohl nur ein Mesh pro Object? Aber in einem Mesh kann es mehrere Primitives geben.
     * <p>
     * Returns node index.
     */
    private int processObject(PortableModelDefinition obj) {
        int meshindex = -1;
        if (obj.geolist.size() > 0) {
            JsonObject mesh = new JsonObject();
            if (obj.name != null) {
                mesh.add("name", new JsonPrimitive(obj.name));
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


        ((JsonObject) scenes.get(0)).getAsJsonArray("nodes").add(new JsonPrimitive(objindex));
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
     * Baut auch BufferView.
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
                itemsize = 4;
                break;
            case 2:
                componentType = new JsonPrimitive(5126);
                type = new JsonPrimitive("VEC2");
                itemsize = 4;
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
        accessors.add(accessor);
        return accessors.size() - 1;
    }

    private JsonArray buildPrimitives(PortableModelDefinition obj) {
        JsonArray primitives = new JsonArray();

        for (int i = 0; i < obj.geolist.size(); i++) {
            //Nicht jede geo hat immer ein Material, z.B bei Genie. Dann wird es ein MEsh ohne Material ->wireframe.
            String mat = null;
            if (obj.geolistmaterial.size() > i) {
                mat = obj.geolistmaterial.get(i);
            }
            JsonObject primitive = buildPrimitive(obj, obj.geolist.get(i), mat);
            primitives.add(primitive);
        }
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
        if (lnormals!=null) {
            attributes.add("NORMAL", new JsonPrimitive(buildAccessor(cnt, 0)));
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
    }

    /*public NativeByteBuffer getBin() {
        return new sgSimpleBuffer(bos.toByteArray());
    }*/
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