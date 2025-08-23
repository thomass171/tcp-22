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

        // 21.8.25 Property "enableUsermode" removed
        properties.put("visualizeTrack", "true");
        // VR control panel visible for debugging 25.1.23:TODO check count values not visible?
        //properties.put("enableHud", "true");

        // emulateVR for testing VR panel outside VR via mouse move/click (mouseclick is right trigger, shift pressed for left trigger)
        // After fix of webxr floor handling yoffsetVR can have a 'real' height (above avatar).
        boolean emulateVR = false;
        if (emulateVR) {
            emulateVR(properties);
        }

        //properties.put("argv.basename","B55-B477");
        //properties.put("argv.basename","B55-B477-small");
        // properties.put("argv.basename","EDDK");
        //properties.put("argv.basename", "TestData");
        //properties.put("argv.basename", "Zieverich-Sued");
        //properties.put("argv.basename", "Desdorf");
        //properties.put("argv.basename","3056443");
        //properties.put("enableFPC", "true");
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

        properties.put("logging.level.de.yard.threed.engine.ecs.InputToRequestSystem", "DEBUG");
        properties.put("logging.level.de.yard.threed", "DEBUG");

        //13.3.19: Scene doch mal wieder aus Property, um nicht so viele Run Configurations zu haben. Nur, wenn sie
        //nicht schon von aussen gesetzt ist.
        //6.3.23 if (System.getProperty("scene") == null) {

        properties.put("scene", "de.yard.threed.engine.apps.reference.ReferenceScene");
        //properties.put("scene", "de.yard.threed.engine.apps.ModelPreviewScene");
        //properties.put("scene", "de.yard.threed.maze.MazeScene");
        //properties.put("scene", "de.yard.threed.engine.apps.vr.VrScene");
        //properties.put("scene", "de.yard.threed.engine.apps.showroom.ShowroomScene");
        //System.setProperty("scene", "de.yard.threed.apps.DisplayClient");
        //properties.put("scene", "de.yard.threed.engine.apps.FirstPersonScene");
        //properties.put("scene", "de.yard.threed.engine.apps.GalleryScene");

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
            // Default wayland only has a default vehicle (loc) moving around, but no 'initialVehicle'.'mobi' can do free flying
            // while loc is auto moving
            properties.put("initialVehicle", "mobi");
            properties.put("initialLocation", "coordinate:90.0,110.5,76.0");
            //properties.put("enableFPC", "true");
        }
        boolean demo = false;
        if (demo) {
            properties.put("basename", "traffic:tiles/Demo.xml");
            // automove is enabled in Demo.xml.
            properties.put("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
        }
        boolean moon = false;
        if (moon) {
            // loc is too high above ground currently. Probably needs an elevation provider
            properties.put("basename", "traffic:tiles/Moon.xml");
            // initialVehicle needed here different to Wayland where it is part of config
            properties.put("initialVehicle", "loc");
            properties.put("visualizeTrack", "true");

            boolean useRoute = true;
            if (useRoute) {
                properties.put("initialRoute", "wp:50.768,7.1672000->takeoff:50.7692,7.1617000->wp:50.7704,7.1557->wp:50.8176,7.0999->wp:50.8519,7.0921->touchdown:50.8625,7.1317000->wp:50.8662999,7.1443999");
            } else {
                // location like in initialRoute. elevation should be resolved by elevation provider.
                properties.put("initialLocation", "geo:50.768,7.1672000");
                properties.put("initialHeading", "320");
            }
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
