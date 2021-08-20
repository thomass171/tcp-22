package de.yard.threed.engine.platform.common;

import de.yard.threed.core.Color;


/**
 * Created by thomass on 09.03.16.
 */
public class Settings {
    public Integer targetframerate = null;
    public Integer aasamples = null;
    //16.4.21: VR besser über cmd line setzen um dann auch control menu zu disablen
    //Ach was, erstmal so lassen. @Deprecate
    public Boolean vrready = null;
    // Der FOV ist in Grad. Vertical(y), so verwendens ThreeJS, JME und Unity.
    //60 fuer Android maze
    public static float defaultfov = 45;//60/*45*/;
    //ThreeJs Beispiele haben haefig 0.1 als near Wert
    //JME hat 1 als Default
    public static float defaultnear = 0.1f;
    //Wegen Flusi defaultfar 100000000 statt 1000 (TODO ueber Scene Settings)
    //23.3.17: Eine für Flusi geeignete grosse far plane verhindert in Unity die Schattendarstellung
    public static float defaultfar = 100000000;
    // Values for default camera
    public Float fov, near, far;

    // leicht hellblauer Hintergrund als Default. Dann kann man schwarze (fehlerhafte Shader) model
    // besser erkennen.
    public static Color backgroundColor = new Color(0.5f, 0.5f, 1);

    // 2.3.16: Der Default muss auf true stehen, sonst laufen die beiden einfachen LWJGL Beispiele nicht.
    // Und ueberhaupt: alles andere ist nicht mehr zeitgemaess.
    public static boolean usevertexarrays = true;
    // siehe DVK
    public static boolean linear = true;
    // speziell fuer JME
    public boolean pngcacheenabled = true;
    /**
     * {@link EngineHelper} Default isType to use platform default
     */
    public Integer minfilter = null, magfilter = null;
    // number of samples?
    public Integer anisotropicFilter = null;
}
