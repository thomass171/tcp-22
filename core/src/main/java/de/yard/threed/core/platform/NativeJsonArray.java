package de.yard.threed.core.platform;

/**
 * analog GWT JSONValue
 */
public interface NativeJsonArray extends NativeJsonValue {
    NativeJsonValue	get(int index);
    int size(); 
}
