using System;
using System.IO;
using System.Xml;
using UnityEngine;
using java.lang;
using java.util;
using de.yard.threed.core.platform;

namespace de.yard.threed.platform.unity
{
    /**
 * Created by thomass on 11.03.17.
 */
    public class UnityXmlAttribute : UnityXmlNode, NativeAttribute
    {
        
        public UnityXmlAttribute (XmlNode attr) : base (attr)
        {
        }

        public String getValue ()
        {
            return node.Value;
        }
          
    }
}
