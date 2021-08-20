package de.yard.threed.core.testutil;

import de.yard.threed.core.platform.Log;

/**
 * Logger to be used in (unit) tests.
 * TODO 8.4.21: Even though in 'core' it should be possible to use sl4j.
 *
 * Created by thomass on 21.03.16.
 */
public class TestLogger implements Log {
    String name;
    public TestLogger(String name) {
        this.name = name;
    }

    @Override
    public void debug(String msg) {
        dolog("DEBUG",msg);
    }

    @Override
    public void info(String msg) {
        dolog("INFO",msg);
    }

    @Override
    public void warn(String msg) {
        dolog("WARN",msg);
    }

    @Override
    public void error(String msg) {
        dolog("ERROR",msg);
    }

    @Override
    public void error(String msg, Exception e) {
        error(msg);
        e.printStackTrace(System.out);
    }

    @Override
    public void warn(String msg, Exception e) {
        // das scheint noch nicht das wahre
        warn(msg+e.toString());
    }
    
    private void dolog(String level, String msg){
        System.out.println(name+":"+level+" "+msg);
    }
}
