using System;
using UnityEngine;
using java.lang;
using java.util;
using de.yard.threed.engine;
using de.yard.threed.core;
using de.yard.threed.core.platform;

namespace de.yard.threed.platform.unity
{

    /**
     * Position, Rotation und Scale Attribute als Superklasse fuer alles, was die verwendet.
     * Vorsicht: Der Name UnityTransform ist schon anderweitig vergeben. 
     */
    public class UnityBase3D  :    NativeTransform/*NativeBase3D*/
    {
        Log logger = Platform.getInstance ().getLog (typeof(UnityBase3D));
        //MA17 protected UnitySceneNode parentscenenode;

        //15.9.16 public GameObject gameObject;
        private UnityEngine.Transform transform;

        /*        public UnityBase3D (UnityEngine.Transform transform/*GameObject gameObject* /)
        {
            //this.gameObject = gameObject;       
            this.transform = transform; 
        }*/



        public UnityBase3D (/*GameObject gameObject*/UnityEngine.Transform transform, UnitySceneNode parentscenenode, bool existing)
        {          
            this.transform = transform;
            if (transform == null) {
                logger.error ("transform is null");
                throw new System.Exception ("transform is null");
            }
            //MA17this.parentscenenode = parentscenenode;
            if (!existing) {
                int id = transform.gameObject.GetInstanceID ();

                //MA17 Platform.getInstance ().native2nativeobject3d.put (id, this);
            }
        }

        public void add (NativeTransform/*Object3D*/ obj)
        {
            UnityBase3D o = (UnityBase3D)obj;

            setParent (o./*gameObject.*/transform, this/*.gameObject*/.transform);
        }


     
        public de.yard.threed.core.Quaternion getRotation ()
        {
            return UnityTransform.getRotation (/*gameObject.*/transform);
        }

        public de.yard.threed.core.Vector3 getPosition ()
        {
            return UnityTransform.getPosition (/*gameObject.*/transform);
        }

        virtual public void setPosition (de.yard.threed.core.Vector3 position)
        {
            UnityTransform.setPosition (/*gameObject.*/transform, (position));
        }

        virtual public void setRotation (/*Native*/de.yard.threed.core.Quaternion rotation)
        {
            UnityTransform.setRotation (/*gameObject.*/transform, rotation);
        }

        virtual public /*Native*/de.yard.threed.core.Vector3 getScale ()
        {
            return UnityTransform.getScale (/*gameObject.*/transform);
        }

        virtual public void translateOnAxis (/*Native*/de.yard.threed.core.Vector3 axis, double distance)
        {
            UnityTransform.translateOnAxis (/*gameObject.*/transform, axis, distance);
        }

        virtual public void setScale (/*Native*/de.yard.threed.core.Vector3 scale)
        {
            UnityTransform.setScale (/*gameObject.*/transform, scale);
        }

        virtual public void rotateOnAxis (/*Native*/de.yard.threed.core.Vector3 axis, double angle)
        {
            UnityTransform.rotateOnAxis (/*gameObject.*/transform, axis, angle);
        }

        virtual public /*Native*/Matrix4 getLocalModelMatrix ()
        {
            return UnityTransform.getLocalModelMatrix (/*gameObject.*/transform);
        }

        /* virtual public void translate (UnityVector3 v)
        {
            position = (UnityVector3)MathUtil2.add (position, v);
        }

        virtual public void translateX (float t)
        {
            //TODO position.x += t;
        }

        virtual public void translateY (float t)
        {
            //TODO position.y += t;
        }

        virtual public void translateZ (float t)
        {
            //TODO position.z += t;
        }*/

        /**
     * Der Sinn dieser Methode ist fraglich
     *
     * @param v
     */
        /*unity virtual public void scale(UnityVector3 v) {
        //ich mach das mal genauso wie setScale
        //scale = scale.add(v);
        setScale(v);
    }*/

        public static void setParent (UnityEngine.Transform o, UnityEngine.Transform parent)
        {
            //Ob das mit dem transform so richtig ist?
            //Unity passt beim setzen des parent transform den local an, damit die World Coordinates gleich bleiben.
            //Strange irgendwie. Darum sichern und und wider setzen.
            // GameObject go = o.gameObject;
            UnityEngine.Vector3 pos = o.localPosition;
            UnityEngine.Vector3 scale = o.localScale;
            UnityEngine.Quaternion rot = o.localRotation;
            //logger.debug("position before add:"+ UnityVector3.dump(o.gameObject.transform.localPosition));
            //logger.debug ("before add"  + ", y=" + obj.getPosition ().getY ());
            o.parent = parent;
            //logger.debug ("position after add:" + UnityVector3.dump (o.gameObject.transform.localPosition));
            //logger.debug ("after add" + ", y=" + obj.getPosition ().getY ());
            o.localPosition = pos;
            o.localScale = scale;
            o.localRotation = rot;
        }

        /**
     * M = T x R x S
     *
     * Winkel werden ueberhaupt nicht mehr verwendet, nur Quaternions.
     *
     * @return
     */
        
        /* virtual protected UnityMatrix4 buildMatrix ()
        {

            UnityMatrix4 translationMatrix = UnityMatrix4.buildTranslationMatrix (position);
            // Die hintere Rotation ist angeblich die erste, d.h. wir rotieren hier in der
            // Reihenfolge ZYX
            UnityMatrix4 rotationMatrix;
            // Winkel und Quaternion sind synchron.
            //OGL if (rotation != null)
            rotationMatrix = (UnityMatrix4)MathUtil2.toRotationMatrix (rotation);
            UnityMatrix4 scaleMatrix = UnityMatrix4.buildScaleMatrix (scale);
            logger.debug ("scaleMatrix=" + new Matrix4 (scaleMatrix).dump (" "));
            logger.debug ("rotationMatrix=" + new Matrix4 (rotationMatrix).dump (" "));
            logger.debug ("translationMatrix=" + new Matrix4 (translationMatrix).dump (" "));
            return (UnityMatrix4)MathUtil2.buildMatrix (translationMatrix, rotationMatrix, scaleMatrix);        
        }*/


