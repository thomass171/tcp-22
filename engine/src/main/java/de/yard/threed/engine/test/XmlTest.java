package de.yard.threed.engine.test;

import de.yard.threed.core.platform.*;

import de.yard.threed.core.XmlException;
import de.yard.threed.core.testutil.TestUtil;

import java.util.HashMap;


/**
 * Date: 27.08.15
 */
public class XmlTest {
    String xmltext = "<?xml version=\"1.0\" ?>\n" +
            "<message>\n" +
            "  <header>\n" +
            "    <to displayName=\"Richard\" address=\"rick@school.edu\" />\n" +
            "    <from displayName=\"Joyce\" address=\"joyce@website.com\" />\n" +
            "    <sent>2007-05-12T12:03:55Z</sent>\n" +
            "    <subject>Re: Flight info</subject>\n" +
            "  </header>\n" +
            "  <body>I'll pick you up at the airport at 8:30.  See you then!</body>\n" +
            "</message>";

    public void testSampleXml() {
        try {
            // parse the XML document into a DOM
            NativeDocument messageDom = Platform.getInstance().parseXml(xmltext);

            // find the sender's display name in an attribute of the <from> tag
            NativeNode fromNode = messageDom.getElementsByTagName("from").getItem(0);
            String from = ((NativeElement) fromNode).getAttribute("displayName");
            TestUtil.assertEquals("from", "Joyce", from);
            NativeAttributeList fromattrlist = ((NativeElement) fromNode).getAttributes();
            TestUtil.assertEquals("fromattrlist.size", 2, fromattrlist.getLength());
            // Die Reihenfolge der Attribute ist nicht definiert!
            HashMap<String,String> attr = new HashMap<String,String>();
            for (int i=0;i<fromattrlist.getLength();i++){
                attr.put(fromattrlist.getItem(i).getNodeName(),fromattrlist.getItem(i).getNodeValue());
            }
            //TestUtil.assertEquals("from.displayname", "displayName", adisplayName);
            TestUtil.assertEquals("from.displayname", "Joyce", attr.get("displayName"));
            //TestUtil.assertEquals("from.address", "address", fromattrlist.getItem(1).getNodeName());
            TestUtil.assertEquals("from.address", "joyce@website.com", attr.get("address"));
            TestUtil.assertEquals("from.address", "joyce@website.com", fromattrlist.getNamedItem("address").getNodeValue());
            
            // get the subject using Node's getNodeValue() function
            String subject = messageDom.getElementsByTagName("subject").getItem(0).getFirstChild().getNodeValue();

            // get the message body by explicitly casting to a Text node
            NativeText bodyNode = (NativeText) messageDom.getElementsByTagName("body").getItem(0).getFirstChild();
            String body = bodyNode.getData();
            TestUtil.assertEquals("body", "I'll pick you up at the airport at 8:30.  See you then!", body);
        } catch (XmlException e) {
            throw new RuntimeException(e);
        }
    }
}
