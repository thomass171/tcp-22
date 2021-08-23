package de.yard.threed.javanative;

public class JavaUtil {

    public static void sleepMs(int millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
