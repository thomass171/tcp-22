package de.yard.threed.platform.jme;

import de.yard.threed.core.platform.NativeSplineInterpolationFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 * Created by thomass on 10.11.15.
 */
public class JmeSplineInterpolationFunction implements NativeSplineInterpolationFunction {
    PolynomialSplineFunction fct;

    public JmeSplineInterpolationFunction(PolynomialSplineFunction fct) {
       this.fct = fct;
    }

    @Override
    public double value(double x) {
        return (double) fct.value(x);
    }
}
