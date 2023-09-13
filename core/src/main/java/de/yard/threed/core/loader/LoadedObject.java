package de.yard.threed.core.loader;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.buffer.NativeOutputStream;
import de.yard.threed.core.Vector2Array;

import de.yard.threed.core.Vector3Array;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.geometry.Face3;
import de.yard.threed.core.geometry.CustomGeometry;
import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.geometry.SimpleGeometry;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by thomass on 12.02.16.
 */
public class LoadedObject extends CustomGeometry {
    Log logger = Platform.getInstance().getLog(LoadedObject.class);
    public String name;
    // Eine Facelist pro Material.
    // 11.12.17: Die werden doch nicht serialisiert? Also sind sie entbehrlich? Nach dem preprocess wohl, vorher werden sie verwendet.
    public List<FaceList> /*group_list*/ faces = new ArrayList<FaceList>();                // points vertex index
    public List<LoadedObject> kids = new ArrayList<LoadedObject>();
    // Die Materiallist darf nicht LoadedMaterial enthalten, weil nicht jedes Modell Material enthält (z.B. BTG und OBJ). Evtl.
    // stimmen die Namen auch nicht (3DS Shuttle). Darum hier im Loader nur den Materialnamen speichern. Der Aufrufer muss dann
    // sehen, wo er das Material herbekommt. Der Materialname in der Liste passt vom Index her zur Facelist Liste (siehe getFaceListByMaterialName()).
    public List<String> facelistmaterial = new ArrayList<String>();
    // Auf AC zugeschnitten, denn nur die(?) haben die Textur am Object.
    public String texture;
    // only ac (?) has a location?
    public Vector3 location = new Vector3(0, 0, 0);
    public Degree crease = null;
    public List<SimpleGeometry> geolist;

    public LoadedObject() {
        //faces.add(new ArrayList<Face>());
    }


  

  

    @Override
    public List<FaceList> getFaceLists() {
        return faces;
    }

    public Face3 addFace(int a, int b, int c) {
        Face3 face = new Face3(a, b, c);
        faces.get(0).faces.add(face);
        return face;
    }

    public FaceList addFacelist(/*int requiredindex*/) {
        /*if (requiredindex < faces.size()) {
            throw new InvalidDataException("unexpected index " + requiredindex);
        }
        while (faces.size() <= requiredindex) {*/
        FaceList list = new FaceList();
        faces.add(list);
        //}
        return list;//faces.get(requiredindex);
    }

    public void addFacelistMaterial(String mat) {
        facelistmaterial.add(mat);
    }

    public FaceList getFaceListByMaterialName(String materialname) {
        if (faces == null) {

        }

        int index = 0;
        for (String matname : facelistmaterial) {
            if (matname.equals(materialname)) {

                return faces.get(index);
            }
            index++;
        }
        return null;
    }

    public SimpleGeometry getGeometryByMaterialName(String materialname) {
        int index = 0;
        for (String matname : facelistmaterial) {
            if (matname.equals(materialname)) {

                return geolist.get(index);
            }
            index++;
        }
        return null;
    }


    public void serialize(NativeOutputStream outs) {
        outs.writeString((name == null) ? "" : name);

        outs.writeInt(geolist.size());
        for (SimpleGeometry geo : geolist) {
            Vector3Array lvertices = geo.getVertices();
            //Face3List facelist = geo.getFaces();
            Vector3Array lnormals = geo.getNormals();

            outs.writeInt(lvertices.size());
            for (int i = 0; i < lvertices.size(); i++) {
                Vector3 v = lvertices.getElement(i);
                outs.writeFloat((float)v.getX());
                outs.writeFloat((float)v.getY());
                outs.writeFloat((float)v.getZ());
            }
            outs.writeInt(lnormals.size());
            for (int i = 0; i < lnormals.size(); i++) {
                Vector3 v = lnormals.getElement(i);
                outs.writeFloat((float)v.getX());
                outs.writeFloat((float)v.getY());
                outs.writeFloat((float)v.getZ());
            }
            int[] indices = geo.getIndices();
            outs.writeInt(indices.length);
            for (int i = 0; i < indices.length; i++) {
                outs.writeInt(indices[i]);
            }
            Vector2Array uvs = geo.getUvs();
            outs.writeInt(uvs.size());
            for (int i = 0; i < uvs.size(); i++) {
                outs.writeFloat((float)uvs.getElement(i).getX());
                outs.writeFloat((float)uvs.getElement(i).getY());
            }
        }
        outs.writeInt(kids.size());
        for (LoadedObject kid : kids) {
            kid.serialize(outs);
        }
        outs.writeInt(facelistmaterial.size());
        for (String s : facelistmaterial) {
            outs.writeString(s);
        }
        outs.writeString(texture);
        outs.writeFloat((float)location.getX());
        outs.writeFloat((float)location.getY());
        outs.writeFloat((float)location.getZ());

        /*crease nicht mehr, weil SimpleGeo schon da ist if (crease != null) {
            outs.writeInt(1);
            outs.writeFloat(crease.degree);
        } else {
            outs.writeInt(0);
        }*/
    }

    /**
     * Bei non distinct material muss die Geometrie gesplittet werden
     *
     * @return
     */
    public boolean hasDistinctMaterials() {
        for (int i = 1; i < facelistmaterial.size(); i++) {
            if (!facelistmaterial.get(0).equals(facelistmaterial.get(i)))
                return false;
        }
        return true;
    }
}
