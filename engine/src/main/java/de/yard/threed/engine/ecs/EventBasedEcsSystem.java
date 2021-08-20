package de.yard.threed.engine.ecs;

/**
 * 17.7.17: Nicht nur für ein Event sondern auch mehrere.
 * 
 * Created by thomass on 27.12.16.
 */
public abstract class EventBasedEcsSystem extends EcsSystem {
    //EventType[] eventtype;

    /*public EventBasedEcsSystem(EventType[] eventtype) {
        this.eventtype = eventtype;
    }*/
    
    /**
     * Event, fuer das dieses System zustaendig ist. null, wenn fuer keins.
     * @return
     */
    /*public EventType[] getEventType() {
        return eventtype;
    }*/

    /**
     * Ein Event verarbeiten.
     *
     * @param evt
     */
    //public abstract void process(Event evt);
    
    /*@Override
    public boolean update(EcsGroup gr, float tpf){
        return false;
    }*/
}
