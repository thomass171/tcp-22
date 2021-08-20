package de.yard.threed.engine;

import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.Platform;

import de.yard.threed.core.XmlException;

/**
 * Wrapper um die Platform XML Methoden.
 * <p>
 * Created by thomass on 27.03.17.
 */
public class XmlDocument {
    public NativeDocument nativedocument;

    public XmlDocument(String xmltext) throws XmlException {
        nativedocument = (Platform.getInstance()).parseXml(xmltext);
    }

    public static XmlDocument buildXmlDocument(String xmltext)  throws XmlException {
        return new XmlDocument(xmltext);
    }
}
