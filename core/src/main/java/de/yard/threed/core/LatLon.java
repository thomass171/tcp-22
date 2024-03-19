package de.yard.threed.core;


/**
 * Like in OSM josm, osm2world, and many others.
 *
 * Auch keine Setter aus dem gleichen Grund wie bei Vector! 8.5.20: Wirklich?
 */
public class LatLon {

    public  double latRad;
    public  double lonRad;




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
     * 18.3.24: GeoCoordinate has its own toString().
     * @return
     */
    public String toWGS84decimalString() {
        return Degree.buildFromRadians(latRad) + ", " + Degree.buildFromRadians(lonRad);
    }





    public static LatLon fromDegrees(double lat, double lon){
        return new LatLon(new Degree(lat),new Degree(lon));
    }
}