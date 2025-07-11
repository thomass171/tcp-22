package de.yard.threed.javacommon;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.configuration.Configuration;
import de.yard.threed.core.platform.DefaultPlatform;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeLogFactory;
import de.yard.threed.core.platform.NativeStringHelper;
import de.yard.threed.core.platform.PlatformInternals;

/**
 * A very mini platform for (Spring) server and tools, where SimpleHeadlessPlatformFactory
 * is too complex, eg. because it expects a HOSTDIR.
 * <p>
 * Derived from ToolsPlatform
 * <p>
 * Created on 08.07.25.
 */
public class MinimalisticPlatform extends DefaultPlatform {

    NativeLogFactory logfactory;

    public static PlatformInternals init(Configuration configuration) {
        instance = new MinimalisticPlatform(configuration, clazz -> new JALog(clazz));

        PlatformInternals platformInternals = new PlatformInternals();

        return platformInternals;
    }

    private MinimalisticPlatform(Configuration configuration, NativeLogFactory logfactory) {
        this.logfactory = logfactory;
        StringUtils.init(buildStringHelper());
    }

    @Override
    public Log getLog(Class clazz) {
        return logfactory.getLog(clazz);
    }

    @Override
    public NativeStringHelper buildStringHelper() {
        return new DefaultJavaStringHelper();
    }
}
