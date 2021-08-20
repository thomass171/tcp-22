package de.yard.threed.engine.platform.common;


/**
 *
 * Die Callback Methoden werden aus dem Main update Thread des Renderers aufgerufen. 
 * 8.3.16: Zumindest bei JME. Aber ob dieses Konzept so das wahre ist? Sollte irgendwie mit Ajax zusammengeführt werden.
 * 26.4.16: Das passt aber auch zu Unity. Wichtig ist, dass die Callback Methoden aus dem MainThread gestartet werden.
 * Created by thomass on 14.11.15.
 * 10.4.17:AsyncJobCallback extrahiert.
 * 2.5.20: Ob das wirklich noch so gebraucht wird? Zumindest in engine? Oder nur in "exceptGWT"?
 * 17.5.20: Reaktivieren fuer alle JME like async jobs in AbstractSceneRunner(mit AsyncJobDelegate)?
 * Aber auch fuer Nutzung aus App, falls die sowas möchte.
 * 20.5.20: Nee, lieber mal als AsyncInvoked. Und das hier deprecated.Wird noch verwendet!
 * 2.8.21:Mal ohne veruchen. Wird aber noch verwendet, z.B. book.
 */
@Deprecated
public interface AsyncJob {
    // Diese Methode wird in einem anderen Thread aufgerufen! Wenn ein Fehler auftritt, muss/kann eine Exception geworfen werden.
    // Dann wird nachher der onFailure() aufgerufen. 13.6.17: Aber warum Exception? Um eine Fehlermeldung zurückzugeben?
    // Irgendwie ist das unhandlich. Mal versuchen ueber Message Returnwert != null.
    String execute();

    AsyncJobCallback getCallback();
    
    String getName();
}
