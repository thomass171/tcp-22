using System;
using System.IO;
using UnityEngine;
using java.lang;
using java.util;
using de.yard.threed.platform;
using System.Xml;
using de.yard.threed.core.platform;

namespace de.yard.threed.platform.unity
{
    
    /**
 * Created by thomass on 11.09.15.
 */
    public class UnityXmlNode : NativeNode
    {
        public   XmlNode node;

        public UnityXmlNode (XmlNode node)
        {
            this.node = node;
        }


        public NativeNode getFirstChild ()
        {
            XmlNode c = node.FirstChild;
            if (c is XmlText) {
                return new UnityXmlText ((XmlText)c);
            }
            return new UnityXmlNode (c);
        }


        public String getNodeValue ()
        {
            return node.Value;
        }

        public String getTextValue ()
        {
            XmlNode c = node.FirstChild;
            //stoimmt das wohl?
            if (c is XmlText) {
                
                return  ((XmlText)c).Value;
            }
            return null;
        }

        public String getNodeName ()
        {
            return node.Name;        
        }


        public NativeNodeList getChildNodes ()
        {
            return new UnityXmlNodeList (node.ChildNodes);
        }

        public NativeAttributeList getAttributes() {
            // node kann z.B. com.sun.org.apache.xerces.internal.dom.DeferredCommentImpl sein
           /* if (!(node is org.w3c.dom.Element)){
                // liefert leere Liste
                return new UnityXmlAttributeList(null);
            }*/
            return new UnityXmlAttributeList(node.Attributes);
        }


    }

}
