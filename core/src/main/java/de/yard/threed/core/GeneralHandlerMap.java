package de.yard.threed.core;

import java.util.HashMap;
import java.util.Map;

public class GeneralHandlerMap<T> {

    Map<T,GeneralHandler> map = new HashMap<T,GeneralHandler>();

    public void addHandler(T key, GeneralHandler handler){
        map.put(key, handler);
    }
}
