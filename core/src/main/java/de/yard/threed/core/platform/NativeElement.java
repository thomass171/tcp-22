package de.yard.threed.core.platform;

import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeNodeList;

/**
 * Created by thomass on 11.09.15.
 */
public interface NativeElement extends NativeNode {

    /**
     * Different from org.w3c.dom.Node it doesn't return "" for a non existing attribute but null.
     */
    String getAttribute(String name);

    NativeNodeList getElementsByTagName(String name);
    //27.3.17: Waarum hier und ncht in Node?
    //NativeAttributeList getAttributes();
}
