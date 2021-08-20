package de.yard.threed.core.platform;

import de.yard.threed.core.Dimension;
import de.yard.threed.core.SceneUpdater;


import java.util.List;

/**
 * Created by thomass on 05.06.15.
 */
public interface NativeScene {
    /**
     * add/remove geht nur mit Object3D, nicht mit Base3D. Das ist ja nichts eigenes.
     * @param object3d
     */
    //void add(NativeObject3D object3d);
    void add(NativeSceneNode object3d);
    //15.6.16 void add(NativeMesh object3d);

    /**
     * 27.4.16: Der remove hier ist fragwürdig, denn ein Objekt muss eigentlich aus dem parent entfernt werden. Und das ist nicht
     * unbedingt die Scene. Eh fragwürdig wegen Unity destroy. Jetzt ueber Platform.
     */
    //21.7.16 void remove(NativeSceneNode object3d);
    //15.6.16 void remove(NativeMesh object3d);

    void add(NativeLight light);
    void addSceneUpdater(SceneUpdater sceneupdater);
    void removeSceneUpdater(SceneUpdater sceneupdater);
    List<SceneUpdater> getSceneUpdater();
    
    //void addActionListener(int[] keycode, NativeActionListener actionListener);
    //2.3.16 void addAnalogListener(int keycode, NativeAnalogListener actionListener);

    /**
     * Die Camera an das model haengen. Das model darf auch null sein.
     *
     * Die position ist im Modelsystem, also relativ zum model.
     *
     */
    /*29.9.18@Deprecated
    void enableModelCamera(NativeSceneNode model, NativeCamera nativecamera, Vector3 position, Vector3 lookat);*/

    /**
     * Liefert die tatsaechliche aktuelle screen/window/viewer size.
     */
    Dimension getDimension();

    
    //2.3.16 void addMouseMoveListener(NativeMouseMoveListener lis);

    
}
