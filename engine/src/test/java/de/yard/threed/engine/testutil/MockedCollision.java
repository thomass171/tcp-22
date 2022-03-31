package de.yard.threed.engine.testutil;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.engine.SceneNode;

public class MockedCollision implements NativeCollision {

    SceneNode sceneNode;
    Vector3 point;

    public MockedCollision(SceneNode sceneNode, Vector3 point) {
        this.sceneNode = sceneNode;
        this.point = point;
    }

    @Override
    public NativeSceneNode getSceneNode() {
        return sceneNode.nativescenenode;
    }

    @Override
    public Vector3 getPoint() {
        return point;
    }
}
