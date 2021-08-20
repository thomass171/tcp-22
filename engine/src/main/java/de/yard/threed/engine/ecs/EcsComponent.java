package de.yard.threed.engine.ecs;

/**
 * Wird von allen Components implementiert.
 * 28.11.16: Mal als reinen Datencontainer versuchen. Dann class statt interface.
 * 03.04.17: tag jetzt konsequent nutzen, bis es eine Registry gibt. Muesste eigentlich static sein (geht nich in C#). Irgenwie doof. Aber abstract ist ganz gut.
 * 01.07.18: No isInit() or init().
 *
 * <p>
 * Created by thomass on 24.11.16.
 */
public abstract class EcsComponent {

    public EcsComponent() {
    }

    public abstract String getTag();

}
