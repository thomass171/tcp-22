using UnityEngine;
using System.Collections;
using System;
using java.lang;
using de.yard.threed.core.platform;
using de.yard.threed.core.resource;
using de.yard.threed.core.buffer;
using de.yard.threed.core;
using de.yard.threed.engine;
using de.yard.threed.engine.loader;
using java.util;
using System.Xml;
using SimpleJSON;

using de.yard.threed.engine.platform.common;
using de.yard.threed.outofbrowser;

namespace de.yard.threed.platform.unity
{
    /**
     *
     */
    public class PlatformUnity : Platform
    {
        // kann nicht ueber die Factory gebaut werden, weil die gerade noch initialisiert wird
        Log logger = new UnityLog (typeof (PlatformUnity));
        public NativeLogFactory logfactory;
        string hostdir;

        private PlatformUnity ()
        {
            logfactory = new UnityLogFactory ();

            hostdir = Environment.GetEnvironmentVariable("HOSTDIR");
            if (hostdir == null)
            {
                Debug.Break();
                throw new System.Exception("HOSTDIR not set");
            }
            Debug.Log("using HOSTDIR=" + hostdir);
        }

        public static PlatformInternals init (HashMap<String, String> props)
        {
            if (instance != null) {
                throw new System.Exception ("already inited");
            }
            instance = new PlatformUnity ();

            Set<String> keyset = props.keySet ();
            foreach (String key in keyset) {
                ((PlatformUnity)instance).properties.put (key, props.get (key));
            }
            UnityResourceManager resourceReader = new UnityResourceManager();
            instance.bundleResolver.add(new SimpleBundleResolver(((PlatformUnity)instance).hostdir + "/bundles", resourceReader));
            instance.bundleResolver.addAll(SyncBundleLoader.buildFromPath(Environment.GetEnvironmentVariable("ADDITIONALBUNDLE"), resourceReader));
            instance.bundleLoader = new AsyncBundleLoader (resourceReader);

            PlatformInternals platformInternals = new PlatformInternals ();
            return platformInternals;
        }

        override public NativeSceneNode buildModel ()
        {
            return buildModel (null);
        }

        /**
         *
         */
        override public NativeSceneNode buildModel (string name)
        {
            UnitySceneNode n = UnitySceneNode.build (name);
            // native2nativewrapper.put (n.getUniqueId (), n);
            return n;
        }

        override public void buildNativeModelPlain (BundleResource filename, ResourcePath opttexturepath, ModelBuildDelegate builddelegate, int options)
        {
            // verwendet nicht asyncjob, weil es ja intern ist. Unity mag evtl. kein modelload in MT. Darum einfach async um das
            // delayed testen zu koennen.
            int delegateid = UnitySceneRunner.getInstance ().invokeLater (builddelegate);

            AsyncHelper.asyncModelBuild (filename, opttexturepath, options, delegateid);
        }

        /*override public void loadBundle (String bundlename, /*AsyncJobCallback* /BundleLoadDelegate ldelegate, bool delayed)
        {
            AsyncHelper.asyncBundleLoad (bundlename, UnitySceneRunner.getInstance ().invokeLater (ldelegate), delayed);
        }*/

        override public List<NativeSceneNode> findSceneNodeByName (string name)
        {
            List<NativeSceneNode> l = new ArrayList<NativeSceneNode> ();
            GameObject go = GameObject.Find (name);
            if (go != null) {
                l.add (new UnitySceneNode (go, true));
            }
            return l;
        }

        override public NativeMesh buildNativeMesh (NativeGeometry geometry, NativeMaterial material, bool castShadow, bool receiveShadow)
        {
            UnityGeometry geo = (UnityGeometry)geometry;
            return UnityMesh.buildMesh (geo, (UnityMaterial)material, castShadow, receiveShadow);
        }

        override public NativeSceneNode buildLine (de.yard.threed.core.Vector3 from, de.yard.threed.core.Vector3 to, de.yard.threed.core.Color color)
        {

            UnitySceneNode n = UnitySceneNode.build ("");
            n.setLineMesh (Scene.getWorld ().mirrorZ (from), Scene.getWorld ().mirrorZ (to), color);
            return n;
        }

        override public void updateMesh (NativeMesh mesh, NativeGeometry geometry, NativeMaterial material)
        {
            ((UnityMesh)mesh).updateMesh ((UnityGeometry)geometry, (UnityMaterial)material);
        }

