package de.yard.threed.graph;

/**
 * Created on 21.11.18.
 */
public class DefaultGraphPathConstraintProvider implements GraphPathConstraintProvider {
    double minimumlen, smotthingradius;

    public DefaultGraphPathConstraintProvider(double minimumlen, double smotthingradius) {
        this.minimumlen = minimumlen;
        this.smotthingradius = smotthingradius;
    }

    public double getMinimumLength() {
        return minimumlen;
    }

    public double getSmoothingRadius(GraphNode graphNode) {
        return smotthingradius;
    }
}
