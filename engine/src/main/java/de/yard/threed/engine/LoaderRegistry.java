package de.yard.threed.engine;


import de.yard.threed.core.CharsetException;
import de.yard.threed.core.Util;


import de.yard.threed.core.loader.AbstractLoader;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.LoaderAC;
import de.yard.threed.core.loader.LoaderGLTF;
import de.yard.threed.core.loader.PortableModelList;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.loader.StringReader;

/**
 *
 * No Singleton, but static.
 * 8.9.23: Sounds nice to have a registry by suffix. But as long as AbstractLoader not only is the loader but also contains the loaded
 * data and parameter for loader are very specific (eg. ignoreacworld), it isn't worth the effort.
 * 8.9.23: Using a LoaderRegistry only sounds good. Model loader might have complex setup
 * (eg. ac texturepath, btg matlib) and the use case often needs to know what type of model
 * it is loading for providing all needed information.
 * 18.10.23: TODO Maybe only tools should have a LoaderRegistry. And the engine itself always uses GLTF. Currrently only "ac" and "gltf" are handled here. And ac shouldn't
 * be needed any more as it is converted to "gltf".
 * <p>
 * Created by thomass on 21.12.16.
 */
public class LoaderRegistry {
    static Log logger = Platform.getInstance().getLog(LoaderRegistry.class);

    /**
     * noch ziemlich drissig
     * Hier muss die ganze Resource rein, weil zum Beispiel die Modelloader den Pfad wissen mussen.
     * 6.12.17: Ob das noch gilt? Ich geb jetzt mal nur noch die extension rein. Vorlauefig wird der Pfad aber fuer acpp noch gebraucht. Auch zukuenftig. Der Pfad 
     * wird immer wichtig sein, auch fuer GLTF. Darum wieder zurueck.
     * TODO BundleResource als PArameter ist ungeeignet, weil es auch extern aus tools verwendet wird. Überhaupt muss die ganze Registry bzw die Logik mittelfristig in
     * die tools wandern. 28.12.17: Jetzt dorhin kopiert.
     * @throws InvalidDataException
     */
    /*13.2.24 public static PortableModelList loadBySuffix(BundleResource file, BundleData /* InputStream* / ins, boolean  ignoreacworld) throws InvalidDataException {
        String filename = file.getName();
        String extension = file.getExtension();
        /*15.6.21 if (extension.equals( "3ds")) {
            AbstractLoader loader = new Loader3DS(new ByteArrayInputStream(ins.b));
            // Bei einem Fehler ist er schon ausgestiegen
            PortableModelList ppfile = loader.preProcess();
            return ppfile;
        }* /
        if (extension.equals( "ac")) {
            // 18.10.23: Only used by tools? But tools has its own loadBySuffix.
            if (!ins.isText()) {
                logger.error("no string data for " + file.getFullName());
                return null;
            }
            String d;
            try {
                d = ins.getContentAsString();
            } catch (CharsetException e) {
                // TODO improved eror handling
                throw new RuntimeException(e);
            }
            AbstractLoader loader = new LoaderAC(new StringReader(d),ignoreacworld);
            // Bei einem Fehler ist er schon ausgestiegen
            // TO DO Wenn das ac in einem jar lag, muss der texturepath auch noch den bundlkepfad enthalten!
            // 6.12.17: Von wann ist das denn? brauchts das wirklich? mal ohne versuchen. Nein, das geht nicht bei readerwriterstg.
            loader.loadedfile.texturebasepath = file.getPath();
            PortableModelList ppfile = loader.preProcess();
            return ppfile;
        }
       /*3.5.19 if (extension.equals( "acpp")) {
            AbstractLoader loader = new LoaderAC(file,new ByteArrayInputStream( ins.b), file.getPath());

            // Bei einem Fehler ist er schon ausgestiegen
            //return new ModelLoaderProcessor(loader,loader.ploadedfile);
            return loader.ploadedfile;
        }* /
        if (extension.equals("btg")) {
            //AbstractLoader loader = new LoaderBTG(((ins.b)), options, boptions);
            Util.nomore();//27.12.17
            return null;//loader.ploadedfile;
        }
        //6.12.17: wegen extension einfach mal annegebn, dass jedes gz ein btg ist
        if (extension.equals( "gz")) {
            //AbstractLoader loader = new LoaderBTG((ins.b), options, boptions);
            //return new ModelLoaderProcessor(loader,null);
            Util.nomore();//27.12.17
            return null;//return loader.ploadedfile;
        }
        // 21.12.17: gltf jetzt auch explizit ladbar (nicht nur als preprocessed)
        if (extension.equals( "gltf")) {
             AbstractLoader loader = LoaderGLTF.buildLoader(file,/*i/*ns.s,* /  file.path);
            // Bei einem Fehler ist er schon ausgestiegen
            PortableModelList ppfile = loader.preProcess();
            //return new ModelLoaderProcessor(loader,ppfile);
            return ppfile;
        }

        logger.warn("unknown suffix " + extension);
        return null;
    }*/


}
