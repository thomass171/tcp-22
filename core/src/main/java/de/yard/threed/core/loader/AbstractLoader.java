package de.yard.threed.core.loader;

import de.yard.threed.core.Degree;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Util;
import de.yard.threed.core.geometry.FaceList;
import de.yard.threed.core.geometry.GeometryHelper;
import de.yard.threed.core.geometry.NormalBuilder;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.geometry.SimpleGeometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 8.9.23: Needs some documentation and maybe small refactorings. See also LoaderRegistry.
 * Created by thomass on 12.02.16.
 */
public abstract class AbstractLoader {
    public LoadedFile loadedfile = new LoadedFile();
    private long starttime = Platform.getInstance().currentTimeMillis();
    private int loaddurationms;

    /**
     * InvalidAcDataException wird nicht abgefangen. Oder doch, wegen Zeilennummer.
     * 01.06.16: Doch alle
     * 23.3.18: Damit werden auch NullPointer und ArrayIndex abgefangen, die immer mal auftreten koennen, wenn die Datei nicht OK ist.
     *
     * @throws InvalidDataException
     */
    protected void load() throws InvalidDataException {
        try {
            doload();
            loaddurationms = (int) (Platform.getInstance().currentTimeMillis() - starttime);
        } catch (Exception se) {
            //Strange exception handling due to C#
            java.lang.Exception e = new java.lang.Exception(se);
            throw new InvalidDataException("error in line " + getCurrentLine() + ": " + /*due to C# e.getClass().getName() +*/ ":" + e.getMessage(), e);
        }
        // success info log is in AsyncHelper

    }

    protected abstract void doload() throws InvalidDataException;

    protected abstract int getCurrentLine();

    protected abstract Log getLog();

    /**
     * Build a unified model.
     */
    public PortableModel buildPortableModel() {
        getLog().info("building PortableModel");

        //12.8.24  Now, this should be the final material building from candidate. Arrange materials first to
        //have final material names when objects are built.
        ArrayList<PortableMaterial> materials = new ArrayList<PortableMaterial>();
        //for (LoadedObject obj : loadedfile.objects) {
            arrangeMaterials(materials, loadedfile.object, loadedfile);
        //}

        PortableModel ppfile = new PortableModel(buildPortableModelDefinition(loadedfile.object), loadedfile.texturebasepath);
        // We don't know source here
        ppfile.materials = materials;
        ppfile.loaddurationms = loaddurationms;
        return ppfile;
    }

    /**
     * Transfer texture names from (AC)Objects to materials.
     * //12.8.24  this should be the final material building from candidate
     * AC verwendet teilweise (z.B. 777 Overhead) Materialien mit verschiedenen Texturen. Dann muss das Material eben dupliziert werden.
     * Renamed from 'transferTextureNames'
     * Also sets finalMaterial in loadedobjects.
     * 14.8.24: This is highly AC specific!
     */
    private void arrangeMaterials(ArrayList<PortableMaterial> materials, LoadedObject obj, LoadedFile loadedfile) {
        if (obj.name != null && obj.name.equals("PANEL_B1")) {
            int z = 8;
        }

        //14.8.24 build material in any case, not only with texture
        obj.finalMaterial = new ArrayList<>();
        for (int index = 0; index < obj.facelistmaterial.size(); index++) {
            String materialname = obj.facelistmaterial.get(index);
            String finalMaterialName = materialname;
            if (obj.texture != null) {
                // Improve? leads to 'ugly' names eg. with material name "Messages/follow_me.pn" and texture '"Messages/blanco.png"'
                finalMaterialName = materialname + "-" + StringUtils.substringBeforeLast(obj.texture, ".");
            }
            // Check whether we have a fitting material already
            PortableMaterial mat = PortableModel.findMaterial(materials, finalMaterialName);
            if (mat == null) {
                // create it
                mat = loadedfile.findMaterialCandidateByName(materialname).buildPortableMaterial(finalMaterialName, obj.texture);
                materials.add(mat);
            }
            obj.finalMaterial.add(finalMaterialName);
        }

        for (LoadedObject o : obj.kids) {
            arrangeMaterials(materials, o, loadedfile);
        }
    }

