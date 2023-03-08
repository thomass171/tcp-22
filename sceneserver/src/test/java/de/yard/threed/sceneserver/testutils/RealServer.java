package de.yard.threed.sceneserver.testutils;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.javanative.JavaUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 */
@Slf4j
public class RealServer {

    public static Process startRealServer(String gridname) throws Exception {

        Process serverProcess;

        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("initialMaze", gridname);

        List<String> args = new ArrayList<>();
        args.add("--throttle=100");
        args.add("--initialMaze=" + gridname);
        args.add("--scene=de.yard.threed.maze.MazeScene");
        // By the way of setting the class path, the class path from the IDE is used, so latest changes apply without
        // running an external maven build.
        serverProcess = JavaUtil.startJavaProcess("de.yard.threed.sceneserver.Main", new ArrayList(), System.getProperty("java.class.path"), args);

        // give server time. TODO check server is ready
        log.debug("Waiting for real server to settle");
        Thread.sleep(5000);

        return serverProcess;
    }


}
