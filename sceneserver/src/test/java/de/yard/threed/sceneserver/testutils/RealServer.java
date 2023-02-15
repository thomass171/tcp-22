package de.yard.threed.sceneserver.testutils;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
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
        serverProcess = execJavaProcess(de.yard.threed.sceneserver.Main.class, new ArrayList(), args);

        // give server time. TODO check server is ready
        log.debug("Waiting for real server to settle");
        Thread.sleep(5000);

        return serverProcess;
    }

    public static void stopRealServer(Process serverProcess) {
        if (serverProcess != null) {
            do {
                serverProcess.destroy();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.debug("Waiting for server process to terminate");
            } while (serverProcess.isAlive());
        }
    }

    /**
     * Lauch a standalone Java process. From "https://lankydan.dev/running-a-java-class-as-a-subprocess".
     *
     * By the way of setting the class path, the class path from the IDE is used, so latest changes apply without
     * running an external maven build.
     */
    public static Process execJavaProcess(Class clazz, List<String> jvmArgs, List<String> args) throws Exception {

        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = clazz.getName();

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        command.addAll(args);

        // Output redirected to stdout will cause '[WARNING] Corrupted STDOUT by directly writing to native stream in forked JVM...'
        // in surefire plugin. But its the only simple way to get the output, which really is useful. No easy way to use a logger.
        ProcessBuilder builder = new ProcessBuilder(command);
        //Process process = builder.inheritIO().start();

        Process process = builder
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                //.redirectInput(ProcessBuilder.Redirect.INHERIT)
                //.redirectOutput(ProcessBuilder.Redirect.INHERIT)
                //.redirectError(ProcessBuilder.Redirect.INHERIT)
                .start();
        return process;
    }
}
