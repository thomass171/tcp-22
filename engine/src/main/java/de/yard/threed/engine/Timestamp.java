package de.yard.threed.engine;

import de.yard.threed.core.platform.Platform;

/**
 * Created by thomass on 20.02.17.
 */
public class Timestamp {
    long timestamp;
    
    public Timestamp(){
        timestamp = Platform.getInstance().currentTimeMillis();
    }
    
    public String getTookLogString(String tag) {
        long elapsed = Platform.getInstance().currentTimeMillis() - timestamp;
        return tag + "took " + elapsed + " ms.";
    }
}
