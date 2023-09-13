package de.yard.threed.core.geometry;


import de.yard.threed.core.loader.PortableModelList;

/**
 * Created on 03.01.19.
 */
public interface ProceduralModelCreator {
    PortableModelList createModel() throws ModelCreateException;
}
