package de.yard.threed.engine;

import de.yard.threed.engine.platform.common.Request;

/**
 * Handling the effect of a VR controller pointer/ray. Eg. intersection or targeting.
 */
public interface PointerHandler {

    void processPointer(Ray ray, boolean left);

    Request getRequestByTrigger(int userEntityId, Ray ray, boolean left);

}
