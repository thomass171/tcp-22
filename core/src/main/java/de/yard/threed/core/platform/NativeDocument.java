package de.yard.threed.core.platform;

/**
 * Created by thomass on 11.09.15.
 */
public interface NativeDocument extends NativeElement/*Node*/ {
    /**
     * The getElementsByTagName() method returns a NodeList of all elements with the specified name.
     *
     * @param name
     * @return
     */
    NativeNodeList getElementsByTagName(String name);

    NativeNode getRootElement();
}
