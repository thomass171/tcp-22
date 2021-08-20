package de.yard.threed.engine;


import de.yard.threed.core.Util;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.loader.*;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.engine.platform.common.StringReader;

/**
 * OSG hat auch so einen Namen als Zentrale Stelle. Besser nicht einfach Registry nennen.
 * Nicht als Singleton, sondern static.
 * <p>
 * Created by thomass on 21.12.16.
 */
public class LoaderRegistry {
    static Log logger = Platform.getInstance().getLog(LoaderRegistry.class);
    //MA17 static HashMap<String, ReadFileCallback> nodeCallbackMap = new HashMap<String, ReadFileCallback>();
    //9.3.21 no longer used? private static HashMap<String, de.yard.threed.engine.LoaderFactory> loader = new HashMap<String, de.yard.threed.engine.LoaderFactory>();

    /**
     * noch ziemlich drissig
     * Hier muss die ganze Resource rein, weil zum Beispiel die Modelloader den Pfad wissen mussen.
     * 6.12.17: Ob das noch gilt? Ich geb jetzt mal nur noch die extension rein. Vorlauefig wird der Pfad aber fuer acpp noch gebraucht. Auch zukuenftig. Der Pfad 
     * wird immer wichtig sein, auch fuer GLTF. Darum wieder zurueck.
     * TODO BundleResource als PArameter ist ungeeignet, weil es auch extern aus tools verwendet wird. Überhaupt muss die ganze Registry bzw die Logik mittelfristig in
     * die tools wandern. 28.12.17: Jetzt dorhin kopiert.
     * @throws InvalidDataException
     */
    public static PortableModelList findLoaderBySuffix(BundleResource file, BundleData /* InputStream*/ ins, boolean  ignoreacworld) throws InvalidDataException {
        String filename = file.getName();
        String extension = file.getExtension();
        /*15.6.21 if (extension.equals( "3ds")) {
            AbstractLoader loader = new Loader3DS(new ByteArrayInputStream(ins.b));
            // Bei einem Fehler ist er schon ausgestiegen
            PortableModelList ppfile = loader.preProcess();
            return ppfile;
        }*/
        if (extension.equals( "ac")) {
            if (!ins.isText()) {
                logger.error("no string data for " + file.getFullName());
                return null;
            }
            AbstractLoader loader = new LoaderAC(new StringReader(ins.getContentAsString()),ignoreacworld);
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
        }*/
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
             AbstractLoader loader = LoaderGLTF.buildLoader(file,/*i/*ns.s,*/  file.path);
            // Bei einem Fehler ist er schon ausgestiegen
            PortableModelList ppfile = loader.preProcess();
            //return new ModelLoaderProcessor(loader,ppfile);
            return ppfile;
        }

        logger.warn("unknown suffix " + extension);
        return null;
    }


}
