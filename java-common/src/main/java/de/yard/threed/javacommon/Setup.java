package de.yard.threed.javacommon;

import java.util.HashMap;

public class Setup {
    /**
     * Also for other Java platforms like OpenGL.
     *
     */
    public static HashMap<String, String> setUp(String[] args){
        HashMap<String, String> properties = new HashMap<String, String>();
        for (String arg : args) {
            if (arg.contains("=")) {
                String[] part = arg.split("=");
                properties.put("argv." + part[0], part[1]);
            }
        }
        // 7.5.19 args von hier setzen, weil sie besser sichtbar sind als über Idea. Aber nur, wenn sie nicht von aussen rein kamen.
        if (args.length == 0) {
            properties.put("argv.enableUsermode", "false");
            properties.put("argv.visualizeTrack", "true");
            //properties.put("argv.enableHud", "true");

            // emulateVR for testing VR panel outside VR via mouse move/click (while shift pressed)
            //properties.put("argv.emulateVR", "true");

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
            properties.put("argv.initialMaze","skbn/SokobanWikipedia.txt");
            //properties.put("argv.initialMaze","maze/Area15x10.txt");
            //properties.put("argv.initialMaze","skbn/DavidJoffe.txt:1");
        }
        //properties.put("argv.vehiclelist","GenericRoad");

        //13.3.19: Scene doch mal wieder aus Property, um nicht so viele Run Configurations zu haben. Nur, wenn sie
        //nicht schon von aussen gesetzt ist.
        if (System.getProperty("scene") == null) {

            System.setProperty("scene", "de.yard.threed.engine.apps.reference.ReferenceScene");
            //System.setProperty("scene", "de.yard.threed.engine.apps.ModelPreviewScene");
            //System.setProperty("scene", "de.yard.threed.maze.MazeScene");
            System.setProperty("scene", "de.yard.threed.engine.apps.vr.VrScene");
            //System.setProperty("scene", "de.yard.threed.apps.DisplayClient");

            boolean wayland = false;
            if (wayland) {
                properties.put("argv.basename", "traffic:tiles/Wayland.xml");
                properties.put("argv.enableAutomove", "true");
                System.setProperty("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
            }
            boolean demo = false;
            if (demo) {
                properties.put("argv.basename", "traffic:tiles/Demo.xml");
                // GraphMovingComponent.automove is needed.
                // properties.put("argv.enableAutomove", "true");
                System.setProperty("scene", "de.yard.threed.traffic.apps.BasicTravelScene");
            }
        }

        return properties;
    }
}
