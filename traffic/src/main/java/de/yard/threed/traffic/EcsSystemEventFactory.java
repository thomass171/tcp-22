package de.yard.threed.traffic;

import de.yard.threed.core.Event;
import de.yard.threed.core.LocalTransform;
import de.yard.threed.core.Payload;
import de.yard.threed.engine.ecs.TeleporterSystem;


/**
 * Abbildung ueber den PlatformEventBus.
 * 17.7.17: irgendwie doppelt. Dies hier nur als Factory.
 * 14.10.21: Zur Signatursicherung aber gut. Wo ist das sonst? Direkt in der Registry?
 * 
 * Created by thomass on 27.12.16.
 */
public class EcsSystemEventFactory {
    public Event evt;
    
   /* private EcsSystemEventFactory(EventType type, Object payload){
        evt = new Event(type,payload);
    }*/
    
    /*7.5.19 public static Event buildModelLoadEvent(FileSystemResource resource){
        return new Event(EventType.MODELLOAD,new Payload(resource));
    }*/

    /*public static EcsSystemEvent buildXmlModelLoadedEvent(SGPropertyNode prop){
        return new EcsSystemEvent(EventType.XMLMODELLOADED,prop);
    }*/

    /*9.3.21 MA31 public static Event buildModelLoadedEvent(ReadResult node){
        return new Event(EventType.MODELLOADED,new Payload(node));
    }*/

    public static Event buildPositionChangedEvent(LocalTransform newpos) {
        return new Event(TeleporterSystem.EVENT_POSITIONCHANGED,new Payload(newpos));
    }
}
