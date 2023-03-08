package de.yard.threed.platform.homebrew;

import java.util.Vector;

/**
 * Created by thomass on 14.03.16.
 */
public class Renderables {
    // 14.3.16: Die renderables sind wegen lineraem Rendern nur noch Meshes.
    public  Vector<HomeBrewMesh> renderables = new Vector<HomeBrewMesh>();
    public  Vector<HomeBrewMesh> transparent = new Vector<HomeBrewMesh>();

    // 5.3.21: Fuer MP auch reine SceneNodes "rendern", um deren Position zu publishen.
    public  Vector<HomeBrewSceneNode> nodes = new Vector<HomeBrewSceneNode>();



}
