package de.yard.threed.platform.webgl;

import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import de.yard.threed.core.platform.NativeElement;
import de.yard.threed.core.platform.NativeNodeList;


/**
 * Created by thomass on 11.09.15.
 */
public class GwtElement extends GwtNode implements NativeElement {
    GwtElement(Node node){
        super(node);
    }
    @Override
    public String getAttribute(String name) {
        return ((Element)node).getAttribute(name);
    }

    @Override
    public NativeNodeList getElementsByTagName(String name) {
        return new GwtNodeList(((Element)node).getElementsByTagName(name));
    }

    
}
