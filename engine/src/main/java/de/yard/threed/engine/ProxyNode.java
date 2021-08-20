package de.yard.threed.engine;

/**
 * Eine Node, die ihre Rotation an eine SlaveNode 1:1 spiegelt.
 */
public class ProxyNode extends SceneNode {
    private SceneNode slaveNode;

    public ProxyNode(SceneNode node, SceneNode slaveNode) {
        super(node);
        this.slaveNode = slaveNode;
        this.setName("ProxyNode");
    }

    @Override
    public Transform getTransform(){
        Transform t = super.getTransform();
        if (slaveNode!=null) {
            return new ProxyTransform(t.transform, slaveNode.getTransform());
        }
        return t;
    }
}
