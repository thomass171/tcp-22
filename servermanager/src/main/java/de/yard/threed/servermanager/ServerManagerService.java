package de.yard.threed.servermanager;

import de.yard.threed.javanative.JavaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Has its own baseport management to make sure a new server doesn't use ports already in use.
 */
@Service
@Slf4j
public class ServerManagerService {

    public static final String STATE_RUNNING = "running";
    public static final String STATE_TERMINATED = "terminated";
    public static final String STATE_ABORTED = "aborted";

    // Ordered desc by age
    List<ServerInstance> serverInstances = new Vector();
    Map<Integer, Process> processMap = new TreeMap<>();

    @Value("${servermanager.lib:}")
    private String servermanagerLib;

    @Value("${servermanager.childmainclass:}")
    private String servermanagerChildMainClass;

    AtomicInteger idInteger = new AtomicInteger(1);

    public ServerInstance startServer(String scenename, Integer baseport, Map<String, String> argMap) {

        int port;
        if (baseport != null) {
            port = baseport;
        } else {
            port = findUnusedBaseport();
        }

        Process process;
        try {
            process = startServerInstance(scenename, port, argMap);
        } catch (Exception e) {
            e.printStackTrace();
            // trigger HTTP code 500
            throw new RuntimeException("couldn't launch server");
        }
        // Wait for Java 9 to get pid from process. So long, but not possible
        /*if (process instanceof java.lang.UNIXProcess){

        }*/
        int id = idInteger.addAndGet(1);
        ServerInstance si = new ServerInstance(id, -1, OffsetDateTime.now(), System.currentTimeMillis(), scenename, argMap, port, STATE_RUNNING, 0);
        serverInstances.add(0, si);
        processMap.put(id, process);
        if (!process.isAlive()) {
            log.warn("process no longer alive");
            si.setState(STATE_ABORTED);
        }

        log.debug("Started process with pid?");
        return si;
    }

    /**
     * make it a single object for more visual json representation
     * The latest instances are at the beginning of the list!
     */
    public ServerInstanceList getServer() {
        ServerInstanceList l = new ServerInstanceList();
        l.setServerInstanceList(serverInstances);
        return l;
    }

    public void stopServer(int id) {
        log.debug("Stopping {}", id);
        for (ServerInstance si : serverInstances) {
            if (id == -1 || si.getId() == id) {
                log.debug("Stopping " + id);
                JavaUtil.stopProcess(processMap.get(id));
                si.setState(STATE_TERMINATED);
            }
        }
    }

    public void stopAllServerAndClean() {
        stopServer(-1);
        processMap = new TreeMap<>();
        serverInstances = new Vector();
    }

    /**
     * baseport is mandatory to know which ports are in use.
     */
    private Process startServerInstance(String scenename, int baseport, Map<String, String> argMap) throws Exception {

        Process serverProcess;

        log.debug("Working Directory = " + System.getProperty("user.dir"));

        //HashMap<String, String> properties = new HashMap<String, String>();
        //properties.put("initialMaze", gridname);

        List<String> args = new ArrayList<>();
        args.add("--throttle=100");
        //args.add("--initialMaze=" + gridname);
        args.add("--scene=" + scenename);
        args.add("--baseport=" + baseport);
        for (String arg : argMap.keySet()) {
            args.add("--" + arg + "=" + argMap.get(arg));
        }
//        Class clazz = Class.forName(servermanagerChildMainClass);
        log.debug("servermanagerLib={}", servermanagerLib);
        String classPath = System.getProperty("user.dir") + "/" + servermanagerLib + "/*";
        log.debug("setting classPath={}", classPath);

        List<String> jvmArgs = new ArrayList<>();
        // spawned process will probably use log4j2.xml from
        // XmlConfiguration[location=jar:file:/Users/thomas/Projekte/tcp-22/sceneserver/target/lib/module-engine-1.0.0-SNAPSHOT-tests.jar!/log4j2.xml]
        // only needed for debugging. jvmArgs.add("-Dlog4j2.debug=true");

        serverProcess = JavaUtil.startJavaProcess(servermanagerChildMainClass, jvmArgs, classPath, args);

        // give server time. TODO check server is ready
        log.debug("Waiting for server to settle");
        Thread.sleep(5000);

        return serverProcess;
    }

    private Integer findUnusedBaseport() {
        int port = 5890;

        do {
            boolean used = false;
            for (ServerInstance si : serverInstances) {
                if (si.getState().equals(STATE_RUNNING) && si.baseport == port) {
                    used = true;
                }
            }
            if (!used) {
                return port;
            }
            port += 2;
        } while (port < 5890 + 10);
        log.warn("No unused port found");
        return null;
    }

    @Scheduled(fixedDelay = 1000)
    public void checkAlive() {
        //log.info("isAlive");
        for (ServerInstance si : serverInstances) {
            if (si.getState().equals(STATE_RUNNING)) {
                if (processMap.get(si.getId()).isAlive()) {
                    si.setUpTime((int) ((System.currentTimeMillis() - si.getStartedMillis()) / 1000));
                } else {
                    si.setState(STATE_ABORTED);
                }
            }
        }
    }
}
