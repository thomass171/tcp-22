package de.yard.threed.core.resource;


/**
 * 19.4.16: Eine Alternative zu einem simplen String als Filename, um besser
 * abbilden zu koennen, dass eine Resource nicht unbedingt als File im FS liegt; zumindest nicht
 * so unmittelbar, das kann auch ein Bundle sein (Unity) oder ein CLASSPATH.
 * Verschmolzen mit Resource zu NativeResource.
 *
 * 27.5.2016: Die Methoden exists() und getParent() gehoeren hier nicht hin, denn die
 * kann es ja erst nach dem Laden der Resource bzw. nur ueber die Platform geben.
 * path ist eigentlich doppelt zu name, was ja auch ein Pfad sein kann.
 * 
 * 01.06.2016: path ist jetzt ein optionmaler PRefix, wenn die Resource nicht gebundled ist. isBundled() hat Prio, dann wird 
 * path ignoriert.
 * 21.12.16: Den path nicht mehr als string, sondern eigene Klasse. Aber auch in einem Bundle kann es einen Pfad geben. Der Name
 * ist idealerweise (muss aber nicht) nur der Dateiname. Darum noch eine Ebene bundlepath. Damit isType isbundled() aber nicht obselet.
 * 
 * Created by thomass on 11.12.15.
 */
public interface NativeResource {

    /**
     * 21.12.16: Liefert den Pfad im Bundle, seit es den bundlepath gibt.
     * @return
     */
    public ResourcePath getPath();

    public String getName();

    /**
     * isbundled wird trotz bundlepath gebraucht, weil der auch null sein kann.
     * @return
     */
    boolean isBundled();

    /**
     * Liefert den uebergeordneten Pfad zu dem "buindle", oder jar, oder sonst was.
     * @return
     */
    ResourcePath getBundlePath();

    /**
     * path+name
     * @return
     */
    String getFullName();
    
    String getExtension();
}
