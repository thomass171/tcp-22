package de.yard.threed.core.platform;

import de.yard.threed.core.ImageData;

/**
 *
 * Die Contents sind pro Page, nicht pro Blatt.
 *
 * Die Seitennummerierung stattet bei 1. Die erste Seite ist die z.B. Buchdeckelseite.
 * 8.3.16: Umbenannt nach Native...
 * 27.4.16: Siehe Kommentar ResourceManager zur Abgrenzung.
 * Created by thomass on 06.02.15.
 */
public interface NativeContentProvider {
    int getNumberOfPages();

    /**
     * Die pageno startet bei 1, wie bei echten Buechern auch.
     * Sollte immer eine Texture liefern, auch bei ungueltigen Seiten (z,B. eine empty page).
     * Das kann da dann ja geloggt werden.
     * 
     *
     * 27.4.16:Liefert Imagedata, weil es async läuft (laufen kann) und da das Texturanlegen (zumindest in Unity) nicht geht,
     * bzw. zumindest fragwürdig ist. Und konzeptmaessig passt das in einem NativeContentProvider auch besser (Trennung View-Model).
     * EmptyPage rausgenommen, denn das ist kein Content. Wie eine leere Seite dargestellt wird, sollte der View wissen (z.B. Photoalbum ist nicht weiss).
     * @param pageno
     * @return
     */
    public ImageData getPage(int pageno);
    //public ImageData getEmptyPage();
}
