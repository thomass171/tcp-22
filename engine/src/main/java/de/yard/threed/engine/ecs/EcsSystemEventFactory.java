package de.yard.threed.engine.ecs;

import de.yard.threed.core.LocalTransform;
import de.yard.threed.engine.EventRegistry;
import de.yard.threed.core.Event;
import de.yard.threed.core.Payload;


/**
 * Abbildung ueber den PlatformEventBus.
 * 17.7.17: irgendwie doppelt. Dies hier nur als Factory.
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
        return new Event(EventRegistry.EVENT_POSITIONCHANGED,new Payload(newpos));
    }
}
