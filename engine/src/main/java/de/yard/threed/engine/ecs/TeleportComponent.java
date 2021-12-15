package de.yard.threed.engine.ecs;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.engine.util.NearView;

import java.util.ArrayList;
import java.util.List;

/**
 * Positionen (Model oder auch Camera) inklusive Orientierung und optional ein parent (z.B. vehicle), an das attached ist.
 * Eine Component für einen Observer (Node mit attached camera).
 * Der observer kann selber keine Camera sein, sondern nur ein Kopfobjekt (Avatar/Pilot) mit attachter Camera, das sich dann dreht.
 * Gibt Überschneidung mit ViewpointComponent.
 * 19.10.17: Auch fuer VR. Um die Camera zu bewegen braucht man noch ObserverSystem.
 * 19.2.18:Jetzt mehrere Groups, jede mit eigenem Index. Das ist doch Quatsch. Das System muss mehrere Entities unterscheiden.
 * 15.10.19:Siehe unbedingt TeleporterSystem!
 * <p>
 * <p>
 * Created by thomass on 09.01.17.
 */
public class TeleportComponent extends EcsComponent {
    Log logger = Platform.getInstance().getLog(TeleportComponent.class);
    TeleportPointList teleportlist = new TeleportPointList();
    public static String TAG = "TeleportComponent";
    private SceneNode observer;
    public boolean needsupdate = false;
    private NearView lastNearView=null;

    /**
     * observer(z.B. avatar) muss/sollte die Camera attached haben.
     */
    public TeleportComponent(SceneNode observer) {
        this.observer = observer;
    }

    @Override
    public String getTag() {
        return "TeleportComponent";
    }

    public void addPosition(LocalTransform posrot) {
        addPosition(null, posrot);
    }

    public void addPosition(String label, LocalTransform posrot) {
        addPosition(label, null, posrot);
    }

    public void addPosition(String label, SceneNode parent, LocalTransform posrot) {
        teleportlist.addPosition(label, parent, posrot, null, null);
    }

    public void addPosition(String label, SceneNode parent, LocalTransform posrot, String targetEntity) {
        teleportlist.addPosition(label, parent, posrot, targetEntity, null);
    }

    public void addPosition(String label, SceneNode parent, LocalTransform posrot, String targetEntity, NearView nearView) {
        teleportlist.addPosition(label, parent, posrot, targetEntity, nearView);
    }

    public static TeleportComponent getTeleportComponent(EcsEntity e) {
        TeleportComponent gmc = (TeleportComponent) e.getComponent(TeleportComponent.TAG);
        return gmc;
    }

    public void removePosition(String label) {
        int index = findPoint(label);
        if (index != -1) {
            teleportlist.point.remove(index);
            //1.1.20 index reset
            teleportlist.index=0;
        }
    }

    public int findPoint(String label) {
        for (int i = 0; i < teleportlist.point.size(); i++) {
            if (teleportlist.point.get(i).label.equals(label)) {
                return i;
            }
        }
        return -1;
    }

    public void setPosition(int i, Vector3 offset) {
        teleportlist.point.get(i).point.position = (offset);
        needsupdate = true;
    }

    /**
     * Might return null when there isType no point defined (yet).
     *
     * @return
     */
    public LocalTransform getPosRot() {
        if (teleportlist.index >= teleportlist.point.size()) {
            return null;
        }
        return teleportlist.point.get(teleportlist.index).point;
    }

    public SceneNode getParent() {
        return teleportlist.point.get(teleportlist.index).parent;
    }

    public NearView getNearView() {
        return teleportlist.point.get(teleportlist.index).nearView;
    }

    public int step(boolean forward) {
        TeleportPointList points = teleportlist;
        int newindex = points.step(forward);
        needsupdate = true;
        logger.debug("newindex="+newindex+"("+points.point.get(newindex).label+") of "+points.point.size()+":"+points.point.get(newindex).point);
        return newindex;
    }

