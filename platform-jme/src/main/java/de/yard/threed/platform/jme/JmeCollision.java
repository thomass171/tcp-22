package de.yard.threed.platform.jme;

import com.jme3.collision.CollisionResult;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeSceneNode;


/**
 * Created by thomass on 30.11.15.
 */
public class JmeCollision implements NativeCollision {
    CollisionResult collision;
    NativeSceneNode model;

    public JmeCollision(NativeSceneNode model, CollisionResult cr) {
        collision = cr;
        this.model = model;
    }

    @Override
    public NativeSceneNode getSceneNode() {
        // Hier muss genau die Node gefunden/verwendet werden,
        // die bei der Suche verwendet wurde.

        //Node rootnode = JmeScene.getInstance().getRootNode();
        //rootnode.getChild()
        return model;
    }

    @Override
    public Vector3 getPoint() {
        return JmeVector3.fromJme(collision.getContactPoint());
    }
}
