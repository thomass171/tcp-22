package de.yard.threed.engine;

import de.yard.threed.core.CharsetException;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.Platform;

import de.yard.threed.core.XmlException;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleData;
import de.yard.threed.core.resource.BundleRegistry;

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

    public XmlDocument(NativeDocument nativedocument)  {
        this.nativedocument = nativedocument;
    }

    public static XmlDocument buildXmlDocument(String xmltext) throws XmlException {
        return new XmlDocument(xmltext);
    }

    public static XmlDocument buildFromBundle(String bundle, String configfile) {

        Log logger = Platform.getInstance().getLog(XmlDocument.class);

        Bundle bnd = BundleRegistry.getBundle(bundle);
        if (bnd == null) {
            logger.error("bundle not found:" + bundle);
            // andere Fehlerbehdnalung?
            return null;
        }
        if (!bnd.contains(configfile)) {
            logger.error("config file not found:" + configfile);
            // andere Fehlerbehdnalung?
            return null;
        }
        BundleData xml = bnd.getResource(configfile);

        NativeDocument tw = null;
        try {
            try {
                tw = Platform.getInstance().parseXml(xml.getContentAsString());
            } catch (CharsetException e) {
                // TODO improved eror handling
                throw new RuntimeException(e);
            }
        } catch (XmlException e) {
            e.printStackTrace();
        }
        if (tw == null) {
            logger.error("parsing xml failed:" + xml);
            return null;
        }
        return new XmlDocument(tw);
    }
}
