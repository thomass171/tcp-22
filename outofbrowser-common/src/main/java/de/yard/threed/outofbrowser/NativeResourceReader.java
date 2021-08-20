package de.yard.threed.outofbrowser;


import de.yard.threed.core.resource.ResourceNotFoundException;

/**
 *
 * Derived from ResourceManager
 * 
 * 19.4.16: 
 * Klasse Resource verschmolzen mit NativeFile zu NativeResource als eine Alternative zu einem simplen String als Filename, um besser
 * abbilden zu koennen, dass eine Resource nicht unbedingt als File im FS liegt; zumindest nicht
 * so unmittelbar, das kann auch ein Bundle sein (Unity) oder ein CLASSPATH.
 * 
 * 27.4.16:
 * Prinzipiell ist ein ResourceManager dateibezogen, egal wo die Datei liegt. Das unterscheidet ihn dann vom Contentprovider, der auf einer
 * höherern Abstraktionsebene arbeitet. Also: ContentProvider verwendet ResourceManager.
 * 
 * 09.04.2017: Bundlekonzept soll Einzeldateien ablösen.
 *
 * Date: 05.08.21
 */
public abstract class NativeResourceReader {

    /**
     * bischen Kruecke. Nicht fuer WebGL.
     * 11.10.18: Liefert nicht byte[], das hat sich nicht bewahrt wegen GWT.
     * 5.8.21: Waere aber auch jetzt keine Hilfe.
     */
    public abstract String loadTextFile(String resource) throws ResourceNotFoundException;

    /**
     * Not for textures. Die kann man technisch natürlich schon hier laden, das ist aber Resourcenverschwenung, weil die
     * Platform die direkt in die GPU laden kann/soll.
     * Wenn der Dateiname mit Endung ".gz" exisitert (nicht in resouce enthalten), wird on-the-fly entpackt.
     *
     */
    public abstract byte[] loadBinaryFile(String resource) throws ResourceNotFoundException;

    /**
     * 4.8.17: Eine Kruecke um Unity und Java an dieser Stelle zusammenzufuehren fuer Bundleimplementierung
     * ".gz" ist hier auch transparent.Das ist fuer Bundlenamen, nicht Inhalte in Bundles.
     */
     public abstract boolean exists(String resource);

}
