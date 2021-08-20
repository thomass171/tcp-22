package de.yard.threed.javacommon;

import de.yard.threed.core.platform.Log;
import org.apache.log4j.Logger;

/**
 * Logimplementierung ueber log4j.
 * 
 * Created by thomass on 05.06.15.
 */
public class JALog implements Log {
    Logger logger;

    public JALog(Class clazz) {
        logger = Logger.getLogger(clazz);
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String msg, Exception e) {
        logger.error(msg,e);
    }

    @Override
    public void warn(String msg, Exception e) {
        logger.error(msg,e);
    }
}
