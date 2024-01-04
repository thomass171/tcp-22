package de.yard.threed.platform.webgl;

import com.google.gwt.xml.client.Document;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeNodeList;

/**
 * Created by thomass on 11.09.15.
 */
public class GwtDocument extends GwtElement implements NativeDocument{
    Document doc;

    GwtDocument(Document doc){
        super(doc.getDocumentElement());
        this.doc = doc;
    }

    @Override
    public NativeNodeList getElementsByTagName(String name) {
        return new GwtNodeList(doc.getElementsByTagName(name));
    }

    @Override
    public NativeNode getRootElement() {
        return new GwtNode(doc.getDocumentElement());
    }


}
