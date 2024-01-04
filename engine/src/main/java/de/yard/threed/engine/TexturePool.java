package de.yard.threed.engine;

import de.yard.threed.core.NumericType;
import de.yard.threed.core.NumericValue;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.BundleResource;
import de.yard.threed.core.resource.URL;

import java.util.HashMap;

/**
 * MA36: Extrahiert aus Platform
 */
public class TexturePool {
    private HashMap<String, NativeTexture> loadedtexture = new HashMap<String, NativeTexture>();

    /**
     * Laden einer Textrur in den GPU Speicher anstossen. Die Textur ist dann nachher (irgendwann) da.
     * Oder auch nicht. Die hier zurueckgelieferte NativeTexture kann sofort in Materialien
     * verwendet werden und referenziert nur die Textur im GPU Speicher, ohne wirklich ihre Daten zu enthalten.
     * <p/>
     * Bei WebGl erfolgt das asynchrone Laden laden intern von ThreeJS.
     * <p/>
     * Die Referenzen werden gemerkt und evtl. mehrfach zurückgeliefert, um das mehrfachladen von Texturen zu
     * vermeiden.
     * 12.10.15: preferred way um Texturen zu laden.
     * 23.2.16: Ob das cachen so ideal ist? Die Wrappings koennten unterschiedlich sein.
     * 19.4.16: Praktisch zur Vermeidung von mehrfachladen wäre es schon. Evtl. ginge auch eine Art Warning.
     * 10.6.16: Auch wenn die Wrappings unterschiedlich sind, sollte es nicht erforderlich sein, die ganze Textur mehrfach
     * zu laden. Ist evtl. platformabhängig. Aber ueber einen häufig relativen Pfadnamen auf dieselbe Textur zu schliessen,
     * birgt natürlich ein Risiko. Vielleicht sollte diese Logik in die Modellader oder eine Modellib.
     * 3.1.19: Das mit der Standardtextur ist doch eine doofe FehlerKaschierung. Das soll der Aufrufer doch pruefen und
     * im Zweifel ohne Material (wireframe) anlegen. Also, liefert jetzt null im Fehlerfall.
     * MA36: Das scheint immer noch nicht das Wahre. Hier mal entfernt.
     * <p>
     * TODO texturepool ueber bundle.
     * 27.7.21: Ohne so einen Pool ist memory usage viel zu gross. In backyard gibts sowas auch noch.
     * 2.1.24: Returns (and ever did) null when the texture couldn't be created.
     * @return
     */
    public NativeTexture loadTexture(BundleResource name, HashMap<NumericType, NumericValue> parameters) {
        if (loadedtexture.get(name.getName()) == null) {
            URL url = URL.fromBundleResource(name);
            if (url == null) {
                Platform.getInstance().getLog(TexturePool.class).error("no URL");
                return null;
            }
            NativeTexture nt = Platform.getInstance().buildNativeTexture(url, parameters);
            if (nt == null) {
                //statistics.texturefailures++;
                return null;
            }
            loadedtexture.put(name.getName(), nt);
            //getLog().debug("Loaded texture "+name.getFullName());
            //statistics.textures++;
        }
        return loadedtexture.get(name.getName());
    }

    /**
     * Eigentlich nur fuer Tests.
     */
    public boolean hasTexture(String name) {
        return loadedtexture.containsKey(name);
    }

}