    private PortableModelDefinition buildPortableModelDefinition(LoadedObject obj) {
        // 27.7.24 these are the lists we had in PortableModelDefinition once. Here these are split now to the children list. Only if needed!
        List<String> geolistmaterial = obj.finalMaterial;//facelistmaterial;
        List<SimpleGeometry> geolist;

        // backfaces (and maybe other scenarios) break the index alignment between geolist and materiallist. So better
        // have a map.
        Map<Integer, Integer> materialMap = null;
        if (obj.vertices.size() > 0) {
            // TODO Normalenliste uebergeben, wenn vorhanden (OBJ?). Bei AC stehen die Normalen im Face.(13.7.16: Nöö, die werden doch berechnet)
            // Dass die keine Kanten haben, ist jetzt erstmal eine Annahme
            // 15.12.16: Ob ein Smoothing über verschiedene Materialien hinweg erfolgen sollte? Unklar. Wird jetzt aber mal so gemacht.
            // Die SmoothingMap kann hier noch nicht erstellt werden, weil noch nicht trianguliert ist und die Normalen noch unbekannt sind.
            boolean hasedges = false;
            if (obj.faces.size() > 1 && !obj.hasDistinctMaterials()) {
                /*ppobj.*/
                geolist = GeometryHelper.prepareGeometry(obj.vertices, obj.getAllFacelists(), /*matlist,*/ null, true, obj.crease/*, hasedges, null*/);
                materialMap = new HashMap<>();
                // 16.9.24:This map building does not appear really reliable. However, due to 'split' each facelist resulted in its own geo,
                // so the list index alignment should still fit. Only backfaces need special handling.
                for (int i = 0; i < geolist.size(); i++) {
                    if (i < obj.faces.size()) {
                        materialMap.put(i, i);
                    } else {
                        materialMap.put(i, backfaceMaterialIndex(obj.backfaces, i - obj.faces.size()));
                    }
                }

            } else {
                // Dann können die Facelisten geflattet werden.
                Degree crease = obj.crease;
                // 31.3.17: Unterdruecken des crease Smoothing bei unshaded (z.B. fuer Douglas.ac)
                // 17.9.24: Buildings also use crease/unshaded. For these it will not fit to just ignore crease.
                // But what does 'unshaded' really mean?
                /*17.9.24 if (obj.faces.size() > 0 && obj.faces.get(0).unshaded) {
                    //und wenn es in den anderen ist?
                    crease = null;
                }*/

                geolist = GeometryHelper.prepareGeometry(obj.vertices, obj.getAllFacelists(), /*matlist,*/ null, false, crease/*, hasedges, null*/);

                if (/*ppobj.*/geolist.size() > 1) {
                    throw new RuntimeException("unexpected multiple geos");
                }
            }
        } else {
            geolist = new ArrayList<SimpleGeometry>();
        }

        // if the LoadedObject needed to be split in multiple geos due to different material, use a synthetic container
        // like LoaderGLTF does.
        // Likely like it always was. Rely on same list size geolist/geolistmaterial?
        PortableModelDefinition ppobj = null;
        if (geolist.size() > 1) {
            ppobj = new PortableModelDefinition();
            for (int i = 0; i < geolist.size(); i++) {
                PortableModelDefinition subnode = new PortableModelDefinition(geolist.get(i), geolistmaterial.get(materialMap.get(i)));
                subnode.setName("subnode" + i);
                ppobj.addChild(subnode);
            }
        } else {
            if (geolist.size() == 0) {
                ppobj = new PortableModelDefinition();
            } else {
                ppobj = new PortableModelDefinition(geolist.get(0), (geolistmaterial == null || geolistmaterial.size() < 1) ? null : geolistmaterial.get(0));
            }
        }
        ppobj.name = obj.name;
        ppobj.translation = obj.location;

        /*3.8.24 obj.faces = null;
        obj.vertices = null;
        obj.normals = null;*/

        //}
        // dont overwrite 'kids'
        //27.7.24 ppobj.kids = preProcess(/*27.7.24 not needed? ppfile,*/ obj.kids);
        // one-to-one convert list objs to list of PortableModel. Children oj objs will become children in PortableModel
        for (int i = 0; i < obj.kids.size(); i++) {
            LoadedObject lobj = obj.kids.get(i);
            ppobj.addChild(buildPortableModelDefinition(lobj));
        }

        return ppobj;
    }

    /**
     * Return the n-th existing backface original index position (that points to correct material)
     */
    private int backfaceMaterialIndex(List<FaceList> backfaces, int n) {
        int cnt = 0;
        for (int i = 0; i < backfaces.size(); i++) {
            if (backfaces.get(i) != null) {
                if (cnt == n) {
                    return i;
                }
                cnt++;
            }
        }
        getLog().error("inconsistency");
        return 0;
    }

}




