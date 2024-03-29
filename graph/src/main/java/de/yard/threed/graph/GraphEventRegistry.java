package de.yard.threed.graph;

import de.yard.threed.core.Event;
import de.yard.threed.core.EventType;
import de.yard.threed.core.Payload;


/**
 * Um Events nicht im System zu definieren, weil sie ja auch übergreifend verwendet werden koennten.
 * <p>
 * Created on 01.03.18.
 */
public class GraphEventRegistry {
    /**
     * In irgendweinem Graph wurde ein Path angelegt. Das impliziert auch eine Erweiterung
     * um Nodes z.B. fuer Smoothing oder besondere Bewegungen. Üblicherweise wird der Pfad
     * dann für ein Movement verwendet.
     * Für z.B. Visualisierung.
     */
    public static EventType GRAPH_EVENT_PATHCREATED = EventType.register(3000,"GRAPH_EVENT_PATHCREATED");

    /**
     * Ein Path wurde abgefahren und ist damit obsolet.
     * (graph,path,vehicle)
     */
    public static EventType GRAPH_EVENT_PATHCOMPLETED = EventType.register(3001,"GRAPH_EVENT_PATHCOMPLETED");

    /**
     * Payload ist layernummer. TODO der graph muss da auch rein.
     */
    public static EventType GRAPH_EVENT_LAYERCREATED = EventType.register(3002,"GRAPH_EVENT_LAYERCREATED");
    public static EventType GRAPH_EVENT_LAYERREMOVED = EventType.register(3003,"GRAPH_EVENT_LAYERREMOVED");
}