        override public NativeByteBuffer buildByteBuffer (int size)
        {
            SimpleByteBuffer buf = new SimpleByteBuffer (new byte [size]);
            return buf;
            //return new NativeVector3Array(buf,0,size);
        }

        override public NativeCamera buildPerspectiveCamera (double fov, double aspect, double near, double far)
        {
            return UnityCamera.buildPerspectiveCamera (fov, aspect, near, far, Settings.backgroundColor);
        }
       
        override public NativeMaterial buildMaterial (string name, HashMap<ColorType, de.yard.threed.core.Color> color, HashMap<string, NativeTexture> texture, HashMap<NumericType, NumericValue> parameter, /*MA36 TODO Effect*/System.Object effect)
        {
            return UnityMaterial.buildMaterial (new MaterialDefinition (name, color, texture, parameter), (Effect)effect);
        }

        //TODO: ob das so bleiben kann ist fraglich wegen analogie zu Ajax
        private NativeTexture buildNativeTextureUnity (NativeResource textureresource, HashMap<NumericType, NumericValue> parameter)
        {
            logger.debug ("buildNativeTexture " + textureresource.getName ());
            UnityTexture tex = UnityTexture.loadFromFile (textureresource);
            if (tex == null) {
                logger.warn ("Loading texture " + textureresource.getName () + " failed. Using default");
                return null;//defaulttexture;
            }
            /*TODO  NumericValue wraps = params.get(NumericType.TEXTURE_WRAP_S);
            if (wraps != null) {
                if (wraps.equals(NumericValue.REPEAT))
                    tex.texture.setWrap(Texture.WrapAxis.S, Texture.WrapMode.Repeat);
            }
            NumericValue wrapt = params.get(NumericType.TEXTURE_WRAP_T);
            if (wrapt != null) {
                if (wrapt.equals(NumericValue.REPEAT))
                    tex.texture.setWrap(Texture.WrapAxis.T, Texture.WrapMode.Repeat);
            }*/
            return tex;
        }

        /*4.10.18 override public void executeAsyncJobNurFuerRunnerhelper (AsyncJob job)
        {
            new UnityThread (job).Start ();
        }*/

        override public NativeTexture buildNativeTexture (ImageData imagedata, bool fornormalmap)
        {
            if (imagedata.buf != null) {
                return UnityTexture.buildFromImage ((imagedata.buf));
            }
            return UnityTexture.buildFromImage ((imagedata), fornormalmap);
        }

        override public NativeTexture buildNativeTexture (BundleResource filename, HashMap<NumericType, NumericValue> parameters)
        {
            if (filename.bundle == null) {
                logger.error ("bundle not set for file " + filename.getFullName ());
                return null;//defaulttexture;
            }
            String bundlebasedir;//= Platform.getInstance().getSystemProperty("BUNDLEDIR") + "/" + filename.bundle.name;
            FileSystemResource resource;//= FileSystemResource.buildFromFullString(bundlebasedir + "/" + filename.getFullName());
            //bundlebasedir = BundleRegistry.getBundleBasedir (filename.bundle.name, false);
            bundlebasedir = BundleResolver.resolveBundle(filename.bundle.name, Platform.getInstance().bundleResolver).getPath();
            resource = FileSystemResource.buildFromFullString (bundlebasedir + "/" + filename.getFullName ());
            return buildNativeTextureUnity (resource, parameters);
        }

        override public NativeTexture buildNativeTexture (NativeCanvas canvas)
        {
            UnityTexture texture = UnityTexture.buildFromImage (((UnityCanvas)canvas).image, false);
            return texture;
        }

        override public NativeLight buildPointLight (de.yard.threed.core.Color argb)
        {
            return UnityLight.buildPointLight (argb);
        }

        override public NativeLight buildAmbientLight (de.yard.threed.core.Color argb)
        {
            return UnityLight.buildAmbientLight (argb);
        }

        override public NativeLight buildDirectionalLight (de.yard.threed.core.Color argb, de.yard.threed.core.Vector3 direction)
        {
            return UnityLight.buildDirectionalLight (argb, direction);
        }

        override public NativeGeometry buildNativeGeometry (Vector3Array vertices, int [] indices, Vector2Array uvs, Vector3Array normals)
        {
            return UnityGeometry.buildGeometry (vertices, indices, uvs, normals);
        }

        /*override public NativeSceneRunner getSceneRunner ()
        {
            return runner;//19.3.16 UnitySceneRunner.getInstance();
        }*/

