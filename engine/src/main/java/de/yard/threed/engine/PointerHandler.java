package de.yard.threed.engine;

import de.yard.threed.engine.platform.common.Request;

public interface PointerHandler {

    void processPointer(Ray ray, boolean left);

    Request getRequestByTrigger(Ray ray, boolean left);

}
