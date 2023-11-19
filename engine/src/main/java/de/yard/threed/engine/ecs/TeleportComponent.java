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
    private ViewpointList teleportlist = new ViewpointList();
    public static String TAG = "TeleportComponent";
    private SceneNode observer;
    public boolean needsupdate = false;
    private NearView lastNearView = null;

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

    public void addPosition(String label, Transform parent, LocalTransform posrot) {
        teleportlist.addEntry(label, posrot, parent);
        // 16.2.22 make the first entry the inital one
        if (teleportlist.size() == 1) {
            teleportlist.setIndex(0);
        }
    }

    public void addPosition(String label, Transform parent, LocalTransform posrot, String targetEntity, NearView nearView) {
        teleportlist.addEntry(label, posrot, parent, targetEntity, nearView);
    }

    public static TeleportComponent getTeleportComponent(EcsEntity e) {
        TeleportComponent gmc = (TeleportComponent) e.getComponent(TeleportComponent.TAG);
        return gmc;
    }

    public void removePosition(String label) {
        teleportlist.removePosition(label);
    }

    public int findPoint(String label) {
        return teleportlist.findPoint(label);
    }

    public void setPosition(int i, Vector3 offset) {
        teleportlist.setPosition(i, offset);
        needsupdate = true;
    }

    /**
     * Might return null when there isType no point defined (yet).
     *
     * @return
     */
    public LocalTransform getPosRot() {
        return teleportlist.getTransform();
    }

    public Transform getParent() {
        return teleportlist.getParent();
    }

    public NearView getNearView() {
        return teleportlist.getNearView();
    }

    public int step(boolean forward) {
        int newindex = teleportlist.step(forward);
        needsupdate = true;
        //logger.debug("newindex="+newindex+"("+points.point.get(newindex).label+") of "+points.point.size()+":"+points.point.get(newindex).point);
        return newindex;
    }

    /**
     * Set position programmatically.
     * 26.10.18
     *
     * @param index
     */
    public void stepTo(int index) {
        //TODO check range
        teleportlist.setIndex(index);
        needsupdate = true;
    }

    public int getPointCount() {
        return teleportlist.size();
    }

    public String getPointLabel(int index) {
        return teleportlist.getLabel(index);
    }

    public void setIndex(int index) {
        teleportlist.setIndex(index);
    }

    public int getIndex() {
        return teleportlist.getIndex();
    }

/*
    public int getIndex() {
        return teleportlist.index;
    }*/

    public String getTargetEntity() {
        return teleportlist.getTargetEntity();
    }

    /**
     * 24.10.19: Aus dem StepController jetzt mal fuer Teleport hierhin kopiert und angepasst/vereinfacht.
     */
    public LocalTransform setNewStep(/*SceneNode node, Vector3 position, Quaternion rotation,*/ boolean useanimation) {
        SceneNode node = observer;
        LocalTransform posrot = teleportlist.getTransform();
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
        Transform parent = null;
        if (teleportlist.getParent() != null) {
            parent = teleportlist.getParent();
        }

        if (parent != null) {
            node.getTransform().setParent(parent);
        } else {
            //Das mit world ist doof. 21.12.17: Und fuehrt bei FlightScene zum "verschwinden" der Scenery.
            //einfach kein parent? Das waere doch sinnig. Ob das in Unity schadet?
            // der Aufrufer FlightScene Navigator muss aber den richtigen parent setzen. Das ist bei anderen auch wichtig. Aber im Zweifel Scene.world
            // ist doch besser als null, denn schiesslich ist das meine root node.
            node.getTransform().setParent(Scene.getWorld().getTransform());
            //node.getTransform().setParent(null);
        }
        if (lastNearView != null) {
            lastNearView.disable();
        }
        if (teleportlist.getNearView() != null) {
            NearView nearView = teleportlist.getNearView();
            if (parent != null) {
                nearView.enable(posrot);
            }
            lastNearView = nearView;
        }
        return posrot;
    }

    public String getObserverName() {
        return observer.getName();
    }
}

