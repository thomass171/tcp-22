package de.yard.threed.platform.webgl;


import de.yard.threed.core.platform.NativeAttribute;
import com.google.gwt.xml.client.Node;

/**
 * Created by thomass on 11.09.15.
 */
public class GwtAttribute extends GwtNode implements NativeAttribute {
    
    GwtAttribute(Node attr){
        super(attr);
    }

    @Override
    public String getValue() {
        return node.getNodeValue();
    }

}
