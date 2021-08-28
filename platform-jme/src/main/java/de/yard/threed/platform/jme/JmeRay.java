package de.yard.threed.platform.jme;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by thomass on 28.11.15.
 */
public class JmeRay implements NativeRay {
    Log logger = Platform.getInstance().getLog(JmeRay.class);

    Ray ray;

    private JmeRay(Ray ray) {
        this.ray = ray;
    }

    public static JmeRay buildRay(Vector3 origin, Vector3 direction) {
        Ray ray = new Ray((JmeVector3.toJme(origin)), JmeVector3.toJme(direction));
        return new JmeRay(ray);
    }

    @Override
    public Vector3 getDirection() {
        return  JmeVector3.fromJme(ray.getDirection());
    }

    @Override
    public Vector3 getOrigin() {
        return  JmeVector3.fromJme(ray.getOrigin());
    }

    @Override
    public List<NativeCollision> intersects(NativeSceneNode model){
        List<NativeCollision> results = findIntersections(((JmeSceneNode)model).getNode());
        return results;
    }

    @Override
    public List<NativeCollision> getIntersections() {
        return findIntersections(JmeScene.getInstance().getRootNode());
    }
    
    private List<NativeCollision>  findIntersections(Node startingnode){
        CollisionResults results = new CollisionResults();
        //Jme wird rekursiv suchen
        startingnode.collideWith(ray, results);
        //logger.debug("found "+results.size()+" collisions for node "+startingnode.getName());
        Iterator iter = results.iterator();
        List<NativeCollision> na = new ArrayList<NativeCollision>();
        while (iter.hasNext()){
            CollisionResult cr = (CollisionResult) iter.next();
            Geometry g = cr.getGeometry();
            //14.11.16 JmeSceneNode n = new JmeSceneNode(g.getParent());
            // Warum muss man eigentlich ueber den parent gehen?
            /*MA17: Ich leg jetzt einfach eine neue Instanz von SceneNode an.
            Integer uniqueid = g.getParent().getUserData("uniqueid");
            if (uniqueid == null){
                logger.warn("Geo without id");
            }else{
                NativeTransform obj = Platform.getInstance().findObject3DById(uniqueid);
                if (obj == null){
                    logger.warn("object not found by id");
                }else {
                    JmeSceneNode n = (JmeSceneNode)obj.getSceneNode();
                    na.add(new JmeCollision(n, cr));
                }
            }*/
            // Warum muss man eigentlich ueber den parent gehen?
            JmeSceneNode n = new JmeSceneNode(g.getParent());
            na.add(new JmeCollision(n, cr));
        }
        return na;
    }
}
