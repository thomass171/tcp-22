using System;
using System.IO;
using System.Xml;
using UnityEngine;
using java.lang;
using java.util;
using de.yard.threed.platform;
using de.yard.threed.core.platform;

namespace de.yard.threed.platform.unity
{
    /**
 * Created by thomass on 11.09.15.
 */
    public class UnityXmlElement : UnityXmlNode , NativeElement
    {
        public UnityXmlElement (XmlNode node) : base (node)
        {

        }

        public String getAttribute (String name)
        {
            XmlAttribute t = ((XmlElement)node).GetAttributeNode (name);
            if (t == null)
            {
                return null;
            }
            return t.Value;
        }


    
        public NativeNodeList getElementsByTagName (String name)
        {
            return new UnityXmlNodeList (((XmlElement)node).GetElementsByTagName (name));
        }


    
        public NativeAttributeList getAttributes ()
        {
            // node kann z.B. com.sun.org.apache.xerces.internal.dom.DeferredCommentImpl sein
            if (!(node is System.Xml.XmlElement)) {
                // liefert leere Liste
                return new UnityXmlAttributeList (null);
            }
            return new UnityXmlAttributeList (((XmlElement)node).Attributes);
        }
    }
}
