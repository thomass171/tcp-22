using System;
using java.lang;
namespace de.yard.threed.platform {

using de.yard.threed.engine;
using de.yard.threed.core.platform;
using de.yard.threed.core;
using de.yard.threed.platform.unity;
    using de.yard.threed.core.configuration;

    /**
     *
     */
    public class TestFactory  {
    //Log logger = Platform.getInstance().getLog(typeof(UnityBase3D));
    
        public static Platform initPlatformForTest(){
            PlatformInternals pli = PlatformUnity.init(new ConfigurationByProperties(new java.util.HashMap<string, string>()));
            PlatformUnity pl = (PlatformUnity)Platform.getInstance ();
            pl.logfactory = new TestLogFactory();
            return pl;
        }

   
}
}