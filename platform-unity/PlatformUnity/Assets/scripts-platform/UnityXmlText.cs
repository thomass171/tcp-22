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
 * Created by thomass on 11.09.15.
 */
    public class UnityXmlText : UnityXmlNode, NativeText
    {

        public UnityXmlText (XmlText text) : base (text)
        {
       
        }

    
        public String getData ()
        {
            return ((XmlText)node).Data;
        }
    }
}
