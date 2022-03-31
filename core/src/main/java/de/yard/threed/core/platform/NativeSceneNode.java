package de.yard.threed.core.platform;

/**
 * 11.3.16: Abgeleitet von Base3D oder Object3D? Es muss add/remove geben. Da Object3D wegen der Worldmatrix ein Parent
 * kennen muss, sollte er auch children kennen und damit add/remove. Damit add/remove nicht doppelt ist,
 * sollte folglich Model von Object3D ableiten statt Base3D.
 * <p>
 * 15.6.16: Umbenannt von Model nach SceneNode. Entspricht einem GameObject in Unity.
 * <p>
 * Created by thomass on 05.06.15.
 */
public interface NativeSceneNode /*16.9.16 extends  NativeObject3D/*Base3D*/ {
    /**
     * oder Object3D?
     */

    //4.2.16 jetzt in Camera void addCamera(NativeCamera camera);
   /*
    * 15.6.16: Mesh ist nicht mehr Teil des Scenegraph, sondern Komponente eines SceneNodes (analog Unity)
    * Aber darf es mehrere Meshes geben? Doch eher nein. Also setter 
    */
    public void setMesh(NativeMesh mesh);

    public NativeMesh getMesh();

    /*
     * 15.6.16: Light ist nicht mehr eigenstaendiger Teil des Scenegraph, sondern Komponente eines SceneNodes (analog Unity)
     * wegen des transofrm sollte das Gameobject (die SceneNode) exklusiv fuer das Light sein). Evtl. sollte man das exlusive orn mit Mesh?      
     * Aber darf es mehrere Lights in einer Node geben? Doch eher nein. Also setter.
     */
    void setLight(NativeLight light);
    
    /**
     * 29.3.22: New approach for a platform id of the node. This id will stay the same independent of wrapping core platform nodes. So it
     * is reliable to use it for checking find/collision results (MA17,MA22).
     * @return
     */
    int getUniqueId();

    /**
     * Entfernen aus Tree, destroy inkl. aller children.
     * 20.2.17: Das ist DIE Methode, um SceneNodes loszuwerden. Das umfasst auch die Childs der Node und das Freigeben von Speicher.
     * <p>
     * Analog zum Unity destroy(). Der macht auch einen destroy auf die Children.
     * Unsichtbar machen (disablen) ist was anderes.
     */
    void destroy();

    boolean isDestroyed();

    void setName(String name);

    String getName();

    /**
     * 23.12.16: Ist die Methode nicht fragwürdig im Sinne einer Unity Analogie? Muesste es stattdessen nicht ein getTransform hier geben?
     * 26.1.17: Ja, Ersetzt jetzt durch Native?Transform .
     *
     * @return
     */
    //NativeObject3D getObject3D();
    NativeTransform getTransform();


    /**
     * Liefert nur auf dem Carrier etwas, null otherwise.
     * @return
     */
    NativeCamera getCamera();

    /**
     * 5.10.17: Pruefung, ob das zugrundliegende Native Objekt das selbe ist.
     * Brauchts aber vielleicht nur intern.
     * 2018: Das ist konzeptionell unsauber, z.B. weil es nur ein Wrapper ist. Und was heisst schon "same".
     * @return
     */
    //boolean isSame(NativeSceneNode node);

    //mal langsam void setCamera(NativeCamera camera);

    //mal langsam public NativeCamera getCamera();

}
