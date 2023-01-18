package de.yard.threed.core.testutil;

import de.yard.threed.core.JavaStringHelper;
import de.yard.threed.core.buffer.NativeByteBuffer;
import de.yard.threed.core.platform.*;

import java.util.HashMap;

/**
 * Simple Platform implementation eg. for unit tests.
 * <p>
 * Just provides a logger und StringHelper.
 * <p>
 * <p>
 * Created on 05.12.18.
 */
public class TestPlatform extends /*16.6.21 SimpleHeadless*/DefaultPlatform {
    
    TestPlatform( NativeLogFactory logfactory) {
        this.logfactory=logfactory;
    }

    public static /*16.6.21 Engine*/PlatformInternals init(NativeLogFactory logfactory, HashMap<String, String> properties) {
        for (String key : properties.keySet()) {
            //System.out.println("transfer of propery "+key+" to system");
            System.setProperty(key, properties.get(key));
        }
        Platform.instance = new TestPlatform( logfactory);
        // 16.5.21: reset not needed here?
        //((EnginePlatform) Platform.instance).resetInit();
        PlatformInternals platformInternals=new PlatformInternals();
        return /*(EnginePlatform)* / Platform.instance*/platformInternals;
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
