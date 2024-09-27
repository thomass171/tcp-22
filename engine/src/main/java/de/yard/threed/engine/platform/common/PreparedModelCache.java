package de.yard.threed.engine.platform.common;

import de.yard.threed.core.loader.PreparedModel;

import java.util.HashMap;
import java.util.Map;

public class PreparedModelCache {
    public Map<String, PreparedModel> cache = new HashMap<String, PreparedModel>();

   public PreparedModel get(String name){
        return cache.get(name);
    }

    public void put(String name, PreparedModel preparedModel){
        cache.put(name,preparedModel);
    }
}
