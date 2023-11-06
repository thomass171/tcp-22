package de.yard.threed.tools.testutil;

import de.yard.threed.core.buffer.ByteArrayInputStream;
import de.yard.threed.core.buffer.SimpleByteBuffer;
import de.yard.threed.core.loader.AbstractLoader;
import de.yard.threed.core.loader.AbstractLoaderBuilder;
import de.yard.threed.core.loader.InvalidDataException;
import de.yard.threed.core.loader.StringReader;
import de.yard.threed.tools.Loader3DS;
import de.yard.threed.tools.LoaderOBJ;

public class Loader3DSBuilderForTest implements AbstractLoaderBuilder {

    public static int instanceCount = 0;

    /**
     * Needs noargconstructor for reflection.
     */
    public Loader3DSBuilderForTest() {
        instanceCount++;
    }

    @Override
    public boolean supports(String extension) {
        return extension.equalsIgnoreCase("3DS");
    }

    @Override
    public AbstractLoader buildAbstractLoader(byte[] data, String filenameForInfo) throws InvalidDataException {
        return new Loader3DS(new ByteArrayInputStream(new SimpleByteBuffer(data)));
    }
}
