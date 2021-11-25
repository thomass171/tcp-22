package de.yard.threed.graph;

import de.yard.threed.core.Vector3;

/**
 * Definition of an arc 3D segment from direction ex to direction ey with center and radius.
 * Created by thomass on 24.05.17.
 * Dies hier sind die daten für einen Innen/Aussenkreis im Schenkel. Es gibt auch GraphArc zur reinen Beschreibung des Bogens.
 */
public class GraphArcParameter {
    public Vector3 arccenter, arcbeginloc, v2, crossproduct;
    public double radius, distancefromintersection, beta;
    public GraphArc arc;

    public GraphArcParameter(Vector3 arccenter, double radius, double distancefromintersection, Vector3 arcbeginloc, double beta, Vector3 v2, Vector3 crossproduct, Vector3 ex, Vector3 n) {
        this.arccenter = arccenter;
        this.arcbeginloc = arcbeginloc;
        this.radius = radius;
        this.distancefromintersection = distancefromintersection;
        this.beta = beta;
        this.v2 = v2;
        this.crossproduct = crossproduct;
        arc=new GraphArc(arccenter,radius,ex,n,beta);
    }

    
}
