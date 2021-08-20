package de.yard.threed.javacommon;


import de.yard.threed.core.platform.NativeAttributeList;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeNodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Created by thomass on 11.09.15.
 */
public class JavaXmlNode implements NativeNode {
    Node node;

    public JavaXmlNode(Node node) {
        this.node = node;
    }

    @Override
    public NativeNode getFirstChild() {
        Node c = node.getFirstChild();
        if (c instanceof Text){
            return new JavaXmlText((Text) c);
        }
        return new JavaXmlNode(c);
    }

    @Override
    public String getNodeValue() {
        return node.getNodeValue();
    }

    @Override
    public String getNodeName() {
        return node.getNodeName();        
    }

    @Override
    public String getTextValue() {
        Node child = node.getFirstChild();
        if (child.getNodeType() == Node.TEXT_NODE) {
            return child.getTextContent();
        }
        return null;
    }

    @Override
    public NativeNodeList getChildNodes() {
        return new JavaXmlNodeList(node.getChildNodes());
    }

    @Override
    public NativeAttributeList getAttributes() {
        // node kann z.B. com.sun.org.apache.xerces.internal.dom.DeferredCommentImpl sein
        if (!(node instanceof org.w3c.dom.Element)){
            // liefert leere Liste
            return new JavaXmlAttributeList(null);
        }
        return new JavaXmlAttributeList(((Element)node).getAttributes());
    }

}
