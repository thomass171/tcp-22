package de.yard.threed.engine.osm;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeDocument;
import de.yard.threed.core.platform.NativeElement;
import de.yard.threed.core.platform.NativeNodeList;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.platform.*;
import de.yard.threed.core.Color;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.XmlException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by thomass on 14.09.15.
 */
public class OsmFactory {
    public static List<SceneNode> buildWays(byte[] osmxml) {
        List<SceneNode> modellist = new ArrayList<SceneNode>();
        try {
            NativeDocument doc = Platform.getInstance().parseXml(StringUtils.buildString(osmxml));
            NativeNodeList xmlallnodes = doc.getElementsByTagName("node");
            HashMap<Long, Node> allnodes = new HashMap<Long, Node>();
            for (int i = 0; i < xmlallnodes.getLength(); i++) {
                NativeElement node = (NativeElement) xmlallnodes.getItem(i);
                allnodes.put(Long.parseLong(node.getAttribute("id")), new Node(Float.parseFloat(node.getAttribute("lat")), Float.parseFloat(node.getAttribute("lon"))));
            }
            NativeNodeList allways = doc.getElementsByTagName("way");
            for (int i = 0; i < allways.getLength(); i++) {
                NativeElement way = (NativeElement) allways.getItem(i);
                modellist.add(buildRoad(way, allnodes));
            }
        } catch (XmlException e) {
            e.printStackTrace();
            //TODO
        }
        return  modellist;
    }

    /**
     * Extrudieren entlang eines Pfades. Könnte auch mit Track kombiniert werden (TODO pruefen).
     * 29.9.15: TODO: MEthode läuft noch auf eine RuntimeException bei der Pfadbildung,
     * Codedublette zu EffectFactory
     * @param way
     * @param allnodes
     */
    private static SceneNode buildRoad(NativeElement way, HashMap<Long, Node> allnodes) {
        Shape shape = new Shape(false);
        shape.addPoint(new Vector2(0.5f, 0));
        shape.addPoint(new Vector2(2.5f, 0));

        Vector3 origin = new Vector3(0, 0, 0);
        SegmentedPath path = new SegmentedPath(origin);
        NativeNodeList waynodes = way.getElementsByTagName("nd");
        for (int i = 0; i < waynodes.getLength(); i++) {
            long ndref = Long.parseLong(((NativeElement) waynodes.getItem(i)).getAttribute("ref"));
            Node node = allnodes.get(ndref);
            Vector3 destination = new Vector3(node.getProjectedLat(), 0, node.getProjectedLon());
            path.addLine(destination);
        }

        ShapeGeometry g = new ShapeGeometry(shape, path);

        Material mat = Material.buildLambertMaterial(new Color(0xCC, 00, 0xCC));

        Mesh mesh = new Mesh(g, mat);
        SceneNode model = new SceneNode();
        model.setMesh(mesh);
        return model;
    }
}
