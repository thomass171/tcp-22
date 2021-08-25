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
    public class UnityXmlDocument : UnityXmlElement/*Node*/ , NativeDocument
    {
        XmlDocument doc;

        public UnityXmlDocument (XmlDocument doc) : base (doc.DocumentElement)
        {
            this.doc = doc;
        }

        public NativeNodeList getElementsByTagName (String name)
        {
            return new UnityXmlNodeList (doc.GetElementsByTagName (name));
        }
    }
}
