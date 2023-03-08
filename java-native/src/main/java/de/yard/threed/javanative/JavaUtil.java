package de.yard.threed.javanative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JavaUtil {

    static Logger logger = LoggerFactory.getLogger(JavaUtil.class.getName());

    public static void sleepMs(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Launch a standalone Java process. From "https://lankydan.dev/running-a-java-class-as-a-subprocess".
     *
     */
    public static Process startJavaProcess(String className, List<String> jvmArgs, String classpath, List<String> args) throws Exception {

        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        //String classpath = System.getProperty("java.class.path");
        //String className = clazz.getName();

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

    public static void stopProcess(Process serverProcess) {
        if (serverProcess != null) {
            do {
                serverProcess.destroy();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.debug("Waiting for process to terminate");
            } while (serverProcess.isAlive());
        }
    }
}
