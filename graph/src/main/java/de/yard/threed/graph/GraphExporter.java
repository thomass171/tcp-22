package de.yard.threed.graph;

import de.yard.threed.core.Vector3;

import java.util.List;

/**
 * Created on 05.06.18.
 */
public class GraphExporter {
    /**
     * 16.5.19: Das mit den tripnodes ist, naja, erstmal so.
     */
    public static String exportToXML(Graph graph, boolean negatez, List<Long> tripnodes) {
        //Locale locale = new Locale("us", "US");
        String s = "";
        s += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        s += "<graph>\n";

        s += "  <nodes>\n";
        for (int i = 0; i < graph.getNodeCount(); i++) {
            GraphNode n = graph.getNode(i);
            Vector3 l = n.getLocation();

            s += buildTag("    ", "n", new String[]{
                    buildAttribute("name", n.getName()),
                    buildAttribute("x", l.getX()),
                    buildAttribute("y", l.getY()),
                    buildAttribute("z", (negatez) ? -l.getZ() : l.getZ())});
        }
        s += ("  </nodes>\n");
        s += ("  <edges>\n");
        for (int i = 0; i < graph.getEdgeCount(); i++) {
            GraphEdge e = graph.getEdge(i);
            //s += (locale, "    <e name=\"%s\" from=\"%s\" to=\"%s\"/>\n", e.getName(), e.getFrom().getName(), e.getTo().getName());
            s += buildTag("    ", "e", new String[]{
                    buildAttribute("name", e.getName()),
                    buildAttribute("from", e.getFrom().getName()),
                    buildAttribute("to", e.getTo().getName())});
        }
        s += ("  </edges>\n");
        if (tripnodes != null) {
            s += ("  <tripnodes>\n");
            for (int i = 0; i < tripnodes.size(); i++) {
                //s += (locale, "    <tripnode osmid=\"%s\"/>\n", tripnodes.get(i).toString());
                s += buildTag("    ", "tripnode", new String[]{
                        buildAttribute("osmid", tripnodes.get(i).toString())});
            }
            s += ("  </tripnodes>\n");
        }
        s += ("</graph>\n");
        return s;
    }

    private static String buildTag(String prefix, String tag, String[] attr) {
        String s = prefix;
        s += "<" + tag;
        for (String a : attr) {
            s += " " + a;
        }
        s += "/>\n";
        return s;
    }

    private static String buildAttribute(String name, double value) {
        String d = "" + value;
        d = d.replace(",", ".");
        return "" + name + "=\"" + d + "\"";
    }

    private static String buildAttribute(String name, String value) {
        return "" + name + "=\"" + value + "\"";
    }
}
