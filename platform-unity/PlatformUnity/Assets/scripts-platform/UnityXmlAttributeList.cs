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
    public class UnityXmlAttributeList : NativeAttributeList
    {
        /*NamedNodeMap*/
        XmlAttributeCollection attrlist;

        public UnityXmlAttributeList (XmlAttributeCollection attrlist)
        {
            this.attrlist = attrlist;
        }

        public NativeAttribute getItem (int i)
        {
            if (attrlist == null) {
                return null;
            }
           
            return new UnityXmlAttribute/*Node*/ (attrlist [i]);
        }

        public int getLength ()
        {
            if (attrlist == null) {
                return 0;
            }
            return attrlist.Count;
        }

        public NativeAttribute getNamedItem (String name)
        {
            if (attrlist == null) {
                return null;
            }
            XmlNode n = attrlist.GetNamedItem (name);
            if (n == null) {
                return null;
            }
            return new UnityXmlAttribute/*Node*/ (n);
        }
    }
}
