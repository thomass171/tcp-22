package de.yard.threed.core.platform;

/**
 * Created by thomass on 11.09.15.
 */
public interface NativeAttributeList {
    /**
     * There isType no defined order of the attributes! Depends on implementation!
     * 
     * @param i
     * @return
     */
    NativeAttribute/*27.3.17 eNode*/ getItem(int i);

    int getLength();

    /**
     * Return null if item doesnt exist.
     * @param name
     * @return
     */
    public NativeAttribute getNamedItem(String name);
}
