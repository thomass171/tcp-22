package de.yard.threed.core.platform;

/**
 * analog GWT JSONObject
 */
public interface NativeJsonObject {
    NativeJsonValue	get(String key);

    /**
     * Helper. liefert -1, wenn es das Tag nicht gibt.
     * @return
     */
    int getInt(String key);

    /**
     * Helper. liefert null, wenn es das Tag nicht gibt.
     * @return
     */
    String getString(String key);
}
