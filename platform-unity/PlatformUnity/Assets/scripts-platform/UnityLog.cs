using System;
using System.Net;
using System.Net.Sockets;
using java.lang;
using UnityEngine;

namespace de.yard.threed.platform
{
    using de.yard.threed.engine;
    using de.yard.threed.core.platform;
   
    using de.yard.threed.platform.unity;

    /**
     *
     */
    public class UnityLog : Log
    {
        // ALle Instanzen schreiben ins selbe file. Das gilt auch fuer den networkstream
        private static System.IO.StreamWriter logfile;
        private static NetworkStream networkstream;

        string name;

        public UnityLog (Type clazz)
        {
            // Beim Pruefen ueber die Platform stürzt Unity ab! Evtl. weil das schon bei statischer Initialisierung aufgerufen wird.
            // 4.8.17: Die Verwendung von SystemInfo.deviceType geht nur im mainthread. Darum erstmal nicht mehr abfragen.
            /*if (SystemInfo.deviceType == DeviceType.Handheld) {//((PlatformUnity)Platform.getInstance()).isHandheld()) {
                // 21.5.16: da gibt es zunaechst mal keine eigenes Logfile bis klar ist, wie man das macht.
                // Das muesste dann ja remote gehen, sonst nutzt es nichts.
                return;
            }*/
            name = clazz.Name;
            // Muesste eigentlich threadsafe gemacht werden...(und nur einmal. pruefen TODO)
            if (logfile == null) {
                logfile = new System.IO.StreamWriter ("Granada.log");
                logfile.AutoFlush = true;
            }
         
        }

        public static void setupNetworkstream (String host)
        {
            try {
                Int32 port = 4321;
                IPAddress ipAddress;// = Dns.GetHostEntry (Dns.GetHostName ()).AddressList[0];
                ipAddress = IPAddress.Parse (host);
                IPEndPoint endpoint = new IPEndPoint (ipAddress, port);
                TcpClient client = new TcpClient ();
                client.Connect (endpoint);
                // Get a client stream for reading and writing.
                //  Stream stream = client.GetStream();
                networkstream = client.GetStream ();
            } catch (System.Exception e) {
                Debug.Log ("Setting up remote logging failed: " + e.Message);
            }
        }

        public void debug (String msg)
        {
            dolog ("DEBUG", msg);
        }

        public void info (String msg)
        {
            dolog ("INFO ", msg);
        }

        public void warn (String msg)
        {
            dolog ("WARN ", msg);
        }

        public void error (String msg)
        {
            dolog ("ERROR", msg);
        }

        public void error (String msg, java.lang.Exception e)
        {
            dolog ("ERROR", msg);
            dolog ("ERROR", e.getStackTrace ());
        }

        public void warn (String msg, java.lang.Exception e)
        {
            dolog ("WARN", msg);
            dolog ("WARN", e.getStackTrace ());
        }


        /**
         * Der Debug.Log von Unity ist nicht unbedingt ideal. Er scheint immer einen Stacktrace mit zu ermitteln.
         * Auf Android mit logcat wird der auch immer mit ausgegeben. Das macht das Log schwer lesbar. Von der
         * Datenmenge mal ganz abgesehen.
         */
        private void dolog (String level, String msg)
        {
            if (logfile != null) {
                // hier muss aber wirklich gelockt werden
                lock (logfile) {
                    string ts = DateTime.Now.ToString ("yyyy.MM.dd HH:mm:ss.fff"); 
                    logfile.WriteLine (ts + " " + level + ":" + name + " " + msg);
                }
                //Debug geht dann nicht in die Unity Console
                if (!level.Equals ("DEBUG")) {
                    Debug.Log (name + ":" + level + " " + msg);
                }
            } else {
                if (networkstream != null) {
                   
                    // hier muss aber wirklich gelockt werden
                    lock (networkstream) {
                        // Translate the passed message into ASCII and store it as a Byte array.
                        // newline mitschicken?
                        string ts = DateTime.Now.ToString ("yyyy.MM.dd HH:mm:ss.fff"); 
                        msg = ts + " " + level + ":" + name + " " + msg + "\n";

                        Byte[] data = System.Text.Encoding.ASCII.GetBytes (msg);         

                        networkstream.Write (data, 0, data.Length);
                        networkstream.Flush ();
                    }

                } else {
                    Debug.Log (name + ":" + level + " " + msg);
                }
            }
        }
    }
}