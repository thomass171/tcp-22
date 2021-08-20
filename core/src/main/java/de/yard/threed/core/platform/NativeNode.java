package de.yard.threed.core.platform;

/**
 * Created by thomass on 11.09.15.
 */
public interface NativeNode {
    NativeNode getFirstChild();

    String getNodeValue();
    String getNodeName();

    /**
     * Simplified method for retrieving like
     * if (child.getNodeType() == Node.TEXT_NODE)
     textContent.append(child.getTextContent());
     * @return
     */
    String getTextValue();
    NativeNodeList getChildNodes();

    //27.3.17: Hierhin verschoben aus NativeElement
    NativeAttributeList getAttributes();
}
