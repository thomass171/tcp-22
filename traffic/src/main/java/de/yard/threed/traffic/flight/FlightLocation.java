package de.yard.threed.traffic.flight;

import de.yard.threed.core.Degree;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Quaternion;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.traffic.EllipsoidCalculations;
import de.yard.threed.traffic.TrafficHelper;
import de.yard.threed.core.GeoCoordinate;



/**
 * Gibts sowas schon? Ähnlich in Stepcontrollern, aber nicht mit Geo. WorldGlobal?
 * 8.6.17: Das ist doch für Modelle, nicht Camera, oder?
 * <p>
 * 10.3.18: Die Klasse ist schon ganz gut zur Darstellung einer Positon/Orientierung in GeoKoordinaten. Analog zu PosRot. , evtl. mit anderem Namen.
 * Und vielleicht fehlt noch yaw?
 *
 * 27.12.21:Needed in FG, so it must be in "traffic".
 * 22.7.24: Has no parse() because its too complex. Needs to consider SmartLocation?
 * 29.3.25:See also PositionerFactory
 * 6.5.25: Don't like this class any more. Should be deprecated.
 * Created by thomass on 05.01.17.
 */
public class FlightLocation {
    public GeoCoordinate coordinates;
    public Degree heading, pitch;

    /**
     * Nach vorne kippen ist negativer Pitch
     * kein Roll.
     *
     * @param coordinates
     * @param heading
     * @param pitch
     */
    public FlightLocation(GeoCoordinate coordinates, Degree heading, Degree pitch) {
        this.coordinates = coordinates;
        this.heading = heading;
        this.pitch = pitch;
    }

    public FlightLocation(GeoCoordinate coordinates, Degree heading) {
        this.coordinates = coordinates;
        this.heading = heading;
        this.pitch = new Degree(0);
    }

    public LocalTransform toPosRot(EllipsoidCalculations rbcp) {
        return new LocalTransform(rbcp.toCart(coordinates,null,null), rbcp.buildZUpRotation(coordinates, heading, pitch));
    }

    /**
     * 12.1.17: Die MEthode sehe ich wegen Eindeutigkeit und möglicher Rundungsfehler mal nicht vor. bzw. deprecated.
     * Die Long/Lat angaben sind führend.
     * 13.1.17: Wobei das fragwürdig ist. Die ganze Arithmetik läuft doch in 3D Koordinaten, z.B. eine FPC ähnliche Bewegung per translateOnAxis.
     * 10.3.18: Das deprecated scheint mir berechtigt, denn ein zurueckrechnen von Quaternion auf heading/pich scheint nicht moeglich.
     * Oder nur eingeschraenkt. Wenn ueberhaupt macht das nur in Nähe der Erdoberfläche Sinn. Und was fuer ein Heading hat ein senkrecht nach unten
     * fliegendes Objekt?
     * 22.9.23: Agreed to above. Calculating back from rotation to heading/pitch cannot be reliable. There should be no use case for this method.
     * @param posRot
     * @return
     */
    @Deprecated
    public static FlightLocation fromPosRot(LocalTransform posRot) {
        double[] a = new double[3];
        posRot.rotation.toAngles(a);
        //System.out.println("x=" + Degree.buildFromRadians(a[0]));
        //System.out.println("y=" + Degree.buildFromRadians(a[1]));
        //System.out.println("z=" + Degree.buildFromRadians(a[2]));
        EllipsoidCalculations rbcp = TrafficHelper.getEllipsoidConversionsProviderByDataprovider();
        FlightLocation fl = new FlightLocation(rbcp.fromCart(posRot.position), Degree.buildFromRadians(a[2]), Degree.buildFromRadians(a[1]));
        return fl;
    }


    /**
     * Hergeleitet fuer Earth, passt aber fuer alle Objekte:
     * Der Pazifik ist rechts auf pos x. Darum einmal rumdrehen, damit dort der
     * Äquator ist. Die y-Achse läuft durch die Pole. Dafür nochmal um 90 Grad.
     * <p>
     * Das geht mit der y-Ache ganz gut, weil die i.d.R. bei OpenGL oben ist. Für die sonstige Orientierung (links/rechts/vorne/hinten)
     * ist das nicht so einfach, weil das auch wieder eine Konventionsfrage ist. Da muss der Aufrufer seine Konvention mitteilen.
     *
     * @param model
     * @return
     */
    public static SceneNode rotateFromYupToFgWorld(SceneNode model) {
        SceneNode fgmodel = SceneNode.buildSceneNode(model, null, Quaternion.buildFromAngles(new Degree(-90), new Degree(180), new Degree(0)));
        return fgmodel;
    }
}