        /**
         * Liefert die Matrix neutral von einer evtl. Handedness Konvertierung.
         */
        public /*Native*/Matrix4 getWorldModelMatrix ()
        {
            Matrix4x4 worldmatrix = /*gameObject.*/transform.localToWorldMatrix;
        return (Matrix4)Scene/*Platform.getInstance ()*/.getWorld ().mirror (UnityMatrix4.fromUnity (worldmatrix));
        }

        virtual public NativeTransform getParent ()
        {
            if (/*gameObject.*/transform.parent == null) {
                return null;
            }
            // Aehnliches Problem wie in JME. Hier kommt nicht dieselbe Wrapperinstanz.
            // 5.10.17: Jetzt trotzdem
            return new UnityBase3D (/*gameObject.*/transform.parent/*.gameObject*/, null, true);
            /*UnityEngine.Transform parent = transform.parent;
            GameObject parentgo = parent.gameObject;

            return Platform.getInstance ().findSceneNodeById (parentgo.GetInstanceID ()).getTransform ();*/
        }

        virtual public void setParent (NativeTransform/*Base3D*/ parent)
        {
            if (parent == null) {
                //setParent (transform, null);
                //UnityBase3D.setParent (transform, ((UnityBase3D)((EnginePlatform)Platform.getInstance ()).getWorld ().getTransform().transform).transform);
                parent = (UnityBase3D)Scene.getWorld ().getTransform().transform;

            } 
            UnityBase3D o = (UnityBase3D)parent;
                setParent (transform, o.transform);
                //Ob das mit dem transform so richtig ist?
                //gameObject.*/transform.parent = o/*.gameObject*/.transform;
	    //13.11.19:Layer vererben
	    setLayer(parent.getLayer());
        }

        public NativeSceneNode getSceneNode ()
        {
            //return parentscenenode;
            return new UnitySceneNode (transform.gameObject, this);
        }

        public NativeTransform/*Object3D/*14.11.16 NativeSceneNode*/ getChild (int index)
        {
            /*MA17 int id = transform.GetChild (index).gameObject.GetInstanceID ();
            return Platform.getInstance ().findObject3DById ((id));*/
            return getChildren ().get (index);
            //14.11.16 notyet return Platform.getInstance ().findSceneNodeById (gameObject.transform.GetChild (index).gameObject.GetInstanceID ());

        }

        public int getChildCount ()
        {
            //return /*gameObject.*/transform.childCount;
	    //14.11.19 wegen Camera selber zaehlen.
	    return getChildren().size();
        }

        /**
     * MA17
     * @return
     */
        public List<NativeTransform> getChildren ()
        {
            List<NativeTransform> l = new ArrayList<NativeTransform> ();
            if (transform == null) {
                logger.warn ("getChildren: transform is null. Probably destroyed");
                return l;
            }
            
            int cnt = transform.childCount;

            for (int i = 0; i < cnt; i++) {
                UnityEngine.Transform child = transform.GetChild (i);
         	//11.11.19: Nicht eigene Camera mitzaehlen
		GameObject go = child.gameObject;
		//logger.debug("go="+go);
		//logger.debug("go.name="+go.name);
		if (!(go.name.EndsWith("Camera"))) {
                    l.add (new UnityBase3D (child, null, true));
                }
            }
            return l;
        }

        /**
     * 08.02.2015: Konzeptionell ist das etwas unrund.
     *
     * @param wireframe
     */
        virtual public void setWireframe (bool wireframe)
        {
            /*TODO Unity foreach (UnityObject3D c  in  children) {
                if (c is UnityMesh) {
                    ((UnityMesh)c).setWireframe (wireframe);
                } else {
                    c.setWireframe (wireframe);
                }
            }*/
        }

        public UnityEngine.Transform getTransform ()
        {
            return transform;
        }

	/**
	 * Wertebereich [0-31].
	 * 12.11.19: Ein GO kann nur in einem einzigen Layer sein.
	 * Das ist wohl anders als in der Camera keine Bitmask, sondern der Layerindex
	 * (https://docs.unity3d.com/ScriptReference/LayerMask.NameToLayer.html).
	 */
	public void setLayer (int layer)
        {
            GameObject go = transform.gameObject;
            //Apparently layer names can only be defined in the editor, not by scripting.
            //logger.debug ("setLayer " + layer + " in " + go.name);
            if (layer == 0) {
                transform.gameObject.layer = 0;
            } else {
                transform.gameObject.layer = 8 + layer;//LayerMask.layer;
            }
            // mark all subnodes, the complete subtree
            java.util.List<NativeTransform> children = getChildren();
            foreach (NativeTransform c in children){
                c.setLayer(layer);
            }
        }

	
	/**
	 * 12.11.19: Exakt gesetzten Layer zurueckliefern.?
	 * Das ist wohl anders als in der Camera keine Bitmask, sondern der Layerindex
	 */
        public int getLayer ()
        {
            //logger.debug ("found unity layer " + transform.gameObject.layer);
            if (transform.gameObject.layer < 8) {
                return 0;
            }
	    //12.11.19: Das gesetzte Bit liefern?
            //return 1 << (transform.gameObject.layer - 8);
            return  (transform.gameObject.layer - 8);
        }


    }
}
