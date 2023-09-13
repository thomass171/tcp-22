package de.yard.threed.core.loader;

import de.yard.threed.core.loader.AbstractLoader;
import de.yard.threed.core.loader.InvalidDataException;

/**
 * A builder that can build the AbstractLoader.
 */
public interface AbstractLoaderBuilder {

    boolean supports(String extension);

    AbstractLoader buildAbstractLoader(byte[] data, String filenameForInfo) throws InvalidDataException;
}
