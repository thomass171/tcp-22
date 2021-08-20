package de.yard.threed.platform.webgl;

import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.Text;
import de.yard.threed.core.platform.NativeAttributeList;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.core.platform.NativeNodeList;

/**
 * Created by thomass on 11.09.15.
 */
public class GwtNode implements NativeNode {
    Node node;

    public GwtNode(Node node) {
        this.node = node;
    }

    @Override
    public NativeNode getFirstChild() {
        Node c = node.getFirstChild();
        if (c instanceof Text){
            return new GwtText((Text) c);
        }
        return new GwtNode(node.getFirstChild());
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
            // 22.3.18: Nicht ganz sicher ob das stimmt
            return child.getNodeValue();
        }
        return null;
    }

    @Override
    public NativeNodeList getChildNodes() {
        return new GwtNodeList(node.getChildNodes());
    }

    @Override
    public NativeAttributeList getAttributes() {
        // node kann z.B. com.sun.org.apache.xerces.internal.dom.DeferredCommentImpl sein
        if (!(node instanceof com.google.gwt.xml.client.Element)){
            // liefert leere Liste
            return new GwtAttributeList(null);
        }
        return new GwtAttributeList(((Element)node).getAttributes());
    }
}
