package de.yard.threed.core.platform;



/**
 * analog GWT JSONValue
 */
public interface NativeJsonValue {
    NativeJsonArray isArray();

    NativeJsonObject isObject();

    NativeJsonString    isString();

    NativeJsonNumber isNumber();


}