        override public Log getLog (System.Type clazz)
        {
            return logfactory.getLog (clazz);
        }

        /**
	     * Ob hier vielleicht ein Inputstream als Input guenstiger ist? Kennt der GWT Ressourceloader
	     * aber nicht.
         * // siehe z.B. http://answers.unity3d.com/questions/59466/loading-xml-file-from-resources-file-after-build.html
		 */
        override public NativeDocument parseXml (String xmltext)
        {
            try {
                //xmlStr = "<?xml version=\"1.0\" ?>\n<name>Oscar</name>";
                //xmlStr = StringUtils.buildString (StringUtils.getBytes(xmlStr));
                System.Xml.XmlDocument doc = new System.Xml.XmlDocument ();
                doc.LoadXml (xmltext);
                //Debug.Log (doc.OuterXml);

                return new UnityXmlDocument (doc);
            } catch (java.lang.Exception e) {
                // ob das mit dem Exceptionhandling Handundfuss hat, ist fraglich.
                throw new UnityXmlException (e);
            }
        }

        override public NativeJsonValue parseJson (String jsonstring)
        {
            JSONNode node = JSON.Parse (jsonstring);
            return new UnityJsonObject ((JSONObject)node);
        }

        /*override public System.Object parseJsonToModel<T> (String jsonstring, System.Type clazz)
        {
            System.Object model = JsonUtility.FromJson<T> (jsonstring);
            logger.debug ("parseJsonToModel " + jsonstring);

            return model;
        }

        override public String modelToJson<T> (System.Object model)
        {
            if (!typeof (T).IsSerializable) {
                throw new System.Exception ("model not serializable");
            }
            string json = JsonUtility.ToJson (model);
            //jsonString = JsonSerializer.Serialize<WeatherForecastWithPOCOs> (model);
            //JSONNode node = JSON.Parse (jsonstring);
            logger.debug ("modelToJson " + json);

            return json;
        }*/

        override public NativeCanvas buildNativeCanvas (int width, int height)
        {
            return new UnityCanvas (width, height);
        }

        override public NativeContentProvider getContentProvider (char type, string location, TestPdfDoc docid)
        {
            /* if (type == 'I') {
                return new PhotoAlbumContentProvider(new File(location));
            }*/
            if (type == 'D') {
                return new UnityPdfContentProvider (docid);
            }
            return (NativeContentProvider)de.yard.threed.core.Util.notyet ();
            // return new SampleContentProvider (11);
        }



        /**
         * 21.5.16: Der Unity TextMesh ist keien Alternative, weil er nicht wirklich ein Mesh ist. Und ansonsten
         * kann Unity nicht einfach in eine Textur schreiben. Echt bloed.
         */
        /*1.3.17 override
        public ImageData buildTextImage (String text, de.yard.threed.platform.common.Color textcolor, String font, int fontsize)
        {
            //Der Texturtyp muss auf "advanced" gesetzt werden und read/write aktiviert werden
            UnityTexture fonttex = (UnityTexture)UnityResourceManager.getInstance ().loadResourceSync (new BundleResource ("FontMap.png")).texture;
            ImageData fontmap = UnityTexture.buildImageData (fonttex.texture);
            //logger.debug (fontmap.countPixelNot0 () + " non 0 pixel in fontmap");

            ImageData image = ImageFactory.buildLabelImage (text, textcolor, fontmap);
            // versuchen wieder freizugeben
            fonttex.texture = null;
            Resources.UnloadUnusedAssets ();
            return image;
        }*/

        override public NativeSplineInterpolationFunction buildNativeSplineInterpolationFunction (double [] x, double [] y)
        {
            //        SplineInterpolator si = new SplineInterpolator();
            //      PolynomialSplineFunction fct = si.interpolate(x, y);
            //    return new JmeSplineInterpolationFunction(fct);
            return (NativeSplineInterpolationFunction)Util.notyet ();
        }

        override public NativeRay buildRay (de.yard.threed.core.Vector3 origin, de.yard.threed.core.Vector3 direction)
        {
            return new UnityRay (origin, direction);
        }

        /* override public NativeFile buildFile (string path)
        {
            return UnityFile.buildFile (path);
        }*/

        /*override public bool exists (NativeResource file)
        {
            return new UnityFile (file).exists ();
        }*/

