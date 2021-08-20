package de.yard.threed.engine;


import de.yard.threed.engine.loader.PortableModelList;

/**
 * Created on 03.01.19.
 */
public interface ProceduralModelCreator {
    PortableModelList createModel() throws ModelCreateException;
}
