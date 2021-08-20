package de.yard.threed.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Packet {

    //Only LinkedHashMap isType sorted, but GWT,C#??
    Map<String, String> map = new HashMap<String, String>();

    public Packet(){

    }

    /*public static Event buildEvent(Packet packet) {
        return null;
    }*/

    public static Packet buildFromBlock(List<String> block) {
        if (block==null){
            return null;
        }
        Packet packet=new Packet();
        for (String s:block){
            String[] parts = StringUtils.split(s,"=");
            packet.add(parts[0],parts[1]);
        }
        return packet;
    }

    public void add(String key, String value) {
        map.put(key, value);
    }

    public List<String> getData() {
        List<String> data = new ArrayList<String>();
        for (String key : map.keySet()) {
            data.add(key + "=" + map.get(key));
        }
        return data;
    }

    public String getValue(String key) {
        return map.get(key);
    }

    /**
     * add well prepared key=value lines
     */
    public void add( Map<String, String> p ) {
        //C# has no putAll()
        for (String key:p.keySet()){
            map.put(key,p.get(key));
        }
    }
}
