package de.yard.threed.tools;

import de.yard.threed.core.FloatHolder;
import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;

import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.loader.BinaryLoader;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.PortableMaterial;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.loader.*;
import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.Color;
import de.yard.threed.core.platform.Config;
import de.yard.threed.core.Util;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.StringUtils;

import java.util.ArrayList;

/**
 * Binary, so not the preferred option (but GLTF bin is also binary)? Thus not in engine. Models (shuttle,genie) are large. Better GLTF Convert?
 * Better tools than sandbox.
 *
 * Created by thomass on 12.02.16.
 */
public class Loader3DS extends BinaryLoader {
    Log logger = Platform.getInstance().getLog(Loader3DS.class);
    private boolean headerchecked = false;
    //byte[] inbuf;
    ByteArrayInputStream buf;

    public Loader3DS(ByteArrayInputStream buf /*InputStream ins*/) throws InvalidDataException {

        //inbuf = ins.readFully();
        this.buf = buf;
        load();

        // close the file
        //TODO gzclose(fp);
        //TODO return true;
    }

    @Override
    protected void doload() throws InvalidDataException {
        //sgSimpleBuffer buf = new sgSimpleBuffer(inbuf);

        readChunklist(buf, null, "");

    }

    @Override
    protected Log getLog() {
        return logger;
    }

