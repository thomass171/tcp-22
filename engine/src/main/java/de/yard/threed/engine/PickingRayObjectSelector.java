package de.yard.threed.engine;

import de.yard.threed.core.Point;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.Platform;

import java.util.List;

public class PickingRayObjectSelector implements ObjectSelector {
    public Log logger = Platform.getInstance().getLog(PickingRayObjectSelector.class);
    SceneNode selectedObject = null;
    Camera camera;
    String mainNodeName;

    public PickingRayObjectSelector(Camera camera, String mainNodeName) {
        this.camera = camera;
        this.mainNodeName = mainNodeName;
    }

    @Override
    public boolean update() {
        Point mouseUp = Input.getMouseUp();
        if (mouseUp != null) {
            // Mousebutton released
            checkForPickingRay(mouseUp);
            return true;
        }
        return false;
    }

    @Override
    public SceneNode getSelectedObject() {
        return selectedObject;
    }

    private void checkForPickingRay(Point mouselocation) {
        int x = mouselocation.getX();
        int y = mouselocation.getY();
        //logger.debug("Mouse moved to x" + x + ", y=" + y);
        Ray pickingray = camera.buildPickingRay(camera.getCarrier().getTransform(), mouselocation);
        logger.debug("built pickingray=" + pickingray + " for x=" + x + ",y=" + y);
        List<NativeCollision> intersects = pickingray.getIntersections();
        if (intersects.size() > 0) {
            SceneNode foundModelObject = findIntersectedObject(intersects);
            if (foundModelObject != null) {
                selectedObject = foundModelObject;
            }
        } else {
            logger.debug("no intersection found");
            selectedObject = null;
        }
        //updateHud();
    }

    /**
     * Only consider parts of the model, but no hud or menu parts
     */
    private SceneNode findIntersectedObject(List<NativeCollision> intersects) {
        String names = "";
        for (int i = 0; i < intersects.size(); i++) {
            names += "," + intersects.get(i).getSceneNode().getName();
        }
        for (NativeCollision intersect : intersects) {
            SceneNode firstIntersect = new SceneNode(intersect.getSceneNode());
            if (isPartOfPreviewModel(firstIntersect)) {
                logger.debug("" + intersects.size() + " intersections detected: " + names + ", getFirst = " + firstIntersect.getName());
                return firstIntersect;
            }
        }
        return null;
    }

    private boolean isPartOfPreviewModel(SceneNode n) {
        if (mainNodeName.equals(n.getName())) {
            return true;
        }
        if (n.getParent() == null) {
            return false;
        }
        return isPartOfPreviewModel(n.getParent());
    }

}
