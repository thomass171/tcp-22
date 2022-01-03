package de.yard.threed.traffic.config;

import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeNode;
import de.yard.threed.engine.util.XmlHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Auch mal generisch.
 * 10.5.19
 *
 */
public class ConfigNodeList extends ConfigNode {
    List<ConfigNode> nodes;

    public ConfigNodeList(NativeNode nativeNode, String tag) {
        super(nativeNode);
        nodes = new ArrayList<ConfigNode>();
        if (XmlHelper.getChildren(nativeNode, tag).size() > 0) {
            List<NativeNode> list = XmlHelper.getChildren(nativeNode, tag);
            for (NativeNode n : list) {
                nodes.add(new ConfigNode(n));
            }
        }
    }

    /*public static List<ConfigNodeList> filter(List<ConfigNodeList> lists, ConfigAttributeFilter filter) {
        List<ConfigNodeList> retlists = new ArrayList<>();
         for (ConfigNode nn:nodes){
            if (nn.complies(filter)) {
                retlists.add(nn);
            }
        }
         return retlists;
    }*/

    public int size() {
        return nodes.size();
    }

    public ConfigNode getNode(int index) {
        return (nodes.get(index));
    }

    public ConfigNode getNodeByName(String name) {
        /*TODOfor (SceneVehicle v : scenevehicles) {
            if (v.getName().equals(name)) {
                return v;
            }
        }*/
        return null;
    }

    public static List<ConfigNodeList> build(NativeDocument tw, String maintag, String tag, String subtag, ConfigAttributeFilter filter){
        List<ConfigNodeList> lists = new ArrayList<ConfigNodeList>();
        List<NativeNode> vehiclelists = (XmlHelper.getChildren(tw, maintag));
        List<NativeNode> nodes = XmlHelper.getChildNodeList(tw, maintag, tag);
        for (NativeNode nn:nodes) {
            if (new ConfigNode(nn).complies(filter)) {
                ConfigNodeList configNodeList = new ConfigNodeList(nn, subtag);
                lists.add(configNodeList);
            }
        }


       /* */
        return lists;
    }

    private void add(ConfigNode configNode) {
        nodes.add(configNode);
    }

    public ConfigNode get(int i) {
        return nodes.get(i);
    }
}
