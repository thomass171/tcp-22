package de.yard.threed.java2cs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomass on 01.04.16.
 */
public class KnownInterfaces {
    Map<String, KnownInterface> knowninterfaces = new HashMap<String,KnownInterface>();

    public void add(String ifx, String[] methods) {
        knowninterfaces.put(ifx, new KnownInterface(ifx,Arrays.asList(methods)));
    }

    /**
     * for FunctionalInterface
     * 21.6.20
     *
     */
    public void add(String ifx, String method) {
        knowninterfaces.put(ifx, new KnownInterface(ifx,method));
    }

    public boolean isFunctionalInterface(String text) {
        for (KnownInterface ki : knowninterfaces.values()){
            if (ki.isFunctionalInterface&&ki.methods.get(0).equals(text)){
                return true;
            }
        }
        return false;
    }

    class KnownInterface    {
        String name;
        List<String> methods;
        boolean isFunctionalInterface= false;

        public KnownInterface(String ifx, List<String> methods) {
            this.name=ifx;
            this.methods=methods;
            isFunctionalInterface= false;
        }

        public KnownInterface(String ifx, String method) {
            this.name=ifx;
            this.methods=  Arrays.asList(method);
            isFunctionalInterface= true;
        }
    }
}