    void readChunklist(ByteArrayInputStream buf, Object3DS p_object, String level) throws InvalidDataException {
        PortableMaterial material = null;
        FloatHolder floatexpector = null;
        ImportMap map = null;

        while (buf.remaining() > 0) {
            int chunkid = buf.readUShort();
            int chunklength = buf.readUInt();

            if (Config.loaderdebuglog)
                logger.debug(level + "ChunkID: " + Util.format("0x%x", chunkid) + ", total length=" + chunklength+",buf.remaining="+buf.remaining()+",buf.size="+buf.getSize());
            if (!headerchecked && chunkid != 0x4D4D) {
                //TODO    fp.close();
                throw new InvalidDataException("Bad 3DS magic/version");
            }
            headerchecked = true;

            switch (chunkid) {
                case 0x0002: //3DS-Version
                    buf.skip(chunklength - 6);
                    break;
                case 0x0030: // int percent
                    int ipercent = buf.readUShort();
                    if (floatexpector == null) {
                        throw new RuntimeException("unexpected percent");
                    }
                    floatexpector.value = ipercent;
                    floatexpector = null;
                    break;
                case 0x0031: // float percent
                    float fpercent = buf.readFloat();
                    if (floatexpector == null) {
                        throw new RuntimeException("unexpected percent");
                    }
                    floatexpector.value = fpercent;
                    floatexpector = null;
                    break;
                case 0x1200: //Background color
                    buf.skip(chunklength - 6);
                    break;
                case 0x1201: //Use background color
                    buf.skip(chunklength - 6);
                    break;
                case 0x2100: //Ambient color
                    buf.skip(chunklength - 6);
                    break;
                case 0x4D4D:
                    break;
                case 0x3D3D:
                    //----------------- EDIT3DS -----------------
                    // Description: 3D Editor chunk, objects layout info

                    //  sgSimpleBuffer buf =  sgReadBytes(fp, chunklength-6);
                    break;
                //--------------- EDIT_OBJECT ---------------
                // Description: Object block, info for each object
                case 0x3D3E: //  Editor configuration main block;
                    buf.skip(chunklength - 6);
                    break;
                case 0x4000:
                    //Object can be a Camera a Light or a mesh
                    if (p_object != null) {
                        throw new RuntimeException("recursive objects");
                    }
                    Object3DS new_object = new Object3DS();
                    loadedfile.objects.add(new_object);
                    // name varying
                    new_object.name = buf.readString();
                    if (Config.loaderdebuglog)
                        logger.debug("new object " + new_object.name);
                    readChunklist(buf.readSubbuffer(chunklength - 6 - (StringUtils.length(new_object.name) + 1)), new_object, level + "  ");
                    //System.out.println("p_object.name: " + p_object.name);
                    break;

                //--------------- OBJ_TRIMESH ---------------
                // Description: Triangular mesh, contains chunks for 3d mesh info
                // Chunk ID: 4100 (hex)
                // Chunk Lenght: 0 + sub chunks
                //-------------------------------------------
                case 0x4100:
                    //TODO: was ist, wenn es mehrere meshes gibt? Unklar, ob es das gibt
                    if (p_object == null) {
                        throw new RuntimeException("mesh outside of object");
                    }
                    readChunklist(buf.readSubbuffer(chunklength - 6), p_object, level + "  ");
                    break;
                //--------------- TRI_VERTEXL ---------------
                // Description: Vertices list
                // Chunk ID: 4110 (hex)
                // Chunk Lenght: 1 x unsigned short (number of vertices)
                //             + 3 x float (vertex coordinates) x (number of vertices)
                //             + sub chunks
                //-------------------------------------------
                case 0x4110:
                    int l_qty = buf.readUShort();
                    if (Config.loaderdebuglog)
                        logger.debug(level + "Number of vertices:" + l_qty);
                    for (int i = 0; i < l_qty; i++) {
                        p_object.vertices.add(new Vector3(buf.readFloat(), buf.readFloat(), buf.readFloat()));
                        if (Config.loaderdebuglog && i <10){
                            Vector3 v = p_object.vertices.get(i);
                            logger.debug("vertex "+i+":"+v.getX()+" "+v.getY()+" "+v.getZ());
                        }
                        // printf("Vertices list z: %f\n",p_object->vertex[i].z);
                    }
                    break;

                //--------------- TRI_FACEL1 ----------------
                // Description: Polygons (faces) list
                // Chunk ID: 4120 (hex)
                // Chunk Lenght: 1 x unsigned short (number of polygons)
                //             + 3 x unsigned short (polygon points) x (number of polygons)
                //             + sub chunks
                //Das sind bei 3DS immer Face3
                case 0x4120:
                    //sgSimpleBuffer buf4120 = sgReadBytes(fp, chunklength - 6);
                    l_qty = buf.readUShort();
                    // p_object->polygons_qty = l_qty;
                    if (Config.loaderdebuglog)
                        logger.debug(level + "Number of polygons:" + l_qty);
                    for (int i = 0; i < l_qty; i++) {
                        int a = buf.readUShort();
                        // printf("Polygon point a: %d\n",p_object->polygon[i].a);
                        int b = buf.readUShort();
                        //printf("Polygon point b: %d\n",p_object->polygon[i].b);
                        int c = buf.readUShort();
                        //printf("Polygon point c: %d\n",p_object->polygon[i].c);
                        int faceflags = buf.readUShort();
                        p_object.addTmpFace(a, b, c);
                        // l_face_flags = ConvertEndianus(l_face_flags);
                        //printf("Face flags: %x\n",l_face_flags);
                    }
                    break;
                case 0x4130: //Face Material Chunk
                    String matname = buf.readString();
                    l_qty = buf.readUShort();
                    if (Config.loaderdebuglog)
                        logger.debug(level + "Number of faces for material " + matname + ": " + l_qty);
                    // Die Faces werden in die Faceliste gehangen, deren Index dem Materialindex entspricht.
                    // Nee, das geht nicht, denn es kann mehrere Objekte geben, die nur Teile der Materialien verwenden. Das ist dann nicht fortlaufend.
                    //Darum parallele Liste
                    //LoadedMaterial mat = findMaterial(name);
                    p_object.addFacelistMaterial(matname);
                    FaceList facelist = p_object.addFacelist();
                    for (int i = 0; i < l_qty; i++) {
                        int faceindex = buf.readUShort();
                        Face3 face = (Face3) p_object.tmpfaces.get(faceindex);
                        if (p_object.texcoords != null) {
                            face.setUV(p_object.texcoords.get(face.index0), p_object.texcoords.get(face.index1), p_object.texcoords.get(face.index2));
                        }
                        facelist.faces.add(face);
                    }
                    break;
                //------------- TRI_MAPPINGCOORS ------------
                // Description: Vertices list
                // Chunk ID: 4140 (hex)
                // Chunk Lenght: 1 x unsigned short (number of mapping points)
                //             + 2 x float (mapping coordinates) x (number of mapping points)
                //             + sub chunks
                //-------------------------------------------
                case 0x4140:
                    //sgSimpleBuffer buf4140 = sgReadBytes(fp, chunklength - 6);
                    l_qty = buf.readUShort();
                    if (Config.loaderdebuglog)
                        logger.debug(level + "Number of uvs: " + l_qty);
                    p_object.texcoords = new ArrayList<Vector2>();
                    for (int i = 0; i < l_qty; i++) {
                        float u = buf.readFloat();
                        //p_object->mapcoord[i].u = ConvertEndianf(p_object->mapcoord[i].u);
                        //printf("Mapping list u: %f\n",p_object->mapcoord[i].u);
                        float v = buf.readFloat();
                        // p_object->mapcoord[i].v = ConvertEndianf(p_object->mapcoord[i].v);
                        // System.out.println(level+"Mapping list v: " + v);
                        p_object.texcoords.add(new Vector2(u, v));
                    }
                    break;
                case 0x4150: //Face Smoothing Group (nfaces*4bytes)
                    // hieraus muessen die Normalen berechnet werden (Durchschnitt aller Facenormals, die sich im Vertex mit einer
                    // Smoothing group id treffen.
                    // Bei Shuttle passt das len=77638 -6/4 = 19408 =  19408 polygons
                    // D.h. pro Face ist hier die Id der Smoothing Group. Die Groupid ist ueber ein gesetztes Bit markiert. Damit kann
                    // es maximal 32 Smoothing Groups pro model geben.
                    // Die 32 ist auch hier erwähnt: https://knowledge.autodesk.com/support/3ds-max/learn-explore/caas/CloudHelp/cloudhelp/2016/ENU/3DSMax/files/GUID-1244162D-A063-486C-BD9B-168466F6488B-htm.html
                    l_qty = p_object.tmpfaces.size();
                    for (int i = 0; i < l_qty; i++) {
                        int smoothinggroup = buf.readUInt();
                        //logger.loaderdebuglog("smoothing group "+i+":smoothinggroup="+smoothinggroup);
                    }
                    break;
                case 0x4160: //Local coordinate system
                    //X1, X2 and X3 represent the axes, O the origin
                    for (int i = 0; i < 2; i++) {
                        float x1 = buf.readFloat();
                        float x2 = buf.readFloat();
                        float x3 = buf.readFloat();
                        float o1 = buf.readFloat();
                        float o2 = buf.readFloat();
                        float o3 = buf.readFloat();
                    }
                    //TODO
                    break;
                case 0xAFFF:
                    // Material Block
                    /*if (materials != null){
                        System.err.println("duplicate material block");
                    }*/
                    // materials = new ArrayList<LoadedMaterial>();
                    material = new PortableMaterial();
                    loadedfile.materials.add(material);
                    break;
                case 0xA000: // Material Name (varying)
                    //sgSimpleBuffer bufA000 = sgReadBytes(fp, chunklength - 6);
                    material.name = buf.readString();
                    if (Config.loaderdebuglog)
                        logger.debug(level + "material.name: " + material.name);
                    break;
                case 0xA010: // Ambient Color
                    //Ist das die Grundfarbe? Kann eigentlich nicht, denn AC hat ambient und rgb
                    material.ambient = readColor(buf, chunklength);
                    break;
                case 0xA020: // Diffuse Color
                    //Ist das wirklich die Grundfarbe?
                    material.color = readColor(buf, chunklength);
                    break;
                case 0xA030: // Specular Color
                    material.specular = readColor(buf, chunklength);
                    break;
                case 0xA040: //Shininess percent
                    material.shininess = new FloatHolder(0);
                    floatexpector = material.shininess;
                    break;
                case 0xA041: //Shininess strength percent. 22.1.18: ignored
                    //material.shininessstrengthpercent = new FloatHolder(0);
                    //floatexpector = material.shininessstrengthpercent;
                    floatexpector = new FloatHolder(0);
                    break;
                case 0xA050: //Transparency percent
                    material.transparencypercent = new FloatHolder(0);
                    floatexpector = material.transparencypercent;
                    break;
                case 0xA100: //Render type (Bedeutung unklar)
                    buf.skip(chunklength - 6);
                    break;
                    /*?     ?? 0xA200 // Texture Map 1
                ?     ?? 0xA230 // Bump Map*/
                case 0xA200: //Texture map 1
                    //readMapChunk(buf);
                    map = new ImportMap();
                    map.percent = new FloatHolder(0);
                    floatexpector = map.percent;
                    break;
                case 0xA230: //Bump map
                    buf.skip(chunklength - 6);
                    break;
                case 0xA300: //filename
                    map.filename = buf.readString();
                    if (Config.loaderdebuglog)
                        logger.debug("map filename: " + map.filename);
                    break;
                case 0xA351: //Mapping parameters
                    //TODO ??
                    int ii = buf.readUShort();
                    break;
                case 0xA354: //V scale
                    map.vscale = buf.readFloat();
                    break;
                case 0xA356: //U scale
                    map.uscale = buf.readFloat();
                    break;
                case 0xA358: //U Offset
                    map.uoffset = buf.readFloat();
                    break;
                case 0xA35A: //V Offset
                    map.voffset = buf.readFloat();
                    break;
                case 0xB000: // Keyframer chunk
                    buf.skip(chunklength - 6);
                    break;
                default:
                    logger.warn("unknown chunkid " + Util.format("0x%x", chunkid));
                    buf.skip(chunklength - 6);
                    // Mal als Fehler betrachten, damit es nicht untergeht
                    throw new InvalidDataException("unknown chunkid " + Util.format("0x%x", chunkid));
            }
        }
    }



    private Color readColor(ByteArrayInputStream buf, int colorlen) throws InvalidDataException {
        int colortype = buf.readUShort();
        int len = buf.readUInt();
        if (Config.loaderdebuglog)
            logger.debug("colortype=" + colortype + ",len=" + len);
        // colortype 16=RGB (3 float)
        // colortype 17=rgb 24bit color (3 Byte)+gamma corrected value
        //colortype = 16;
        switch (colortype) {
            case 16:
                Color c = new Color(buf.readFloat(), buf.readFloat(), buf.readFloat(), 1);
                return c;
            case 17:
                c = new Color(buf.readByte(), buf.readByte(), buf.readByte(), 255);
                if (colorlen - 6 > 9) {
                    // das kommt dann noch: Rgb color gamma corrected (byte format)
                    colortype = buf.readUShort();
                    len = buf.readUInt();
                    if (Config.loaderdebuglog)
                        logger.debug("colortype=" + colortype + ",len=" + len);
                    c = new Color(buf.readByte(), buf.readByte(), buf.readByte(), 255);
                }
                return c;
            default:
                throw new InvalidDataException("unknown color type " + colortype);
        }
    }
}
