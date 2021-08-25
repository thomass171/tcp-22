using System;
using java.lang;
using UnityEngine;

namespace de.yard.threed.platform
{
    using de.yard.threed.engine;
    using de.yard.threed.core.platform;
    using de.yard.threed.core;
    using de.yard.threed.platform.unity;

    /**
     * 6.4.16: Damit Tests anders loggen koennen.
     */
    public class TestLogFactory : NativeLogFactory
    {

        public Log getLog (Type clazz)
        {
            return new TestLogger (clazz);
        }  
    }

    public class TestLogger : Log {
        string name;

        public TestLogger(Type clazz) {
            name = clazz.Name;
        }

        public void debug(String msg) {
            dolog("DEBUG",msg);
        }

        public void info(String msg) {
            dolog("INFO",msg);
        }

         public void warn(String msg) {
            dolog("WARN",msg);
        }

        public void error(String msg) {
            dolog("ERROR",msg);
        }

        public void error(String msg, java.lang.Exception e) {
            e.printStackTrace ();
            dolog("ERROR",msg);
        }

        public void warn(String msg, java.lang.Exception e) {
            e.printStackTrace ();
            dolog("ERROR",msg);
        }


        private void dolog(String level, String msg){
            Debug.Log(name+":"+level+" "+msg);
        }
    }
}