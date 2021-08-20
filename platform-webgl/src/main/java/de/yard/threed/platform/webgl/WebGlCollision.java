package de.yard.threed.platform.webgl;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeSceneNode;


/**
 * Created by thomass on 18.12.15.
 */
public class WebGlCollision implements NativeCollision {
    private WebGlSceneNode model;
    private WebGlVector3 point;

    public WebGlCollision(WebGlSceneNode model, WebGlVector3 point) {
        this.model = model;
        this.point = point;
    }

    @Override
    public NativeSceneNode getSceneNode() {
        return model;
    }

    @Override
    public Vector3 getPoint() {
        return WebGlVector3.fromWebGl(point);
    }
}
