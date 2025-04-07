package de.yard.threed.graph;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.Vector3;

/**
 * 4.7.24: Now the default rotation provider that derives orientation from edge orientation.
 */
public class DefaultEdgeBasedRotationProvider implements RotationProvider {

    public DefaultEdgeBasedRotationProvider() {
    }

    @Override
    public Quaternion get3DRotation(Graph graph, GraphPosition cp) {

        return DefaultEdgeBasedRotationProvider.get3DRotation(cp.reverseorientation, cp.currentedge.getEffectiveDirection(cp.getAbsolutePosition()),
                graph.getGraphOrientation().getUpVector(cp.currentedge));
    }

    /**
     * 14.12.16: Die Richtung der Kante steht ja fest, aber für die Ermittlung der kompletten Rotation brauchts auch einen up-Vektor.
     * 19.04.17: Aus GraphEdge hier hin, denn die Rotation ist keine Angelegenheit des Graphen. Erstmal static (fuer Tests) bis das rund ist.
     * 02.05.17: Immer einen upVector annehmen. Damit keinen referenceback mehr.(??) Eine Edge in Richtung (0, 0, -1) mit upvector nach y führt zur Identityrotation.
     * Das ist dann quasi eine Art Defaultrotation des Graphen. Hmm, das mit dem upvector in 3D ist aber fraglich, oder?
     * 15.3.18: Weil es doch Sache des Graphen ist, zumindets in weiten Teilen, wider zurück nach GraphEdge? Naja. wird aber nur bei Movement gebraucht.
     * Bevor ich das nochmal dahin verschieben will: Die Orientierung eines Vehicle auf einem Graph ist KEINE Graphfunktionalität, bestenfalls
     * eines GraphPath.
     * 29.3.18: Fuer Edges
     * ausserhalb der plane Ebene hat mann dann aber immer noch keine definierte Rotation, z.B. edges
     * parallel zum upVector.
     * 4.7.24: Moved from GraphOrientation to here. After many different approaches in the early days to find the 'correct' implementation it turned out
     * that there is no 'correct'. Its just a convention. And its just a cross product of edge direction and upVector.
     *
     * @return
     */
    public static Quaternion get3DRotation(boolean reverseorientation, Vector3 effectivedirection,            Vector3 up) {
        //logger.debug("get3DRotation: edgeposition=" + edgeposition + ",reverseorientation="+reverseorientation+",effectivedirection="+effectivedirection);

        if (reverseorientation) {
            effectivedirection = effectivedirection.negate();
        }

        // Don't be confused by phrase 'Look'. Its just building the cross product and then extracting the rotation.
        Quaternion rotation = Quaternion.buildLookRotation(effectivedirection.negate(), up);

        Quaternion localr = new Quaternion();
        //return baser.multiply(edger).multiply(localr);
        //16.3.18 Reihenfolge gefaellt mir so besser:19.2.20: Aber ist das auch richtig? mal anders rum versuchen. Damit stimmt SolarSystem dann. Ich blick nicht mehr durch.
        // 6.7.24 forwardrotation not longer needed?
        return rotation;//localr.multiply(rotation);//.multiply(forwardrotation);

    }


}
