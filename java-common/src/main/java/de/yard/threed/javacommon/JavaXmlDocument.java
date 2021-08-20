package de.yard.threed.javacommon;


import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNodeList;
import org.w3c.dom.Document;

/**
 * Created by thomass on 11.09.15.
 */
public class JavaXmlDocument extends JavaXmlElement/*Node*/ implements NativeDocument {
    Document doc;

    public JavaXmlDocument(Document doc){
        super(doc.getDocumentElement());
        this.doc = doc;
    }

    @Override
    public NativeNodeList getElementsByTagName(String name) {
        return new JavaXmlNodeList(doc.getElementsByTagName(name));
    }
}
