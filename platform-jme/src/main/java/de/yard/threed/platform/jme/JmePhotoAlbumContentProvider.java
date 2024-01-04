package de.yard.threed.platform.jme;

import de.yard.threed.core.platform.Platform;
import de.yard.threed.outofbrowser.FileSystemResource;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeContentProvider;
import de.yard.threed.core.ImageData;
import de.yard.threed.core.Color;
import de.yard.threed.core.ImageFactory;
import de.yard.threed.javacommon.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 27.7.21:Candidate for java-common?
 * Created by thomass on 06.11.15.
 */
public class JmePhotoAlbumContentProvider implements NativeContentProvider {
    Log logger = Platform.getInstance().getLog(JmePhotoAlbumContentProvider.class);
    ImageData emptytexture;
    List<File[]> pagessource = new ArrayList<File[]>();
File directory;

    public JmePhotoAlbumContentProvider(File directory) {
        this.directory = directory;
        // Eine empty page wird auf jeden Fall gebraucht, sonst folgt irgendwann NPE
        ImageData image = ImageFactory.buildSingleColor(200, 100, Color.GRAY);
        emptytexture = image;//Platform.getInstance().buildNativeTexture(image);

        // TODO andere Fehlerbehandlung
        if (!directory.exists()) {
            logger.warn("not exists");
            return;
        }
        if (!directory.isDirectory()) {
            logger.warn("no directory");
            return;
        }
        File[] currentpair = new File[2];
        for (File file : directory.listFiles()) {
            if (file.getName().toUpperCase().endsWith("JPG")) {
                logger.debug("found jpg file " + file.getName());
                if (currentpair[0] == null) {
                    currentpair[0] = file;
                } else {
                    currentpair[1] = file;
                }
                if (currentpair[1] != null) {
                    pagessource.add(currentpair);
                    currentpair = new File[2];
                }
            }
        }
        if (currentpair[0] != null) {
            pagessource.add(currentpair);
        }


    }

    @Override
    public int getNumberOfPages() {
        return pagessource.size();/* / 2 + pagessource.size() % 2;*/
    }

    @Override
    public ImageData getPage(int pageno) {
        if (pageno < 1 || pageno > pagessource.size()) {
            logger.warn("pageno = " + pageno + ", pagessource.size=" + pagessource.size());
            return getEmptyPage();
        }
        File[] sources = pagessource.get(pageno - 1);
        //JmeTexture topimage = JmeTexture.loadFromFile(directory.getAbsolutePath()+"/"+sources[0].getName());
        ImageData topimage = ImageUtil.buildImageData(ImageUtil.loadImageFromFile(new FileSystemResource(directory.getAbsolutePath() + "/" + sources[0].getName())));
                JmeTexture bottomimage = null;
        if (sources[1] != null) {
            FileSystemResource r= new FileSystemResource(directory.getAbsolutePath()+"/"+sources[1].getName());
            bottomimage = JmeTexture.loadFromFile(directory.getName(), ImageUtil.loadImageFromFile(r));
        }
        //TODO bottom
        return topimage;//new NativeTexture[]{topimage,bottomimage};
    }

  /*  @Override*/
    public ImageData getEmptyPage() {
        return emptytexture;
    }
}
