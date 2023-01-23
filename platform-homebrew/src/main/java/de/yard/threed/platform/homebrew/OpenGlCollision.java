package de.yard.threed.platform.homebrew;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeSceneNode;


/**
 * Created by thomass on 25.08.16.
 */
public class OpenGlCollision implements NativeCollision {
    private final HomeBrewSceneNode node;
    Vector3 point;

    OpenGlCollision(HomeBrewSceneNode node, Vector3 point){
        this.node = node;
        this.point = point;
    }
    
    @Override
    public NativeSceneNode getSceneNode() {
        return node;
    }

    @Override
    public Vector3 getPoint() {
        return point;
    }
}
