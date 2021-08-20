package de.yard.threed.engine.platform.common;

import de.yard.threed.core.platform.AsyncJobDelegate;

/**
 *
 * Die Callback Methoden werden aus dem Main update Thread des Renderers aufgerufen. 
 * 8.3.16: Zumindest bei JME. Aber ob dieses Konzept so das wahre ist? Sollte irgendwie mit Ajax zusammengeführt werden.
 * 26.4.16: Das passt aber auch zu Unity. Wichtig ist, dass die Callback Methoden aus dem MainThread gestartet werden.
 * Created by thomass on 14.11.15.
 * 10.4.17:Aus AsyncJob extrahiert. Der success hat bewusst keine Ergebnisparameter, weil die quais nicht allgemeingueltig definierbar sind.
 * Schoener ware es aber.
 * 18.5.20: Deprecated zugunsten von {@link AsyncJobDelegate}. Wird aber bestimmt problematisch.
 * 2.8.21:Mal ohne versuchen.Wird aber noch verwendet, z.B. book.
 */
@Deprecated
public interface AsyncJobCallback {
    
    void onSuccess();

    /**
     * Bekommt die Exception aus dem execute(). 13.6.17: Jetzt Message.
     */
    void onFailure(/*java.lang.Exception*/String e);
}
