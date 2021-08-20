package de.yard.threed.core.platform;

import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeSceneNode;

import java.util.List;

/**
 * Created by thomass on 28.11.15.
 */
public interface NativeRay {
    /**
     * Liefert die Direction neutral von einer evtl. Handedness Konvertierung.
     * siehe auch DVK.
     */
    Vector3 getDirection();

    /**
     * Liefert origin neutral von einer evtl. Handedness Konvertierung.
     * siehe auch DVK.
     */
    Vector3 getOrigin();

    /**
     * 30.11.15: Wie das plattformuebergreifend arbeiten soll ist noch
     * nicht endgültig definiert. Rekursiv ab dem uebergebenem model (wie JME),
     * oder nicht (wie ThreeJS)?
     * 5.1.16: Rekursiv, das kann ThreeJS auch. Siehe auch Kommentar in Referencescene.
     * Bewaehren muss sich das ganze noch.
     * 3.3.17: In Unity ist jetzt auch eine rekursive SubModel Suche implementiert.
     * Also Fazit: Es wird auf Collisions unterhalb von model gesucht.
     * 23.3.18: So einfach ist das nicht. Unity kann das offenbar überhaupt nicht, sondern
     * durchsucht nur eine Liste von Collidern. Eine Baumsuche muesste man dann
     * nachbilden, was einigermassen aufwaendig und anfaellig ist.
     * JME kann zwar Trees durchsuchen, das scheint aber nicht so ganz zu funktionieren (ist nicht ganz klar).
     * Und prinzipiell ist die Idee mit den Collidern schon ganz gut.
     * Daher will ich mich doch mal von dieser Methode loesen, vorerst als deprecated,
     * um nicht zu viele Baustellen zu haben. MA22
     */
    @Deprecated
    List<NativeCollision> intersects(NativeSceneNode model);

    /**
     * Einfach alle Collisions ermitteln, ohne einen (Teil)Graph zu uebergeben, in dem gesucht wird. Ist mehr Unity Like.
     * Und manchmal auch praktischer.
     */
    List<NativeCollision> getIntersections();

}
