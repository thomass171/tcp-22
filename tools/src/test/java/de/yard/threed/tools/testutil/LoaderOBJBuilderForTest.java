package de.yard.threed.tools.testutil;

import de.yard.threed.engine.AbstractLoaderBuilder;
import de.yard.threed.engine.loader.AbstractLoader;
import de.yard.threed.engine.loader.InvalidDataException;
import de.yard.threed.engine.platform.common.StringReader;
import de.yard.threed.tools.LoaderOBJ;

public class LoaderOBJBuilderForTest implements AbstractLoaderBuilder {

    public static int instanceCount = 0;

    /**
     * Needs noargconstructor for reflection.
     */
    public LoaderOBJBuilderForTest() {
        instanceCount++;
    }

    @Override
    public boolean supports(String extension) {
        return extension.equalsIgnoreCase("OBJ");
    }

    @Override
    public AbstractLoader buildAbstractLoader(byte[] data) throws InvalidDataException {
        return new LoaderOBJ(new StringReader(new String(data)));
    }
}
