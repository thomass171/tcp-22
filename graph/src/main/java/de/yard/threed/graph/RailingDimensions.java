package de.yard.threed.graph;

/**
 * Created by thomass on 28.11.16.
 */
public class RailingDimensions {
    // Wegen Problemen mit zu kleinem near in Unity darf die trackwidth nicht zu klein werden, z.B. H0. Darum einfach mal von 1 ausgehen.
    //public static float trackwidthH0 = 0.0165f;
    // Dann sind das hier auch alles Meter.
    public static float trackwidth = 1;
    // Der Abstand zwischen zwei parallelen Gleisen.
    static float trackdistance = 4;
    // Bogenradius
    public static float innerarcradius = 30;
    static float outerarcradius = innerarcradius+trackdistance;

    // Der Abzweigungswinkel einer Weiche. Ergibt sich indirekt aus dem trackdistance
    //public static Degree brancharc = new Degree(30);
    // Die Länge des Abzweigungsbogens einer Weiche (als Kantenlänge, nicht echte Bogenlänge)
    public static float branchlen=3;
}
