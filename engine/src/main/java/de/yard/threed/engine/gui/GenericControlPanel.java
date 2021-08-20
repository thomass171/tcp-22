package de.yard.threed.engine.gui;

import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;

/**
 * 3.5.21: Das muss nicht unbedingt ein menu sein.
 */
public interface GenericControlPanel {

    //geht nicht generisch, weil fuer mouseclick jemand die Camera kennen muss
    //Aber menu soll doch keine Camera kennen! Der Menu Provider kennt die Camera?
    //Aehnliches gilt fuer den Controller
    //Request checkForClickedButton(Point mouselocation);
    /**
     * 30.12.19: Es ist doch nur konsequent, hier parallel zum Delegate den Request zu verschicken statt ihn zurückzuliefern.
     * Geht aber nicht wegen ECS independence.
     * Vorläufig noch mit Returnwert, bis alle Aufrufer das können.
     * return true if any button/area was clicked, false otherwise
     */
    boolean checkForClickedArea(Ray ray);


}
