package de.yard.threed.engine.test;

import de.yard.threed.core.Vector2;
import de.yard.threed.core.Util;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.testutil.TestUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Vor allem für Unity. Auch fuer java.lang.
 * <p/>
 * Created by thomass on 06.04.16.
 */
public class JavaUtilTest {
    public void testList() {
        List<String> slist = new ArrayList<String>();
        slist.add("a");
        slist.add("bb");
        int cnt = 0;
        int totallength = 0;
        for (String s : slist) {
            cnt++;
            totallength += StringUtils.length(s);
        }
        TestUtil.assertEquals("cnt", 2, cnt);
        TestUtil.assertEquals("size", 2, slist.size());
        TestUtil.assertEquals("totallength", 3, totallength);
        slist.add("ccc");
        slist.add("dddd");
        TestUtil.assertEquals("size", 4, slist.size());
        List<String> sublist = slist.subList(1, 3);
        TestUtil.assertEquals("sublist.size", 2, sublist.size());
        TestUtil.assertEquals("sublist.0", "bb", sublist.get(0));

        slist.set(1, "bbbbb");
        TestUtil.assertEquals("size", 4, slist.size());
        totallength = 0;
        for (String s : slist) {
            cnt++;
            totallength += StringUtils.length(s);
        }
        TestUtil.assertEquals("totallength", 13, totallength);

        slist.remove(1);
        TestUtil.assertEquals("size", 3, slist.size());
        totallength = 0;
        for (String s : slist) {
            cnt++;
            totallength += StringUtils.length(s);
        }
        TestUtil.assertEquals("totallength", 8, totallength);
    }

    public void testMap() {
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(1, "a");
        map.put(2, "bbc");
        TestUtil.assertEquals("len2", 3, StringUtils.length(map.get(2)));
        TestUtil.assertNull("len2", map.get(33));

        int cnt = 0;
        int totallength = 0;
        for (Integer key : map.keySet()) {
            String s = map.get(key);
            cnt++;
            totallength += StringUtils.length(s);
        }
        TestUtil.assertEquals("cnt", 2, cnt);
        TestUtil.assertEquals("size", 2, map.keySet().size());
        TestUtil.assertEquals("totallength", 4, totallength);
    }

    public void testMap2() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("basetex", "a");
        map.put("2", "bbc");
        TestUtil.assertEquals("basetex", "a", map.get("basetex"));
        map.put("basetex", "b");
        TestUtil.assertEquals("basetex", "b", map.get("basetex"));
        TestUtil.assertNull("basetexxxxxxx",  map.get("basetexxxxxxxx"));
    
        Map<String, Vector2> map3 = new HashMap<String, Vector2>();
        map3.put("basetex", new Vector2(1,2));
        TestUtil.assertTrue("basetex",  map3.get("basetex") != null);
        TestUtil.assertNull("basetexxxxxxx",  map3.get("basetexxxxxxxx"));

    }
    
    public void testStringBuffer() {
        StringBuffer sb = new StringBuffer();
        sb.append("a");
        sb.append("bb");

        TestUtil.assertEquals("size", 3, sb.length());
        TestUtil.assertEquals("toString", "abb", sb.toString());

        sb.append("ccc");
        TestUtil.assertEquals("toString", "abbccc", sb.toString());
        sb.delete(1, 4);
        TestUtil.assertEquals("toString", "acc", sb.toString());

        sb = new StringBuffer("world");
        sb.delete(0, 6);
        TestUtil.assertEquals("toString", "", sb.toString());

    }

    public void testSet() {
        HashMap<Integer, String> ypos = new HashMap<Integer, String>();
        ypos.put(1, "");
        ypos.put(20, "");
        ypos.put(100, "");
        ypos.put(510, "");
        Set<Integer> iset = ypos.keySet();
        TestUtil.assertEquals("cnt", 4, iset.size());
        TestUtil.assertEquals("closest 17", 20, Util.findClosestIntInSet(iset, 17));
        TestUtil.assertEquals("closest 22", 20, Util.findClosestIntInSet(iset, 22));
        int totallength = 0;
        for (Integer s : iset) {
            totallength += (int) s;
        }

        TestUtil.assertEquals("totallength", 631, totallength);

    }
    
    public void testParse(){
        float d = (float) java.lang.Double.parseDouble("0.5f");
        TestUtil.assertEquals("parse double",0.5f,d);
    }
}
