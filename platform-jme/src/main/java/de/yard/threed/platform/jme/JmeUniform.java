package de.yard.threed.platform.jme;

import de.yard.threed.core.platform.NativeUniform;

public abstract class JmeUniform<T> implements NativeUniform<T> {

    @Override
    public abstract void setValue(T value) ;
}
