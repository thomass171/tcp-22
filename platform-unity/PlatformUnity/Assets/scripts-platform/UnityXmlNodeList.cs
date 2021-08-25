using System;
using System.IO;
using UnityEngine;
using java.lang;
using System.Xml;
using java.util;

using de.yard.threed.core.platform;

namespace de.yard.threed.platform.unity
{
    
    /**
 * Created by thomass on 11.09.15.
 */
    public class UnityXmlNodeList : NativeNodeList
    {
        XmlNodeList nodelist;

        public UnityXmlNodeList (XmlNodeList nodelist)
        {
            this.nodelist = nodelist;
        }

            public NativeNode getItem (int i)
        {
            return new UnityXmlElement/*Node*/ (nodelist[i]);
        }


    public int getLength ()
        {
            return nodelist.Count;
        }
    }
}
