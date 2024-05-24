package de.yard.threed.traffic;

import de.yard.threed.core.GeneralHandler;
import de.yard.threed.core.ObjectBuilder;

import java.util.HashMap;
import java.util.Map;

public class BuilderRegistry {

    // ObjectBuilder is the best available delegate currently
    static Map<String, ObjectBuilder<AbstractSceneryBuilder>> registry = new HashMap<String, ObjectBuilder<AbstractSceneryBuilder>>();

    public static AbstractSceneryBuilder buildSceneryBuilder(String name) {
        if (name.equals("MoonSceneryBuilder")) {
            return new MoonSceneryBuilder();
        }
        if (registry.containsKey(name)) {
            return registry.get(name).buildFromString("");
        }
        throw new RuntimeException("unknown builder " + name);
    }

    public static void add(String key, ObjectBuilder handler){
        registry.put(key, handler);
    }
}
