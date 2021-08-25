using System;
using System.IO;
using UnityEngine;
using java.lang;
using java.util;


namespace de.yard.threed.platform.unity
{
    /**
     * Geht nicht direkt in Arrays, weil die Groesse noch nicht klar ist
     * 1.8.16:Deprecated
     */
    public class UnityVBO  //:  NativeVBO
    {
        public List<UnityEngine.Vector3> vertices = new ArrayList<UnityEngine.Vector3> ();
        public List<UnityEngine.Vector3> normals = new ArrayList<UnityEngine.Vector3> ();
        public List<UnityEngine.Vector2> uvs = new ArrayList<UnityEngine.Vector2> ();
        private List<Int32> indexes = new ArrayList<Int32> ();

        public UnityVBO ()
        {
        }

        public int addRow (de.yard.threed.core.Vector3 vertex, de.yard.threed.core.Vector3 normal, de.yard.threed.core.Vector2 uv)
        {
            vertices.add (UnityVector3.toUnity(vertex));
            normals.add(UnityVector3.toUnity(normal));
            uvs.add(new Vector2((float)uv.getX(),(float)uv.getY()));
            return vertices.size () - 1;
        }

        public void addTriangle (int ili, int index0, int index1, int index2)
        {
            //ili wird fuer JME einfach ignoriert. Es gibt nur eine
            // Indexliste. Wenn es mehrere Materialien gab, muss der Mesh
            // vorher in Submeshes verteilt worden sein.
            indexes.add(index0);
            indexes.add(index1);
            indexes.add(index2);
        }

        public int[] getIndexes(){
            int[] ni = new int[indexes.size()];
            for (int i = 0; i < ni.Length; i++) {
                ni[i] = indexes.get(i);
                //logger.info("index " + +i + ":" + ni[i]);

            }
            return ni;
        }
    }

}