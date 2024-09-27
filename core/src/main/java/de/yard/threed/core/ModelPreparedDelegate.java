package de.yard.threed.core;


import de.yard.threed.core.loader.PreparedModel;

/**
 * Listener for preparing a shared model via the platform.
 */
@FunctionalInterface
public interface ModelPreparedDelegate {

    /**
     *
     */
    void modelPrepared(PreparedModel preparedModel);
}
