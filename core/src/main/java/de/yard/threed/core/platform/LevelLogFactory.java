package de.yard.threed.core.platform;

import de.yard.threed.core.StringUtils;
import de.yard.threed.core.configuration.Configuration;

import java.util.HashMap;
import java.util.Map;

public class LevelLogFactory implements NativeLogFactory{

    int defaultLevel;
    NativeLogFactory logFactory;
    Map<String, Integer> definitions = new HashMap<>();

    public LevelLogFactory(Configuration configuration, NativeLogFactory logFactory, int defaultLevel) {
        this.logFactory=logFactory;
        this.defaultLevel = defaultLevel;
        String label = "logging.level";
        for (String p : configuration.getProperties()) {
            if (StringUtils.startsWith(p, label)) {

                String value = StringUtils.substringAfter(p, label + ".");
                String sLevel = configuration.getString(p, "");
                int level = parseLevel(sLevel);
                if (level == -1) {
                    throw new RuntimeException("unknown log level:" + sLevel);
                }
                definitions.put(StringUtils.substringBeforeLast(value, "="), level);
            }
        }
    }

    private int parseLevel(String level) {
        if (StringUtils.toLowerCase(level).equals("debug")) {
            return DefaultLog.LEVEL_DEBUG;
        }
        if (StringUtils.toLowerCase(level).equals("info")) {
            return DefaultLog.LEVEL_INFO;
        }
        if (StringUtils.toLowerCase(level).equals("error")) {
            return DefaultLog.LEVEL_ERROR;
        }
        if (StringUtils.toLowerCase(level).equals("warn")) {
            return DefaultLog.LEVEL_WARN;
        }
        return -1;
    }

    public Log getLog(Class clazz) {

        return new DefaultLog(getLevel(clazz.getName()), logFactory.getLog(clazz));
    }

    private int getLevel(String name) {
        int level = defaultLevel;
        String[] parts = StringUtils.split(name, "\\.");
        if (parts.length != 0) {

            String key = "";
            for (int i = 0; i < parts.length; i++) {
                key += ((i == 0) ? "" : ".") + parts[i];
                if (definitions.containsKey(key)) {
                    level = definitions.get(key);
                }
            }
        }
        return level;
    }
}
