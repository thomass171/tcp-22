package de.yard.threed.engine;

import java.util.HashMap;

/**
 * Dasselbe Material kann unter verschiedenen Namen auftauchen (z.B. bei FG Road und Freeway)
 * 2.5.19: Aber aknn man sicher sein, dass es wirklich dasselbe ist? Z.B (un/flat)shaded.
 * Eigentlich ist es doch ein zugrosses Risiko im Vergleich zum Nutzen, oder?
 * Naja, mal seh'n. Bei FG vielleicht sinnvoll. Aber dann muessen mehr Kriterien in den Key.
 *
 *
 * Created by thomass on 22.02.16.
 */
@Deprecated
public class MaterialPool extends HashMap<String, Material> {

}
