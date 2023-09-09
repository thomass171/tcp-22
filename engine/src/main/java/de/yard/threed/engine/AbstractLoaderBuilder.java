package de.yard.threed.engine;

import de.yard.threed.engine.loader.AbstractLoader;
import de.yard.threed.engine.loader.InvalidDataException;

/**
 * A builder that can build the AbstractLoader.
 */
public interface AbstractLoaderBuilder {

    boolean supports(String extension);

    AbstractLoader buildAbstractLoader(byte[] data) throws InvalidDataException;
}
