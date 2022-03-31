package de.yard.threed.engine;

import de.yard.threed.engine.platform.common.Request;

public interface PointerHandler {

    void processPointer(Ray ray, boolean left);

    Request getRequestByTrigger(int userEntityId, Ray ray, boolean left);

}
