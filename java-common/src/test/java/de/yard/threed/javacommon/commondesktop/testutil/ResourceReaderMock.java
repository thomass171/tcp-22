package de.yard.threed.javacommon.commondesktop.testutil;


import de.yard.threed.core.resource.ResourceNotFoundException;
import de.yard.threed.outofbrowser.NativeResourceReader;

public class ResourceReaderMock extends NativeResourceReader {
    @Override
    public String loadTextFile(String resource) throws ResourceNotFoundException {
        return "abc";
    }

    @Override
    public byte[] loadBinaryFile(String resource) throws ResourceNotFoundException {
        return new byte[7];
    }

    @Override
    public boolean exists(String resource) {
        return true;
    }
}
