package de.yard.threed.engine;

/**
 * In Anlehnung an das GWT ClientBundle.
 *
 * Eine Resource wird geladen und steht dann nach dem Laden zur Verfügung.
 * Irgendwie sehr abstrakt, darum auch abstract.
 *
 * 19.4.16: Jetzt erstmal eine Alternative zu einem simplen String als Filename, um besser
 * abbilden zu koennen, dass eine Resource nicht unbedingt als File im FS liegt; zumindest nicht
 * so unmittelbar, das kann auch ein Bundle sein (Unity) oder ein CLASSPATH.
 * Verschmolzen mit NativeFile zu NativeResource. 
 * 
 * 27.03.2017: Erweitert zu einem Wrapper mit statischen load Methoden um die Platform.
 * 09.04.2017: Jetzt auch Wrapper fuer Bundle.
 * 
 * Created by thomass on 29.09.15.
 */
public abstract class Resource {
    String source;
    String name;

    public Resource(String name, String source) {
        this.source = source;
        this.name = name;
    }

    public Resource() {

    }

    /*16.10.18 public static LoadedResource loadResourceSync(final NativeResource resource) throws ResourceNotFoundException{
        return  Platform.getInstance().getRessourceManager().loadResourceSync(resource);
    }*/

}
