package de.yard.threed.core.configuration;


import java.util.HashMap;
import java.util.Map;

/**
 * Also for the query string in a browser app?
 * 5.2.23 Using prefix "argv." appears to be a bad idea meanwhile.
 */
public class ConfigurationByArgs extends Configuration {

    private Map<String, String> argProperties = new HashMap<String, String>();

    /**
     * Use the spring way.
     * StringUtils/platform are not available here.
     */
    public ConfigurationByArgs(String[] args) {
        for (String arg : args) {
            //C#if (arg.StartsWith("--")) {
            //C#String[] p = arg.Substring(2).Split("=");
            if (arg.startsWith("--")) {
                String[] p = arg.substring(2).split("=");
                switch (p.length) {
                    case 2:
                        argProperties.put(p[0], p[1]);
                        break;
                    default:
                        throw new RuntimeException("invalid arg" + arg);
                }
            }
        }
    }

    @Override
    public String getPropertyString(String property) {

        //5.2.23 return  Platform.getInstance().getSystemProperty("argv." + property);
        return argProperties.get(property);
    }
}
