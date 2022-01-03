package de.yard.threed.traffic.config;

/**
 * Created on 09.01.19.
 */
public class ConfigAttributeFilter {
    public String attribute, value;
    //wie ist der Filter zu bewerten, wenn es das Attribut nicht gibt.
    public boolean missingattributecomplies;

    public ConfigAttributeFilter(String attribute, String value, boolean missingattributecomplies) {
        this.attribute = attribute;
        this.value = value;
        this.missingattributecomplies = missingattributecomplies;
    }
}