        override public float getFloat (byte [] buf, int offset)
        {
            if (!BitConverter.IsLittleEndian) {
                Util.notyet ();
                return 0;
            }
            return System.BitConverter.ToSingle (buf, offset);
        }

        override public void setFloat (byte [] buf, int offset, float f)
        {
            if (!BitConverter.IsLittleEndian) {
                Util.notyet ();
                return;
            }
            byte [] buf1 = BitConverter.GetBytes (f);
            for (int i = 0; i < 4; i++) {
                buf [offset + i] = buf1 [i];
            }
            return;
        }


        override public double getDouble (byte [] buf, int offset)
        {
            if (!BitConverter.IsLittleEndian) {
                Util.notyet ();
                return 0;
            }
            return System.BitConverter.ToDouble (buf, offset);
        }

        public static UnityEngine.Color buildColor (de.yard.threed.core.Color col)
        {
            return new UnityEngine.Color (col.getR (), col.getG (), col.getB (), col.getAlpha ());
        }

        override public NativeStringHelper buildStringHelper ()
        {
            return new UnityStringHelper ();
        }

        override public long currentTimeMillis ()
        {
            return UnityUtil.currentTimeMillis ();//(long)(DateTime.UtcNow - Jan1st1970).TotalMilliseconds;
        }

        /**
         * Pruefen, ob eine Taste/Button gedrueckt ist. Das kann Unity einfach so
         * und braucht nicht die pressedkeys Liste. Die wird allerdings zusaetzlich
         * für keys verwendet, die ueber Network eingespielt werden.
         */
        override public bool GetKeyDown (int keycode)
        {
            Nullable<UnityEngine.KeyCode> ucode = map2UnityKeycode (keycode);
            if (ucode == null) {
                return false;
            }
            // Mappen der Joysticktasten bzw. BT Controller Pfeiltasten) auf Arrowkeys
            if (ucode.Value == UnityEngine.KeyCode.LeftArrow) {
                bool jst = UnityEngine.Input.GetKeyDown (UnityEngine.KeyCode.JoystickButton2);
                if (jst) {
                    return true;
                }
            }
            if (ucode.Value == UnityEngine.KeyCode.RightArrow) {
                bool jst = UnityEngine.Input.GetKeyDown (UnityEngine.KeyCode.JoystickButton1);
                if (jst) {
                    return true;
                }
            }
            if (ucode.Value == UnityEngine.KeyCode.DownArrow) {
                bool jst = UnityEngine.Input.GetKeyDown (UnityEngine.KeyCode.JoystickButton0);
                if (jst) {
                    return true;
                }
            }
            if (ucode.Value == UnityEngine.KeyCode.UpArrow) {
                bool jst = UnityEngine.Input.GetKeyDown (UnityEngine.KeyCode.JoystickButton3);
                if (jst) {
                    return true;
                }
            }
            // Check, ob etwas ueber UDP reingekommen ist
            Boolean pressedbyudp = ((UnitySceneRunner)AbstractSceneRunner.getInstance ())./*runnerhelper.*/keyPressed (keycode);
            if (pressedbyudp) {
                return true;
            }
            return UnityEngine.Input.GetKeyDown (ucode.Value);
        }

        /**
         * * (0,0) ist wie bei getMouseMove() links unten.
         * Verwendung von "Up", um auch mal Draggen zu koennen.
         */
        override public Point getMouseClick ()
        {
            if (UnityEngine.Input.GetMouseButtonUp (0)) {
                return getMousePosition ();
            }
            return null;
        }

        override public Point getMousePress ()
        {
            //16.8.19: Unklar, ob das richtig ist fuer press
            if (UnityEngine.Input.GetMouseButtonDown (0)) {
                return getMousePosition ();
            }
            return null;
        }

        /**
         * Kann solange abgefragt werden, wie die Taste gedrückt ist.
         *
         * @return
         */
        override public bool GetKey (int keycode)
        {
            Nullable<UnityEngine.KeyCode> ucode = map2UnityKeycode (keycode);
            if (ucode == null) {
                return false;
            }
            return UnityEngine.Input.GetKey (ucode.Value);
        }

