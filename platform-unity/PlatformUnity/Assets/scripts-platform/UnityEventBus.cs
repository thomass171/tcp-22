using System;
using java.lang;
using UnityEngine;
using de.yard.threed.engine;
using de.yard.threed.core.platform;
using de.yard.threed.core;
using java.util;

//nicht in Unity using System.Collections.Concurrent;

namespace de.yard.threed.platform.unity
{
    /**
     * 28.12.16: Ist wohl noch nicht so ganz rund wegen  multithreading und poll?
     */
    public class UnityEventBus  :  NativeEventBus
    {
        Log logger = Platform.getInstance ().getLog (typeof(UnityEventBus));
        private List<EventSubscriber> subscriberlist = new ArrayList<EventSubscriber> ();
        private List<de.yard.threed.core.Event> events = new ArrayList<de.yard.threed.core.Event> ();
        private  bool perqueue = true;
        private static NativeEventBus instance;

        public static NativeEventBus getInstance ()
        {
            if (instance == null) {
                instance = new UnityEventBus ();
            }
            return instance;
        }

        public void subscribe (object objectClass, EventSubscriber eventSubscriber)
        {
            subscriberlist.add (eventSubscriber);
        }

        public void publish (de.yard.threed.core.Event evt)
        {
            events.add (evt);
        }

        public de.yard.threed.core.Event poll (int timeoutmillis)
        {
            if (events.size () == 0) {
                return null;
            }
            // TODO timeout
            de.yard.threed.core.Event evt = events.get (0);
            events.remove (0);
            return evt;
        }


        public int getEventCount ()
        {
            return events.size ();
        }

        public void clear ()
        {
            events.clear ();
        }
    }
}