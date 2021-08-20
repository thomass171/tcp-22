package de.yard.threed.engine.ecs;

import de.yard.threed.core.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomass on 28.11.16.
 */
public class EcsGroup {
    // 9.5.17: Hier die Enttty vorzuhalten, könnte konzeptionell unsauber sein. Ist aber praktisch.
    // 6.4.18: nur private, weil entity jetzt direkt im update mit uebergeben wird.
    private EcsEntity entity;
    public List<EcsComponent> cl = new ArrayList<EcsComponent>();
    private static List<String> knowngroups = new ArrayList<String>();
    private static int uniqueid = 1;
    // Eindeutige Id einer Group Instanz. Unterschiedlich in jeder Entity.
    public int id = uniqueid++;
    public boolean inited = false;

    /* public  GraphVisualizer visualizer;
    public GraphPosition position;
    public Transform mover,observer;
    
    
    

    public EcsGroup(Transform mover, Transform observer, GraphVisualizer visualizer, GraphPosition position) {
        this.mover = mover;
        this.observer = observer;
        this.visualizer=visualizer;
        this.position = position;
    }*/

  /*9.5.17   public EcsGroup(EcsComponent c) {
        cl.add(c);
    }*/

    public EcsGroup(List<EcsComponent> cl,EcsEntity e) {
        this.cl = cl;
        this.entity = e;
    }

    public EcsGroup() {

    }

    public void init() {

    }

    public void add(EcsComponent c) {
        cl.add(c);
    }

    /**
     * component order matters!
     *
     * @param components
     * @return
     */
    public static Map<String, EcsGroup> getMatchingGroups(List<EcsComponent> components,EcsEntity e) {
        Map<String, EcsGroup> groups = new HashMap<String, EcsGroup>();

        for (String groupid : knowngroups) {
            List<EcsComponent> matching;
            if ((matching = matchesgroupid(components, groupid)) != null) {
                groups.put(groupid, new EcsGroup(matching,e));
            }
        }
        return groups;
    }

    private static List<EcsComponent> matchesgroupid(List<EcsComponent> components, String groupid) {
        List<EcsComponent> group = new ArrayList<EcsComponent>();

        String[] cname = StringUtils.split(groupid, ",");
        for (int i = 0; i < cname.length; i++) {
            int index;
            if ((index = indexOfComponent(cname[i], components)) != -1) {
                group.add(components.get(index));
            } else {
                return null;
            }
        }
        return group;
    }

    private static int indexOfComponent(String s, List<EcsComponent> components) {
        for (int i = 0; i < components.size(); i++) {
            if (components.get(i).getTag().equals(s)) {
                return i;
            }
        }
        return -1;
    }

    public static String registerGroup(String[] componenttag) {
        String groupid = "";
        for (String s : componenttag) {
            if ((StringUtils.length(groupid) > 0)){
                groupid +=",";
            }
            groupid +=  s;
        }
        knowngroups.add(groupid);
        return groupid;
    }

    public boolean isInited(EcsGroup group) {
        // Nicht ganz sauber, aber das Flag muss ja in allen identisch sein.
        //return cl.get(0).isInited();
        return inited;
    }
    
    @Override
    public  String toString(){
        return "EcsGroup(id="+id+","+cl.size()+" comps)";
    }
}