        override public Point getMouseMove ()
        {
            float mousexThreshold = 0.02f;
            float mouseyThreshold = 0.02f;
            float mousex = UnityEngine.Input.GetAxis ("Mouse X");
            float mousey = UnityEngine.Input.GetAxis ("Mouse Y");

            bool mousemoved = false;
            //int x = (int)Math.Round (mousex);
            //int y = (int)Math.Round (mousey);
            if (mousex > mousexThreshold) {
                // Code for mouse moving right
                mousemoved = true;
            } else if (mousex < -mousexThreshold) {
                // Code for mouse moving left
                mousemoved = true;
            } else {
                // Code for mouse standing still
            }

            if (mousey > mouseyThreshold) {
                // Code for mouse moving forward
                mousemoved = true;
            } else if (mousey < -mouseyThreshold) {
                // Code for mouse moving backward
                mousemoved = true;
            } else {
                // Code for mouse standing still
            }

            //  UnityEngine.Vector3 v = UnityEngine.Input.mousePosition;
            if (mousemoved) {
                return getMousePosition ();
            }
            return null;
        }

        private Point getMousePosition ()
        {
            UnityEngine.Vector3 v = UnityEngine.Input.mousePosition;
            //logger.debug ("omousePosition=" + v);
            int x = (int)Math.Round (v.x);
            int y = (int)Math.Round (v.y);

            return new Point (x, y);
        }

        override public String getName ()
        {
            return "Unity";
        }

        /**
         * Solange es kein besseres Konzep gibt, einfach über hashmap nachbilden.
         */
        HashMap<String, String> properties = new HashMap<String, String> ();

        public override void setSystemProperty (String key, String value)
        {
            properties.put (key, value);
        }

        override public String getSystemProperty (String key)
        {
            return properties.get (key);
        }

        override public NativeEventBus getEventBus ()
        {
            return UnityEventBus.getInstance ();
        }



        private Nullable<UnityEngine.KeyCode> map2UnityKeycode (int keycode)
        {
            switch (keycode) {
            case de.yard.threed.engine.KeyCode.KEY_ZERO:
                return UnityEngine.KeyCode.Alpha0;
            case de.yard.threed.engine.KeyCode.KEY_ONE:
                return UnityEngine.KeyCode.Alpha1;
            case de.yard.threed.engine.KeyCode.KEY_TWO:
                return UnityEngine.KeyCode.Alpha2;
            case de.yard.threed.engine.KeyCode.KEY_THREE:
                return UnityEngine.KeyCode.Alpha3;
            case de.yard.threed.engine.KeyCode.KEY_FOUR:
                return UnityEngine.KeyCode.Alpha4;
            case de.yard.threed.engine.KeyCode.KEY_FIVE:
                return UnityEngine.KeyCode.Alpha5;
            case de.yard.threed.engine.KeyCode.KEY_SIX:
                return UnityEngine.KeyCode.Alpha6;
            case de.yard.threed.engine.KeyCode.KEY_SEVEN:
                return UnityEngine.KeyCode.Alpha7;
            case de.yard.threed.engine.KeyCode.KEY_EIGHT:
                return UnityEngine.KeyCode.Alpha8;
            case de.yard.threed.engine.KeyCode.KEY_NINE:
                return UnityEngine.KeyCode.Alpha9;
            case de.yard.threed.engine.KeyCode.KEY_A:
                return UnityEngine.KeyCode.A;
            case de.yard.threed.engine.KeyCode.KEY_B:
                return UnityEngine.KeyCode.B;
            case de.yard.threed.engine.KeyCode.KEY_C:
                return UnityEngine.KeyCode.C;
            case de.yard.threed.engine.KeyCode.KEY_D:
                return UnityEngine.KeyCode.D;
            case de.yard.threed.engine.KeyCode.KEY_E:
                return UnityEngine.KeyCode.E;
            case de.yard.threed.engine.KeyCode.KEY_F:
                return UnityEngine.KeyCode.F;
            case de.yard.threed.engine.KeyCode.KEY_G:
                return UnityEngine.KeyCode.G;
            case de.yard.threed.engine.KeyCode.KEY_H:
                return UnityEngine.KeyCode.H;
            case de.yard.threed.engine.KeyCode.KEY_K:
                return UnityEngine.KeyCode.K;
            case de.yard.threed.engine.KeyCode.KEY_L:
                return UnityEngine.KeyCode.L;
            case de.yard.threed.engine.KeyCode.KEY_M:
                return UnityEngine.KeyCode.M;
            case de.yard.threed.engine.KeyCode.KEY_N:
                return UnityEngine.KeyCode.N;
            case de.yard.threed.engine.KeyCode.KEY_O:
                return UnityEngine.KeyCode.O;
            case de.yard.threed.engine.KeyCode.KEY_P:
                return UnityEngine.KeyCode.P;
            case de.yard.threed.engine.KeyCode.KEY_Q:
                return UnityEngine.KeyCode.Q;
            case de.yard.threed.engine.KeyCode.KEY_R:
                return UnityEngine.KeyCode.R;
            case de.yard.threed.engine.KeyCode.KEY_S:
                return UnityEngine.KeyCode.S;
            case de.yard.threed.engine.KeyCode.KEY_T:
                return UnityEngine.KeyCode.T;
            case de.yard.threed.engine.KeyCode.KEY_U:
                return UnityEngine.KeyCode.U;
            case de.yard.threed.engine.KeyCode.KEY_V:
                return UnityEngine.KeyCode.V;
            case de.yard.threed.engine.KeyCode.KEY_W:
                return UnityEngine.KeyCode.W;
            case de.yard.threed.engine.KeyCode.KEY_X:
                return UnityEngine.KeyCode.X;
            case de.yard.threed.engine.KeyCode.KEY_Y:
                return UnityEngine.KeyCode.Y;
            case de.yard.threed.engine.KeyCode.KEY_Z:
                return UnityEngine.KeyCode.Z;
            case de.yard.threed.engine.KeyCode.KEY_SPACE:
                return UnityEngine.KeyCode.Space;
            case de.yard.threed.engine.KeyCode.KEY_PAGEUP:
                return UnityEngine.KeyCode.PageUp;
            case de.yard.threed.engine.KeyCode.KEY_PAGEDOWN:
                return UnityEngine.KeyCode.PageDown;
            case de.yard.threed.engine.KeyCode.KEY_PLUS:
                return UnityEngine.KeyCode.Plus;
            case de.yard.threed.engine.KeyCode.KEY_DASH:
                return UnityEngine.KeyCode.Minus;
            case de.yard.threed.engine.KeyCode.KEY_TAB:
                return UnityEngine.KeyCode.Tab;
            case de.yard.threed.engine.KeyCode.KEY_SHIFT:
                return UnityEngine.KeyCode.LeftShift;
            case de.yard.threed.engine.KeyCode.KEY_CTRL:
                return UnityEngine.KeyCode.LeftControl;
            case de.yard.threed.engine.KeyCode.KEY_LEFT:
                return UnityEngine.KeyCode.LeftArrow;
            case de.yard.threed.engine.KeyCode.KEY_RIGHT:
                return UnityEngine.KeyCode.RightArrow;
            case de.yard.threed.engine.KeyCode.KEY_UP:
                return UnityEngine.KeyCode.UpArrow;
            case de.yard.threed.engine.KeyCode.KEY_DOWN:
                return UnityEngine.KeyCode.DownArrow;

            default:
                logger.warn ("unknown keycode " + keycode);
                return null;
            }
        }


