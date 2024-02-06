package de.yard.threed.core.platform;


/**
 * Created by thomass on 20.04.15.
 */
public interface Log {
    
    void trace(String msg);

    void debug(String msg);

    void info(String msg);

    void warn(String msg);

    void error(String msg);

    void error(String msg, java.lang.Exception e);

    void warn(String msg, java.lang.Exception e);

}
