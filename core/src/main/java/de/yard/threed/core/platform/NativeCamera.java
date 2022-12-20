package de.yard.threed.core.platform;

import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Point;
import de.yard.threed.core.Vector3;

/**
 * A camera component (like a mesh). So no transform here. [[MA29]]
 *
 * There is no lookat(). This is just a convenience method that could be implemented independent from the platform. lookat() functions
 * in the platform use different concepts.
 * And there is no up-vector for similar reasons. An up-vector just feeds the illusion of 'rotations can be simple'.
 * <p>
 * Created by thomass on 05.06.15.
 */
public interface NativeCamera {

    /**
     *
     */
    public Matrix4 getProjectionMatrix();

    /**
     * Returns matrix neutral from possible handedness conversion.
     * siehe auch DVK.
     *
     * @return
     */
    public Matrix4 getViewMatrix();

    /**
     * 7.4.16: Build a picking ray derived from the current mouselocation. Thats why its located in camera.
     * 4.11.19: Not suitable in WebVR because the real viewpoint auch mit disAbled VR(!) 1.7m higher than the claimed camera Position! So better pass real view point.
     * 20.12.22: But what is the purpose of mouse location in VR?
     */
    NativeRay buildPickingRay(NativeTransform realViewPosition, Point mouselocation);

    double getNear();

    double getFar();

    double getAspect();

    double getFov();

    /**
     * Returns "real" camera position independent from carrier. Only for VR.
     * 20.12.22: What is it needed for?
     * @return
     */
    Vector3 getVrPosition(boolean dumpInfo);

    /**
     * Set the layer to be rendered by this camera. No bitmask, just an index from 0-15.
     *
     * @param layer
     */
    void setLayer(int layer);

    /**
     * 7.10.19 Useful even when the platform might use bitmaps for layer. These are flattened to only one layer.
     */
    int getLayer();

    void setName(String name);

    String getName();

    NativeSceneNode getCarrier();

    void setClearDepth(boolean clearDepth);

    void setClearBackground(boolean clearBackground);

    /**
     * dis/enable Rendering of this camera.
     * @param b
     */
    void setEnabled(boolean b);

    void  setFar(double far);
}
