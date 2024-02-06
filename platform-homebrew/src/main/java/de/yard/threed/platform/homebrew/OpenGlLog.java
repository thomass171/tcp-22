package de.yard.threed.platform.homebrew;

import de.yard.threed.core.platform.Log;

/**
 * Simple Logimplementierung, weil OpenGl in core liegt geht kein log4j.
 * Das ist aber Kokelores.
 * 
 * Created by thomass on 05.02.17.
 */
public class OpenGlLog implements Log {
    
    public OpenGlLog() {
     }

    @Override
    public void trace(String msg) {
        System.out.println(msg);
    }

    @Override
    public void debug(String msg) {
        System.out.println(msg);
    }

    @Override
    public void info(String msg) {
        System.out.println(msg);
    }

    @Override
    public void warn(String msg) {
        System.out.println(msg);
    }

    @Override
    public void error(String msg) {
        System.out.println(msg);
    }

    @Override
    public void error(String msg, Exception e) {
        System.out.println(msg);
        e.printStackTrace(System.out);
        
    }

    @Override
    public void warn(String msg, Exception e) {
        System.out.println(msg);
    }
}