    /**
     * Set position programmatically.
     * 26.10.18
     * @param index
     */
    public void stepTo(int index) {
        TeleportPointList points = teleportlist;
        //TODO check range
        points.index = index;
        needsupdate = true;
    }

    public int getPointCount() {
        return teleportlist.point.size();
    }

    public String getPointLabel(int index) {
        return teleportlist.point.get(index).label;
    }

    public void setIndex(int index) {
        teleportlist.index = index;
    }

    public int getIndex() {
        return teleportlist.index;
    }

    public String getTargetEntity() {
        return teleportlist.point.get(teleportlist.index).targetEntity;
    }

    /**
     * 24.10.19: Aus dem StepController jetzt mal fuer Teleport hierhin kopiert und angepasst/vereinfacht.
     */
    public LocalTransform setNewStep(/*SceneNode node, Vector3 position, Quaternion rotation,*/ boolean useanimation) {
        SceneNode node = observer;
        LocalTransform posrot = getPosRot();
        if (posrot == null) {
            //no point to step to yet.
            return null;
        }
        if (useanimation && node != null) {
            // Animation dürfte nicht zwischen attached/detached gehen, bzw. nur kompliziert. 31.8.16: Darum nicht mit camera sondern nur mit Node.
            //THREED Base3D startprs = fpscamera.getPrsData();
            Animation animation = new MoveAnimation(node, posrot.position, posrot.rotation, 10);
            SceneAnimationController.getInstance().startAnimation(animation, true);
        } else {
            node.getTransform().setPosition(posrot.position);
            node.getTransform().setRotation(posrot.rotation);
        }
        SceneNode parent = null;
        if (getParent() != null) {
            parent = getParent();
        }

        if (parent != null) {
            node.getTransform().setParent(parent.getTransform());
        } else {
            //Das mit world ist doof. 21.12.17: Und fuehrt bei FlightScene zum "verschwinden" der Scenery.
            //einfach kein parent? Das waere doch sinnig. Ob das in Unity schadet?
            // der Aufrufer FlightScene Navigator muss aber den richtigen parent setzen. Das ist bei anderen auch wichtig. Aber im Zweifel Scene.world
            // ist doch besser als null, denn schiesslich ist das meine root node.
            node.getTransform().setParent(Scene.getWorld().getTransform());
            //node.getTransform().setParent(null);
        }
        if (lastNearView!=null){
            lastNearView.disable();
        }
        if (getNearView() != null) {
            NearView nearView = getNearView();
            if (parent != null) {
                nearView.enable(posrot);
            }
            lastNearView=nearView;
        }
        return posrot;
    }

    public String getObserverName() {
        return observer.getName();
    }
}

class TeleportPointList {
    public List<TeleportPoint> point = new ArrayList<TeleportPoint>();
    public int index = 0;

    public void addPosition(String label, SceneNode parent, LocalTransform posrot, String targetEntity, NearView nearView) {

        //this.label.add(label);
        point.add(new TeleportPoint(posrot, label, parent, targetEntity, nearView));
        //this.parent.add(parent);
    }

    public int step(boolean forward) {
        if (point.size() > 0) {
            if (forward) {
                if (++index >= point.size())
                    index = 0;
            } else {
                if (--index < 0)
                    index = point.size() - 1;
            }
            return index;
        }
        return -1;
    }
}

class TeleportPoint {
    LocalTransform point;
    String label;
    SceneNode parent;
    //hier eine EcsEntity Referenz aufzunehmen ist eine starke Dependency. Darum nur der Name.
    /*EcsEntity*/ String targetEntity;
    NearView nearView;

    TeleportPoint(LocalTransform point, String label, SceneNode parent, String targetEntity, NearView nearView) {
        this.point = point;
        this.label = label;
        this.parent = parent;
        this.targetEntity = targetEntity;
        this.nearView = nearView;
    }
}
