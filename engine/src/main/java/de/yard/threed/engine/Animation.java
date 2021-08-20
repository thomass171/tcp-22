package de.yard.threed.engine;

/**
 * Date: 15.07.14
 */
public abstract class Animation {
    public AnimationListener lis;
    
    public Animation() {
    }

    public Animation(AnimationListener lis) {
        this.lis = lis;
    }

    public abstract boolean process(boolean forward);

    /**
     * Implementierer muss die dann ueberschreiben.
     */
    public void reset() {

    }

    public abstract String getName() ;

}
