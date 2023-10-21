package de.yard.threed.javacommon;

import java.util.HashMap;

/**
 * Setup for Java platforms like JME and Homebrew, but not for Unity and Browser, which have their own setup.
 * <p>
 * All settings can be overwritten via command line args (later in main).
 * <p>
 */
public class Setup {

    // optional red box in camera view (for debugging camera space)
    private static String sceneExtension0 = "{\n" +
            "  \"objects\": [\n" +
            "    {\n" +
            "      \"name\": \"extension red box\",\n" +
            "      \"geometry\": \"primitive: box\",\n" +
            "      \"material\": \"color: red\",\n" +
            "      \"position\": \"0 0 -3\",\n" +
            "      \"rotation\": \"0 0 0\",\n" +
            "      \"scale\": \"0.1 0.1 0.1\",\n" +
            "      \"parent\": \"deferred-camera\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    static String mazeshost = "https://ubuntu-server.udehlavj1efjeuqv.myfritz.net";

    public static HashMap<String, String> setUp() {
        HashMap<String, String> properties = new HashMap<String, String>();

        properties.put("enableUsermode", "false");
        properties.put("visualizeTrack", "true");
        // VR control panel visible for debugging 25.1.23:TODO check count values not visible?
        //properties.put("enableHud", "true");

        // emulateVR for testing VR panel outside VR via mouse move/click (mouseclick is right trigger, shift pressed for left trigger)
        // After fix of webxr floor handling yoffsetVR can have a 'real' height (above avatar).
        boolean emulateVR = false;
        if (emulateVR) {
            emulateVR(properties);
        }

        //properties.put("argv.initialVehicle", "c172p");
        //Evtl. Bluebird statt c172p wegen sonst verdecktem menu.
        //properties.put("argv.initialVehicle", "bluebird");
        //properties.put("argv.basename","B55-B477");
        //properties.put("argv.basename","B55-B477-small");
        // properties.put("argv.basename","EDDK");
        //properties.put("argv.basename", "TestData");
        //properties.put("argv.basename", "Zieverich-Sued");
        //properties.put("argv.basename", "Desdorf");
        //properties.put("argv.basename","3056443");
        //properties.put("argv.enableFPC", "true");
        //18.11.19: NearView geht in VR eh nicht, darum damit üblicherweise auch sonst nicht arbeiten.
        //properties.put("argv.enableNearView", "true");
        properties.put("initialMaze", "skbn/SokobanWikipedia.txt");
        properties.put("initialMaze", "maze/Maze-P-Simple.txt");
        //properties.put("initialMaze", "maze/Maze-D-80x25.txt");
        //properties.put("initialMaze", mazeshost + "/mazes/mazes/1");
        //properties.put("argv.initialMaze", "maze/Maze-P-60x20.txt");
        //properties.put("argv.initialMaze", "maze/Maze-M-30x20.txt");
        //properties.put("argv.initialMaze","maze/Area15x10.txt");
        //properties.put("argv.initialMaze","skbn/DavidJoffe.txt:1");
        //properties.put("argv.sceneExtension0",sceneExtension0);

        //properties.put("server", "localhost:5890");
        //properties.put("server", "192.168.98.151:5890");

        //properties.put("theme", "dungeon");
        //properties.put("theme", "dungeon-art");
        //properties.put("teamSize", "1");

        //properties.put("argv.vehiclelist","GenericRoad");

        //13.3.19: Scene doch mal wieder aus Property, um nicht so viele Run Configurations zu haben. Nur, wenn sie
        //nicht schon von aussen gesetzt ist.
        //6.3.23 if (System.getProperty("scene") == null) {

        properties.put("scene", "de.yard.threed.engine.apps.reference.ReferenceScene");
        //System.setProperty("scene", "de.yard.threed.engine.apps.ModelPreviewScene");
        //properties.put("scene", "de.yard.threed.maze.MazeScene");
        //System.setProperty("scene", "de.yard.threed.engine.apps.vr.VrScene");
        //System.setProperty("scene", "de.yard.threed.apps.DisplayClient");
       //properties.put("scene", "de.yard.threed.engine.apps.FirstPersonScene");

        boolean testFireTargetMarker = false;
        if (testFireTargetMarker) {
            // only in VR and mode 1
            properties.put("scene", "de.yard.threed.maze.MazeScene");
            properties.put("initialMaze", "maze/Maze-P-Simple.txt");
            properties.put("vrFireMode", "1");
            emulateVR(properties);
        }
        boolean wayland = false;
        if (wayland) {
            properties.put("basename", "traffic:tiles/Wayland.xml");
            properties.put("enableAutomove", "true");
            properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        }
        boolean demo = false;
        if (demo) {
            properties.put("basename", "traffic:tiles/Demo.xml");
            // automove is enabled in Demo.xml.
            properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        }


        return properties;
    }

    private static void emulateVR(HashMap<String, String> properties) {
        properties.put("emulateVR", "true");
        // 1.3 is only good for maze, VrScene needs less (eg 0.3), or better 0 like BasicTravelScene. App does the rest.
        properties.put("offsetVR", "0,0,0");
    }

    // Now in Maze-D-80x25.txt
    static String genV1 = "";
}
