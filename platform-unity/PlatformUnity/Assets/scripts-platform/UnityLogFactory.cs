using System;
using java.lang;
using UnityEngine;

namespace de.yard.threed.platform
{
    using de.yard.threed.engine;
    using de.yard.threed.core.platform;
    
    using de.yard.threed.platform.unity;

    /**
     * 6.4.16: Logging ausserhalb von Tests, z.B. log4j
     */
    public class UnityLogFactory : NativeLogFactory
    {
        public Log getLog (Type clazz)
        {
            return new UnityLog (clazz);
        }
    }


}