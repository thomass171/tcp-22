package de.yard.threed.core.testutil;

import de.yard.threed.core.JavaStringHelper;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.*;

import java.util.HashMap;

/**
 * Simple Platform implementation eg. for unit tests.
 * <p>
 * Just provides a logger und StringHelper.
 * SimpleHeadlessPlatform is not available in core because it depends on core (would cause a cyclic dependency)
 * <p>
 * <p>
 * Created on 05.12.18.
 */
public class TestPlatform extends DefaultPlatform {
    
    TestPlatform( NativeLogFactory logfactory) {
        this.logfactory=logfactory;
        StringUtils.init(buildStringHelper());
    }

    public static PlatformInternals init(NativeLogFactory logfactory, Configuration configuration) {

        Platform.instance = new TestPlatform( logfactory);
        PlatformInternals platformInternals=new PlatformInternals();
        return platformInternals;
    }

    @Override
    public Log getLog(Class clazz) {
        return logfactory.getLog(clazz);
    }

    @Override
    public NativeStringHelper buildStringHelper() {
        return new JavaStringHelper();
    }

    @Override
    public float getFloat(byte[] buf, int offset) {
        return 0;
    }

    @Override
    public void setFloat(byte[] buf, int offset, float f) {

    }

    @Override
    public double getDouble(byte[] buf, int offset) {
        return 0;
    }

    @Override
    public NativeByteBuffer buildByteBuffer(int size) {
        return null;
    }
}
