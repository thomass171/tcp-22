using System;
using System.IO;
using UnityEngine;
using java.lang;
using System.Xml;
using java.util;
using de.yard.threed.core;
using de.yard.threed.core.platform;

namespace de.yard.threed.platform.unity
{
/**
 * Created by thomass on 11.09.15.
 */
public class UnityXmlException : System.Exception/*NativeException*/   {
        public UnityXmlException(java.lang.Exception e) :base(e.getMessage()){

    }
}
}
