package de.yard.threed.engine;

import de.yard.threed.core.*;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.platform.NativeCollision;

import java.util.List;

/**
 * Display marker in y0 plane.
 * <p>
 * Not only/necassarily for VR.
 * <p>
 * 15.5.21
 */
public class GridTeleporter {
    static Log logger = Platform.getInstance().getLog(GridTeleporter.class);

    public static double VISIBLE_GROUNDMARKER_Y = 0.002;
    public static double HIDDEN_GROUNDMARKER_Y = 0.001;

    SceneNode locationMarker, directionMarker;

    public GridTeleporter(SceneNode locationMarker, SceneNode directionMarker) {
        this.locationMarker = locationMarker;
        this.directionMarker = directionMarker;
    }

    /**
     * Show a teleport destination marker on a tile depending on the position, where the ray hits the tile.
     * Skizze 65
     *
     * @param ray
     * @param tileCandidate
     * @return
     */
    public GridTeleportDestination updateDestinationMarker(Ray ray, SceneNode tileCandidate, double gridSize) {
        if (ray != null) {
            List<NativeCollision> intersections = ray.getIntersections(tileCandidate, true);
            //logger.debug("tile intersections: " + intersections.size());
            //TODO only use first/nearest?
            for (int i = 0; i < intersections.size(); i++) {
                //logger.debug("intersection: " + intersections.get(i).getSceneNode().getName());
                Vector3 ip = intersections.get(0).getPoint();
                //destinationMarker.getTransform().setPosition(new Vector3(ip.getX(), 0.01, ip.getZ()));
                GridTeleportDestination markerTransform = moveDestinationMarker(new Vector2(ip.getX(), ip.getZ()), locationMarker, directionMarker, gridSize);
                //logger.debug("updateDestinationMarker: tile hit marker="+markerTransform);
                return markerTransform;
            }
        }
        // no intersection of tile->no marker
        //logger.debug("updateDestinationMarker: no marker with ray "+ray);
        return null;
    }

    /**
     * for a y0 plane.
     * <p>
     * Skizze 65
     * <p>
     * Returns the location where a teleport should end (center) and the corresponding rotation.
     *
     * @param rawIntersection
     */
    public GridTeleportDestination moveDestinationMarker(Vector2 rawIntersection, SceneNode localMarker, SceneNode directionMarker, double gridSize) {

        double s2 = gridSize / 2;
        double s4 = gridSize / 4;
        double s8 = gridSize / 8;
        // center is the center of the grid cell in world coordinates (not logical grid coordinates), eg. (10.5,-4.5) for cell (7,3)
        Vector2 center = new Vector2(Util.roundDouble(rawIntersection.getX() / gridSize) * gridSize, Util.roundDouble(rawIntersection.getY() / gridSize) * gridSize);

        double xoffset = rawIntersection.getX() - center.getX();
        double yoffset = rawIntersection.getY() - center.getY();

        Quaternion rotation;
        Vector2 d;
        char dir;

        //logger.debug("center=" + center + ",xoffset=" + xoffset + ",yoffset=" + yoffset);
        if (Math.abs(xoffset) < s4) {
            // inner x area
            if (Math.abs(yoffset) > s4 && Math.abs(yoffset) < s2) {
                // lower or upper area
                if (yoffset < 0) {
                    //logger.debug("upper area");
                    rotation = Quaternion.buildRotationY(new Degree(0));
                    d = new Vector2(center.getX(), center.getY() - s4 - s8);
                    dir = 'N';
                } else {
                    //logger.debug("lower area");
                    rotation = Quaternion.buildRotationY(new Degree(180));
                    d = new Vector2(center.getX(), center.getY() + s4 + s8);
                    dir = 'S';
                }
                transformMarker(d, directionMarker, localMarker, rotation);
                return new GridTeleportDestination(new LocalTransform(new Vector3(d.getX(), 0, d.getY()), rotation), dir);
            }
        }
        if (Math.abs(yoffset) < s4) {
            // inner y area
            if (Math.abs(xoffset) > s4 && Math.abs(xoffset) < s2) {
                // left or right area
                if (xoffset < 0) {
                    //logger.debug("? area");
                    rotation = Quaternion.buildRotationY(new Degree(90));
                    d = new Vector2(center.getX() - s4 - s8, center.getY());
                    dir = 'W';
                } else {
                    //logger.debug("? area");
                    rotation = Quaternion.buildRotationY(new Degree(-90));
                    d = new Vector2(center.getX() + s4 + s8, center.getY());
                    dir = 'E';
                }
                transformMarker(d, directionMarker, localMarker, rotation);
                return new GridTeleportDestination(new LocalTransform(new Vector3(d.getX(), 0, d.getY()), rotation), dir);
            }
        }

        // assume not in any direction area. No direction indicator. No rotation
        transformMarker(center, localMarker, directionMarker, null);
        return new GridTeleportDestination(new LocalTransform(new Vector3(center.getX(), 0, center.getY()), null));
    }

    /**
     * Get the offset of the center of a teleport marker to a field center as absolute value (direction independent)
     */
    public static double getCenterOffset(double gridSize) {
        double s4 = gridSize / 4;
        double s8 = gridSize / 8;

        return s4 + s8;
    }

    private void transformMarker(Vector2 location, SceneNode visibleMarker, SceneNode hiddenMarker, Quaternion rotation) {
        //rotating local marker doesn't matter
        if (rotation != null) {
            visibleMarker.getTransform().setRotation(rotation);
        }
        visibleMarker.getTransform().setPosition(new Vector3(location.getX(), VISIBLE_GROUNDMARKER_Y, location.getY()));
        hiddenMarker.getTransform().setPosition(new Vector3(location.getX(), HIDDEN_GROUNDMARKER_Y, location.getY()));
    }
}
