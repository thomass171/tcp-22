using System;
using System.IO;
using UnityEngine;
using java.lang;
using java.util;
using de.yard.threed.core.platform;
using de.yard.threed.core.buffer;
using de.yard.threed.engine.util;

/**
 * 16.10.18: Die alte LoadedResource Klasse fuere Unity mit InputStream
 */
namespace de.yard.threed.platform.unity
{
    public class UnityLoadedResource
    {
        ByteArrayInputStream bais;
        public NativeTexture texture;

        public UnityLoadedResource (ByteArrayInputStream bais)
        {
            this.bais = bais;
        }


        public UnityLoadedResource( NativeTexture texture) {
            this.texture = texture;
        }
    }
}

