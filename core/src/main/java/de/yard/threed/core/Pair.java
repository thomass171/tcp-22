package de.yard.threed.core;

/**
 * C# hat auch so eine Klasse.
 * 
 * Created by thomass on 08.08.16.
 */
public class Pair<T1, T2> {
    T1 first;
    T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;

    }

   
    
    public T1 getFirst(){
        return first;
    }
    
    public T2 getSecond(){
        return second;
    }

    public void setFirst(T1 t1) {
        first=t1;
    }

    public void setSecond(T2 t2) {
        second=t2;
    }
}
