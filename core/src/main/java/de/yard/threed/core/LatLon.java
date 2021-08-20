package de.yard.threed.core;


/**
 * Like in OSM josm, osm2world, and many others.
 *
 * Auch keine Setter aus dem gleichen Grund wie bei Vector! 8.5.20: Wirklich?
 */
public class LatLon {

    public  double latRad;
    public  double lonRad;

    //Taken from FG
    public static double ERAD = 6378138.12;


    public LatLon(double lat, double lon) {
        this.latRad = lat;
        this.lonRad = lon;
    }


    public LatLon(Degree lat, Degree lon) {
        this.latRad = lat.toRad();
        this.lonRad = lon.toRad();
    }

    public double getLonRad() {
        return lonRad;
    }

    public double getLatRad() {
        return latRad;
    }

    public Degree getLonDeg() {
        return Degree.buildFromRadians(lonRad);
    }

    public Degree getLatDeg() {
        return Degree.buildFromRadians(latRad);
    }

    public void setLongitudeRad(double longitudeRad) {
        this.lonRad = longitudeRad;
    }

    public void setLatitudeRad(double latitudeRad) {
        this.latRad = latitudeRad;
    }

    @Override
    public String toString() {
        //return latitudeRad+" "+longitudeRad;
        return toWGS84decimalString()/* + ", " + elevationM + " m"*/;
    }

    /**
     * Es gibt wohl die Konvention Breite, Länge
     *
     * @return
     */
    public String toWGS84decimalString() {
        return Degree.buildFromRadians(latRad) + ", " + Degree.buildFromRadians(lonRad);
    }

    /**
     * like in FG, distance in meter.
     * Algorithm taken from geo.nas.
     * "spährisches Dreieck"
     * <p>
     * 22.3.18: Mit der Berechnung mit den MathUtil float MEthoden hatte ich weniger Rundungsfehler als mit double. Hmmmm. Suspekt.
     *
     * @return
     */
    public LatLon applyCourseDistance(Degree coursedeg, double dist) {
        double course = coursedeg.toRad();
        //course *= D2R;
        dist /= (double) ERAD;

        if (dist < 0.0) {
            dist = Math.abs(dist);
            course = course - Math.PI;
        }

        double lon = 0;
        double lat = Math.asin(Math.sin(latRad) * Math.cos(dist)
                + Math.cos(latRad) * Math.sin(dist) * Math.cos(course));

        // Java has % module operator for float (different to C++, where Math.mod() needs to be used)
        if (Math.cos(latRad) > MathUtil2.FLT_EPSILON) {
            lon = Math.PI - ((Math.PI - lonRad
                    - Math.asin(Math.sin(course) * Math.sin(dist)
                    / Math.cos(latRad)) % (2 * Math.PI)));
        }
        return new LatLon(lat, lon);
    }

    public Degree courseTo(LatLon dest) {
        if (latRad == dest.latRad && lonRad == dest.lonRad) {
            return new Degree(0);
        }

        //TODO Singularitaeten abfangen? an den Polen?
        double dlon = dest.lonRad - lonRad;
        double ret = 0;

        ret = (Math.atan2(Math.sin(dlon) * Math.cos(dest.latRad),
                Math.cos(latRad) * Math.sin(dest.latRad)
                        - Math.sin(latRad) * Math.cos(dest.latRad)
                        * Math.cos(dlon)) %/*,*/  (2 * Math.PI)) ;
        return Degree.buildFromRadians(ret);

    }

    /**
     * From https://de.wikipedia.org/wiki/Orthodrome and geo.nas
     */
    public double distanceTo(LatLon dest) {
        if (latRad == dest.latRad && lonRad == dest.lonRad) {
            return 0;
        }

        double a = Math.sin((latRad - dest.latRad) * 0.5);
        double o = Math.sin((lonRad - dest.lonRad) * 0.5);
        return (2.0 * ERAD * Math.asin(Math.sqrt(a * a + Math.cos(latRad)
                * Math.cos(dest.latRad) * o * o)));
    }

    public static LatLon fromDegrees(double lat, double lon){
        return new LatLon(new Degree(lat),new Degree(lon));
    }
}