        override
        public void abort ()
        {
            de.yard.threed.core.Util.notyet ();

        }

        override public bool isDevmode ()
        {
            //oder?
            return true;
        }

        protected override Log getLog ()
        {
            return logger;
        }

        override public NativeVRController getVRController (int index)
        {
            return null;
        }

        override public NativeRenderProcessor buildSSAORenderProcessor ()
        {
            Util.notyet ();
            return null;
        }

        override public void addRenderProcessor (NativeRenderProcessor renderProcessor)
        {
            Util.notyet ();
        }

        override public NativeScene getScene ()
        {
            return nativeScene;
        }
    }

    public class UnityThread
    {
        private bool m_IsDone = false;
        private object m_Handle = new object ();
        private System.Threading.Thread m_Thread = null;
        AsyncJob job;
        static Log logger = PlatformUnity.getInstance ().getLog (typeof (UnityThread));

        public UnityThread (AsyncJob job)
        {
            this.job = job;
        }

        public virtual void Start ()
        {
            m_Thread = new System.Threading.Thread (Run);
            m_Thread.Start ();
            logger.debug ("job started");

        }

        private void Run ()
        {
            try {
                logger.debug ("job running");

                string msg = job.execute ();
                if (Config.isAsyncdebuglog()) {

                    logger.debug ("job completed");
                }
                UnitySceneRunner.getInstance ().addCompletedJob (new CompletedJob (job, msg));
            } catch (java.lang.Exception e) {
                UnitySceneRunner.getInstance ().addCompletedJob (new CompletedJob (job, e.getMessage ()));
            }

        }

    }
}
