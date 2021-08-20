package de.yard.threed.javacommon;


import de.yard.threed.core.resource.NativeResource;
import de.yard.threed.outofbrowser.FileSystemResource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 19.4.16 Nicht umbenannt nach JAResource, weil dies ja eine explizite Implementierung für FS Zugriff ist.
 * 26.5.16: Der darf NativeResource nicht implementieren, sondern als Parameter erwarten.
 * Created by thomass on 11.12.15.
 */
public class JAFile { //implements NativeResource {
    public File file;

    public JAFile(NativeResource path) {
        this.file = new File(path.getFullName());
    }

    //@Override
    public boolean exists() {
        return file.exists();
    }

   // @Override
    public String getParent() {
        return file.getParent();
    }

    //@Override
    public String getPath() {
        return file.getPath();
    }

    //@Override
    public String getName() {
        return file.getName();
    }

    //@Override
    public boolean isBundled() {
        return false;
    }

    public List<NativeResource> listFiles(){
        File[] files = file.listFiles();
        List<NativeResource> list = new ArrayList<NativeResource>();
        for (File f:files){
            list.add(new FileSystemResource(f.getAbsolutePath()));
        }
        return list;
    }

   /* public static NativeResource buildFile(String path) {
        // das mit dem Pfad ist erstmal ein Provisoruim, bis Resourcenumgang geklärt ist
        // 19.3.16: Vorlaeufig geklaert.
        //16.3.16: jetzt nach resources verschoben
        /*19.3.16: FileReader ist jetzt auch classpath faehig if (!path.startsWith("/")){
            //path = "src/main/webapp/"+path;
            path = "src/main/resources/"+path;
        }* /
        return new JAFile(path);
    }*/
}
