package de.yard.threed.engine;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.util.NearView;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ViewpointList {
    Log logger = Platform.getInstance().getLog(ViewpointList.class);

    private List<ViewpointListEntry> points = new ArrayList<ViewpointListEntry>();
    private int index = -1;

    public ViewpointList() {
    }

    public void addEntry(Vector3 position, Quaternion rot) {
        addEntry("", new LocalTransform(position, rot), null);
    }

    public void addEntry(Vector3 position, Quaternion rotation, Transform parent) {
        addEntry(new LocalTransform(position, rotation), parent);
    }

    public void addEntry(LocalTransform posrot, Transform parent) {
        addEntry("", posrot, parent);
    }

    public void addEntry(String label, LocalTransform posrot, Transform parent) {
        points.add(new ViewpointListEntry(label, posrot, parent, null, null));
    }

    public void addEntry(String label, LocalTransform posrot, Transform parent, String targetEntity, NearView nearView) {
        points.add(new ViewpointListEntry(label, posrot, parent, targetEntity, nearView));
    }

    public void addEntryForLookat(Vector3 position, Vector3 lookat) {
        addEntry(position, MathUtil2.buildLookRotation(lookat.subtract(position).negate(), new Vector3(0, 1, 0)));
    }


    public int step(boolean forward) {

        if (points.size() > 0) {
            if (forward) {
                if (++index >= points.size())
                    index = 0;
            } else {
                if (--index < 0)
                    index = points.size() - 1;
            }

            return index;
        }
        return -1;
    }

    public LocalTransform stepTo(int pos) {
        index = pos;
        return points.get(index).transform;
    }

    /**
     * Might return null when there is no point defined (yet).
     *
     * @return
     */
    public LocalTransform getTransform() {
        if (index<0||index >= points.size()) {
            return null;
        }
        return points.get(index).transform;
    }

    public Transform getParent() {
        return points.get(index).parent;
    }

    public String getLabel() {
        return points.get(index).label;
    }

    public String getLabel(int i) {
        return points.get(i).label;
    }

    public NearView getNearView() {
        return points.get(index).nearView;
    }

    public int size() {
        return points.size();
    }

    public int findPoint(String label) {
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).label.equals(label)) {
                return i;
            }
        }
        return -1;
    }

    public void removePosition(String label) {
        int index = findPoint(label);
        if (index != -1) {
            points.remove(index);
            //1.1.20 index reset
            index = 0;
        }
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setPosition(int i, Vector3 pos) {
        points.get(i).transform.position = pos;
    }

    public String getTargetEntity() {
        return points.get(index).targetEntity;
    }

    public int getIndex() {
        return index;
    }
}

class ViewpointListEntry {
    LocalTransform transform;
    String label;
    Transform parent;
    // Optional name of a related entity. A reference to EcsEntity would be a too hard dependency.
    String targetEntity;
    NearView nearView;

    ViewpointListEntry(String label, LocalTransform transform, Transform parent, String targetEntity, NearView nearView) {
        this.transform = transform;
        this.label = label;
        this.parent = parent;
        this.targetEntity = targetEntity;
        this.nearView = nearView;
    }
}