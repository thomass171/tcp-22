package de.yard.threed.core.geometry;


import de.yard.threed.core.loader.PortableModel;

/**
 * Created on 03.01.19.
 */
public interface ProceduralModelCreator {
    PortableModel/*List*/ createModel() throws ModelCreateException;
}
