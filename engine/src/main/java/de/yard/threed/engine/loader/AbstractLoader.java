package de.yard.threed.engine.loader;

import de.yard.threed.core.Degree;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.geometry.GeometryHelper;
import de.yard.threed.engine.platform.common.SimpleGeometry;

import java.util.ArrayList;
import java.util.List;

/**
 * 12.12.17: Hier ist jetzt der preprocess, damit BTG Loader den ueberschreiben kann (andere natuerlich auch, gibt es aber noch nicht).
 * Created by thomass on 12.02.16.
 */
public abstract class AbstractLoader {
    public LoadedFile loadedfile = new LoadedFile();
    public PortableModelList ploadedfile = null;//30.12.18new PortableModelList();
    //public Platform pf = ((Platform)Platform.getInstance());
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
            loaddurationms= (int) (Platform.getInstance().currentTimeMillis() - starttime);
        } catch (Exception se) {
            //Sonderbares ExceptionHandling wegen C#
            java.lang.Exception e = new java.lang.Exception (se);
            throw new InvalidDataException("error in line " + getCurrentLine() + ": " + /*wegen C# e.getClass().getName() +*/ ":" + e.getMessage(), e);
        }
        // success info log isType in AsyncHelper
       
    }

    protected abstract void doload() throws InvalidDataException;

    protected abstract int getCurrentLine();

    protected abstract Log getLog();

    /**
     * Die eingelesenen Geometrien in SimpleGeos konvertieren.
     */
    public PortableModelList preProcess() {
        getLog().info("preprocessing");
        PortableModelList ppfile = new PortableModelList(loadedfile.texturebasepath);
        // source liegt hier nicht vor
        ppfile.objects = preProcess(ppfile,loadedfile.objects);
        ppfile.materials = loadedfile.materials;
        //29.12.18 ppfile.texturebasepath = loadedfile.texturebasepath;
        //27.12.17: Die Texturenames aus den Objects in die Materialien uebernehmen.
        for (LoadedObject obj : loadedfile.objects){
            transferTextureNames(ppfile, obj);
        }
        ppfile.loaddurationms = loaddurationms;
        return ppfile;
    }

    /**
     * Die Texturenames aus den (AC)Objects in die Materialien uebernehmen.
     * AC verwendet teilweise (z.B. 777 Overhead) Materialien mit verschiedenen Texturen. Dann muss das Material eben dupliziert werden.
     */
    private void transferTextureNames(PortableModelList ppfile, LoadedObject obj){
        if (obj.name != null && obj.name.equals("PANEL_B1")){
            int z=8;
        }

        if (obj.texture!=null){
            for (int index=0;index<obj.facelistmaterial.size();index++){
                String materialname = obj.facelistmaterial.get(index);
                PortableMaterial mat = ppfile.findMaterial(materialname);
                if (mat.texture == null){
                    mat.texture = obj.texture;
                }else{
                    if (!mat.texture.equals(obj.texture)){
                        //Duplicate material logging not required? 
                        getLog().warn("ambicious texture name in material "+materialname+" in "+obj.name);
                        //build unique name
                        PortableMaterial dupmat = mat.duplicate(mat.name+ppfile.materials.size());
                        ppfile.materials.add(dupmat);
                        dupmat.texture = obj.texture;
                        obj.facelistmaterial.set(index,dupmat.name);
                    }
                }
               
            }
        }
        for (LoadedObject o : obj.kids){
            transferTextureNames(ppfile, o);
        }
    }
    
    private List<PortableModelDefinition> preProcess(PortableModelList ppfile, List<LoadedObject> objs) {
        List<PortableModelDefinition> ppobjs = new ArrayList<PortableModelDefinition>();
        for (int i = 0; i < objs.size(); i++) {
            LoadedObject obj = objs.get(i);
            PortableModelDefinition ppobj = new PortableModelDefinition();
            ppobj.name = obj.name;
            ppobj.geolistmaterial=obj.facelistmaterial;
            ppobj.translation = obj.location;
            ppobjs.add(ppobj);
            // Nicht mehrfach preprocessen. 11.12.17: Mit Aufsplittung nicht mehr pruefbar
            //if (obj.geolist == null) {
            if (obj.vertices.size() > 0) {
                // TODO Normalenliste uebergeben, wenn vorhanden (OBJ?). Bei AC stehen die Normalen im Face.(13.7.16: Nöö, die werden doch berechnet)
                // Dass die keine Kanten haben, ist jetzt erstmal eine Annahme
                // 15.12.16: Ob ein Smoothing über verschiedene Materialien hinweg erfolgen sollte? Unklar. Wird jetzt aber mal so gemacht.
                // Die SmoothingMap kann hier noch nicht erstellt werden, weil noch nicht trianguliert ist und die Normalen noch unbekannt sind.
                boolean hasedges = false;
                if (obj.faces.size() > 1 && !obj.hasDistinctMaterials()) {
                    ppobj.geolist = GeometryHelper.prepareGeometry(obj.vertices, obj.faces, /*matlist,*/ null, true, obj.crease, hasedges, null);

                } else {
                    // Dann können die Facelisten geflattet werden. 
                    Degree crease = obj.crease;
                    // 31.3.17: Unterdruecken des crease Smoothing bei unshaded (z.B. fuer Douglas.ac)
                    if (obj.faces.size() > 0 && obj.faces.get(0).unshaded) {
                        //und wenn es in den anderen ist?
                        crease = null;
                    }
                    ppobj.geolist = GeometryHelper.prepareGeometry(obj.vertices, obj.faces, /*matlist,*/ null, false, crease, hasedges, null);
                    if (ppobj.geolist.size() > 1) {
                        throw new RuntimeException("unexpected multiple geos");
                    }
                }
            } else {
                ppobj.geolist = new ArrayList<SimpleGeometry>();
            }
            obj.faces = null;
            obj.vertices = null;
            obj.normals = null;
            //}
            ppobj.kids = preProcess(ppfile,obj.kids);
        }
        return ppobjs;
    }

}




