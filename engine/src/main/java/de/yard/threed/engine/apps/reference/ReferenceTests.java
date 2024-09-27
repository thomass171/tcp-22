package de.yard.threed.engine.apps.reference;

import de.yard.threed.core.Degree;
import de.yard.threed.core.Dimension;
import de.yard.threed.core.JsonHelper;
import de.yard.threed.core.MathUtil2;
import de.yard.threed.core.Matrix3;
import de.yard.threed.core.Matrix4;
import de.yard.threed.core.Point;
import de.yard.threed.core.Quaternion;
import de.yard.threed.core.StringUtils;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.NativeCollision;
import de.yard.threed.core.platform.NativeJsonObject;
import de.yard.threed.core.platform.NativeJsonValue;
import de.yard.threed.core.platform.NativeSceneNode;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.Assert;
import de.yard.threed.core.testutil.RuntimeTestUtil;
import de.yard.threed.engine.Camera;
import de.yard.threed.engine.FirstPersonController;
import de.yard.threed.engine.FirstPersonTransformer;
import de.yard.threed.engine.Mesh;
import de.yard.threed.engine.Ray;
import de.yard.threed.engine.SceneNode;
import de.yard.threed.engine.Transform;
import de.yard.threed.engine.gui.FovElement;
import de.yard.threed.engine.platform.common.AbstractSceneRunner;
import de.yard.threed.engine.platform.common.Settings;

import java.util.ArrayList;
import java.util.List;

import static de.yard.threed.core.Matrix4.cos;
import static de.yard.threed.core.Matrix4.sin;

public class ReferenceTests {

    static Log logger = Platform.getInstance().getLog(de.yard.threed.engine.apps.reference.ReferenceTests.class);

    /**
     * Pickingray aus der Default Camera Position (0,5,11) genau in die Mitte der Scene (mousevector =0,0,0.5)
     */
    public static void testRay(Dimension dim, Camera camera) {
        logger.info("testRay");
        Ray ray = camera.buildPickingRay(camera.getCarrier().getTransform(), new Point(dim.width / 2, dim.height / 2)/*, dim*/);
        logger.debug("ray origin=" + ray.getOrigin().dump("") + " for dimension " + dim.toString());
        logger.debug("ray direction=" + ray.getDirection().dump("") + " for dimension " + dim.toString());
        logger.debug("viewer matrix=" + camera.getViewMatrix().dump("\n"));
        logger.debug("projection matrix=" + camera.getProjectionMatrix().dump("\n"));
        // Bei Blickrichtung 0,0,0 sind die Werte plausibel, x ist nichts, y leicht nach unten und z halt nach hinten
        Vector3 expecteddirection = new Vector3(0, -0.4138f, -0.91f);
        double distance = Vector3.getDistance(expecteddirection, ray.getDirection());
        if (distance > 0.1f) {
            Assert.fail("test failed: expecteddirection deviation =" + distance);
        }
        RuntimeTestUtil.assertVector3("ray origin", new Vector3(0, 5, 11), ray.getOrigin());

        // Ein Ray etwas schraeg nach rechts unten (die Referenzwerte beziehen sich auf dim 800x600)
        ray = camera.buildPickingRay(camera.getCarrier().getTransform(), new Point(dim.width / 2 + 90, dim.height / 2 + 40)/*, dim*/);
        logger.debug("2.ray direction=" + ray.getDirection() + " for dimension " + dim);
        logger.debug("2.viewer matrix=" + camera.getViewMatrix().dump("\n"));
        logger.debug("2.projection matrix=" + camera.getProjectionMatrix().dump("\n"));
        expecteddirection = new Vector3(0.12313132f, -0.36020958f, -0.92470956f);
        distance = Vector3.getDistance(expecteddirection, ray.getDirection());
        if (distance > 0.1f) {
            if (!isUnity()) {
                // Die Groesse 800x600 ist bei Unity nicht so vorhanden, dann passen die Refwerte auch nicht.
                Assert.fail("test failed: expecteddirection deviation =" + distance);
            }
        }
        // ray to controlMenu should find that intersection (important for ThreeJS special way of raycaster works)
        ray = new Ray(ReferenceScene.INITIAL_CAMERA_POSITION, new Vector3(0.0012989715968508482, -0.6979321644779659, -0.7161626955237842));
        // hits button, backplane and ground. In webgl its 7 (button and backplane three times each)??
        List<NativeCollision> intersections = ray.getIntersections();
        for (NativeCollision nc : intersections) {
            logger.debug("Hit " + nc.getSceneNode().getName());
        }
        RuntimeTestUtil.assertTrue("controlMenu ray intersections", intersections.size() >= 3);
    }

    public static void mvpTest(Camera camera, Dimension screensize, boolean usedeferred) {
        logger.info("mvpTest");
        RuntimeTestUtil.assertVector3("default camera position (carrier)", new Vector3(0, 5, 11), camera.getCarrierPosition());

        // Die Referenzwerte hängen von den camra Einstellungen (z.B. aspect) ab und muessen deshalb berechnet werden.
        Matrix4 projectionmatrixexpected = Camera.createPerspectiveProjection(camera.getFov(), camera.getAspect(), camera.getNear(), camera.getFar());
        Matrix4 pm = camera.getProjectionMatrix();
        logger.debug("projection matrix=\n" + pm.dump("\n"));
        //24.9.19: Der Test scheitert, wenn das Fenster nicht 4:3 ist (800x600), z.B. in WebGL.
        RuntimeTestUtil.assertMatrix4(projectionmatrixexpected, pm);

        // Die Referenzwerte stammen aus ThreeJS (default camera position)
        Matrix4 viewmatrixexpected = new Matrix4(
                1, 0, 0, 0,
                0, 0.91f, -0.414f, 0,
                0, 0.414f, 0.91f, -12.083f,
                0, 0, 0, 1);
        Matrix4 vm = camera.getViewMatrix();
        logger.debug("viewer matrix=\n" + vm.dump("\n"));
        RuntimeTestUtil.assertMatrix4(viewmatrixexpected, camera.getViewMatrix());

        Quaternion expectedcamrot = new Quaternion(-0.212f, 0, 0, 0.977f);
        Matrix4 rm;
        rm = new Matrix4();
        rm = Matrix4.buildRotationMatrix(expectedcamrot);
        logger.debug("rm=" + rm.dump("\n"));
        logger.debug("rmi=" + rm.getInverse().dump("\n"));
        double[] rmiangles = new double[3];
        rm.getInverse().extractQuaternion().toAngles(rmiangles);
        logger.debug("rmiangles=" + rmiangles[0] + " " + rmiangles[1] + " " + rmiangles[2]);
        logger.debug("rmi gegenprobe1=" + Matrix4.buildRotationXMatrix(Degree.buildFromRadians(rmiangles[0])).dump("\n"));
        Quaternion camrot = camera.getCarrier().getTransform().getRotation();
        logger.debug("camrot=" + camrot.dump(""));
        double[] angles = new double[3];
        camrot.toAngles(angles);
        logger.debug("camrot angles=" + angles[0] + " " + angles[1] + " " + angles[2]);
        RuntimeTestUtil.assertEquals("aspect", (double) screensize.width / screensize.height, camera.getAspect());
        RuntimeTestUtil.assertEquals("fov", Settings.defaultfov, camera.getFov());
        RuntimeTestUtil.assertEquals("far", 50/*Settings.defaultfar*/, camera.getFar());
        RuntimeTestUtil.assertEquals("near", Settings.defaultnear, camera.getNear());
        Matrix4 cameraworldmatrix = camera.getWorldModelMatrix();
        RuntimeTestUtil.assertVector3("extracted camera position scale", new Vector3(0, 5, 11), cameraworldmatrix.extractPosition());
        RuntimeTestUtil.assertEquals("camera.name", "Main Camera", camera.getName());
        RuntimeTestUtil.assertEquals("camera.carrier.name", "Main Camera Carrier", camera.getCarrier().getName());
        RuntimeTestUtil.assertNotNull("camera.parent", camera.getCarrier().getParent());
        RuntimeTestUtil.assertEquals("camera.parent.name", "World", camera.getCarrier().getParent().getName());
        //Wieso 2? Evtl. wegen GUI?. 7.10.19: Die default und die vom HUD (Layer 1 deferred). Und die xplizite deferred (layer 2) dazu.
        RuntimeTestUtil.assertEquals("cameras", 2 + ((usedeferred) ? 1 : 0), AbstractSceneRunner.instance.getCameras().size());

        if (usedeferred) {
            List<Transform> camchildren = camera.getCarrier().getTransform().getChildren();
            SceneNode secondcarrier = null;
            for (int i = 0; i < camchildren.size(); i++) {
                logger.debug("camcarrier child " + i + ":" + camchildren.get(i).getSceneNode().getName());
                if (StringUtils.startsWith(camchildren.get(i).getSceneNode().getName(), "deferred")) {
                    secondcarrier = camchildren.get(i).getSceneNode();
                }
            }
            // ist 5, weil FOV, gui, button, maincamera auch drin sind, und dann noch deferred cam
            // 3.12.18: nur noch drei, fov gui button sind an eigener camera, dafuer aber hud carrier
            // 24.9.19: Aber der main carrier ist doch nicht sein eigener Child. Duerften doch nur zwei sein: deferred carrier und hud carrier. JME darf nicht seine
            // CameraNode mitzaehlen (und Unity seine Camera). Die Camera selber ist ja kein Child, sondern Component.
            // 11.11.19: Also bleiben Hud und deferred-camera Carrier, obwohl es wieder fraglich erscheint, das main camera nicht dabei ist.
            // Ich glaube, ich nehme die MainCam wieder auf (2->3). 15.11.19: Doch wieder nicht, in WebGl führt dazu Kruecken.
            RuntimeTestUtil.assertEquals("camera children", 2, camchildren.size());
            SceneNode hudcarrier = FovElement.getDeferredCamera(null).getCarrier();
            // am Hud-carrier sind Hud,control//button und?? Hat der mal die Camera mitgezaehlt? Ich komm nur noch auf 2. 11.11.19: Wieder 3 durch mitzaehlen Camera
            List<Transform> hudcarrierchildren = hudcarrier.getTransform().getChildren();
            for (int i = 0; i < hudcarrierchildren.size(); i++) {
                logger.debug("hudcarrierchildren child " + i + ":" + hudcarrierchildren.get(i).getSceneNode().getName());
            }
            RuntimeTestUtil.assertEquals("hud carrier children", 2/*7.10.19 3*/, hudcarrier.getTransform().getChildren().size());

            RuntimeTestUtil.assertNotNull("getSecond carrier", secondcarrier);
            RuntimeTestUtil.assertVector3("getSecond camera position", new Vector3(0, 0, 0), secondcarrier.getTransform().getPosition());
            RuntimeTestUtil.assertQuaternion("getSecond camera rotation", new Quaternion(), secondcarrier.getTransform().getRotation());
            Camera scam = secondcarrier.getCamera();
            RuntimeTestUtil.assertNotNull("getSecond carriers camera not set", scam);
            RuntimeTestUtil.assertEquals("deferred camera name", "deferred-camera", scam.getName());
            logger.debug("deferred asserted");
        }
    }

    public static void testOriginalScale(SceneNode box) {
        logger.debug("box.scale=" + box.getTransform().getScale());
        Vector3 expectedscale = new Vector3(0.5f, 0.5f, 0.5f);
        if (Vector3.getDistance(expectedscale, box.getTransform().getScale()) > 0.1f) {
            throw new RuntimeException("test expectedscale failed");
        }
        /*if (box.object3d.getScale().getX() != 0.5f) {
            throw new RuntimeException("test expectedscale failed");
        }*/
    }

    public static void testParent(SceneNode m, SceneNode expectedparent) {
        if (m.getTransform().getParent() == null) {
            throw new RuntimeException("parent isType null");
        }
        SceneNode p = m.getTransform().getParent().getSceneNode();

        if (!p.getName().equals(expectedparent.getName())) {
            throw new RuntimeException("unexpected parent " + expectedparent.getName() + " of " + m.getTransform().getParent().getSceneNode().getName());
        }

    }

    public static void testExtracts(SceneNode movebox) {
        Matrix4 currentmodel = (movebox.getTransform().getLocalModelMatrix());
        logger.debug("currentmodel m=\n" + currentmodel.dump("\n"));
        Matrix4 modelexpected = new Matrix4(0.5f, 0, 0, 0,
                0, 0.5f, 0, 0.75f,
                0, 0, 0.5f, 0,
                0, 0, 0, 1);
        RuntimeTestUtil.assertMatrix4(modelexpected, currentmodel);

        Vector3 scale = currentmodel.extractScale();
        RuntimeTestUtil.assertVector3("movebox scale", new Vector3(0.5f, 0.5f, 0.5f), scale);

        // Die movebox hat ja keine eigene Rotation
        Matrix3 rotation = currentmodel.extractRotation();
        Matrix3 rotationexpected = new Matrix3(
                1, 0, 0,
                0, 1, 0,
                0, 0, 1);
        RuntimeTestUtil.assertMatrix3("movebox rotation", rotationexpected, rotation);
        Quaternion rot = currentmodel.extractQuaternion();
        Quaternion rotexpected = new Quaternion(0, 0, 0, 1);
        RuntimeTestUtil.assertQuaternion("movebox rot", rotexpected, rot);
    }

    public static void testIntersect(ArrayList<SceneNode> towerrechts, SceneNode movingbox) {
        logger.info("intersectionTest");

        // Jetzt ein Ray, der die grosse rote Box trifft und einer für die moving box.
        // Warum jeweils nur eine intersection kommt, mag ThreeJS spezifisch sein.

        Ray raycasterredbox;
        List<NativeCollision> intersects;
        // Geht in Unity trotz groesserer Toleranzen nicht, evtl. weil Ray Origin im screen statt in camera ist?
        // 2.3.17: Jetzt durch Nutzung Rayhelper aber nicht mehr. Allerdings dürften diese fixen Werte nicht zum Unity aspect passen und damit scheitern.
        if (!isUnity()) {
            raycasterredbox = new Ray(new Vector3(0, 5, 11), new Vector3(0.23f, -0.37f, -0.9f));
            intersects = raycasterredbox.getIntersections(towerrechts.get(0), true);
            //liefert 1 oder 2
            if (intersects.size() == 0) {
                Assert.fail("no red box intersection found(1)");
            }
            //wenn er zwei findet, ist etwas unklar, welchen zu pruefen. Einer muss es sein.
            boolean boxHit = false;
            for (int index = 0; index < intersects.size(); index++) {
                logger.debug("redbox.intersect=" + (intersects.get(index).getPoint()).dump(" "));
                if (Vector3.getDistance(new Vector3(3.19f, -0.14f, -1.5f), intersects.get(index).getPoint()) < 0.1) {
                    boxHit = true;
                }
            }
            if (!boxHit) {
                Assert.fail("redbox invalid intersection(s)");
            }

            Ray raycastermovingbox = new Ray(new Vector3(0, 5, 11), new Vector3(0.26f, -0.25f, -0.93f));
            intersects = raycastermovingbox.getIntersections(movingbox, true);
            //liefert 1 oder 2
            if (intersects.size() == 0) {
                Assert.fail("no moving box intersection found");
            }
            logger.debug("movingbox.intersect=" + (intersects.get(0).getPoint()).dump(" "));
            // Bei JME und Unity kommt es zu deutlichen Rundungsfehlern oder einfach anderen Resultaten,
            // evtl. weil Ray Origin im screen statt in camera ist?
            // Darum groessere Toleranz. Ob das so ganz richtig ist, muss sich noch zeigen. Ist aber erstmal besser als nichts.
            // Bei WebGl ist das der Punkt 0. Alles irgendwie nicht konsistent.
            int pindex = 1;
            if (isWebGl()) {
                pindex = 0;
            }
            RuntimeTestUtil.assertVector3("movingbox.intersect", new Vector3(3.9f, 1.25f, -2.95f), (intersects.get(pindex).getPoint()), 0.5f);
        }

        // Und jetzt einer von ganz weit weg, der die rote Box im Center treffen muesste.
        Vector3 redboxpos = new Vector3(4, 0, -3);
        // Unity trifft bei 1000*1000 nicht mehr, nur bis 1000*100.
        double len = 1000 * 100;
        Vector3 campos = new Vector3(4 * len, 5 * len, 11 * len);
        raycasterredbox = new Ray(campos, redboxpos.subtract(campos));
        intersects = raycasterredbox.getIntersections(towerrechts.get(0), true);
        //liefert 1 oder 2
        if (intersects.size() == 0) {
            Assert.fail("no red box intersection found(2)");
        }
        RuntimeTestUtil.assertEquals("name", "rechts 0", intersects.get(0).getSceneNode().getName());
        logger.debug("redbox.intersect=" + (intersects.get(0).getPoint()).dump(" "));
        intersects = raycasterredbox.getIntersections();
        //liefert 1 oder 2 (Ground doch vielleicht auch?)
        if (intersects.size() == 0) {
            Assert.fail("no red box intersection found(3)");
        }
        assertIntersection(intersects, "rechts 0");
    }

    private static void assertIntersection(List<NativeCollision> intersects, String expectedname) {
        for (NativeCollision nc : intersects) {
            if (nc.getSceneNode() == null) {
                Assert.fail("no mesh");
            }
            if (nc.getSceneNode().getName() == null) {
                Assert.fail("no mesh name");
            }
            if (nc.getSceneNode().getName().equals(expectedname))
                return;
        }
        Assert.fail("expected intersection found:" + expectedname);
    }

    public static void testMovingboxView(ReferenceScene rs) {
        logger.info("testMovingboxView");
        rs.controller.stepTo(1);

        Matrix4 mboxworldmatrix = (rs.getMovingbox().getTransform().getWorldModelMatrix());
        logger.debug("moving box worldmatrix=\n" + mboxworldmatrix.dump("\n"));
        Matrix4 expectedmboxworldmatrix = new Matrix4(
                0.25f, 0, 0, 4,
                0, 0.25f, 0, 1.125f,
                0, 0, 0.25f, -3,
                0, 0, 0, 1);
        RuntimeTestUtil.assertMatrix4(expectedmboxworldmatrix, mboxworldmatrix);

        // Die Referenzwerte stammen aus ThreeJS(?)
        Matrix4 cameraworldmatrix = rs.getMainCamera().getWorldModelMatrix();
        logger.debug("camera worldmatrix=\n" + cameraworldmatrix.dump("\n"));
        Matrix4 expectedcameraworldmatrix = new Matrix4(
                0, 0, 0.25f, 4.625f,
                0, 0.25f, 0, 1.65f,
                -0.25f, 0, 0, -3,
                0, 0, 0, 1);
        RuntimeTestUtil.assertMatrix4(expectedcameraworldmatrix, cameraworldmatrix);

        // Die Referenzwerte stammen aus ThreeJS
        Matrix4 viewmatrixexpected = new Matrix4(
                0, 0, -4, -12,
                0, 4, 0, -6.6f,
                4, 0, 0, -18.5f,
                0, 0, 0, 1);
        Matrix4 vm = rs.getMainCamera().getViewMatrix();
        logger.debug("viewer matrix moving box=" + vm.dump("\n"));
        RuntimeTestUtil.assertMatrix4(viewmatrixexpected, vm);

        RuntimeTestUtil.assertEquals("camera.name", "Main Camera", rs.getMainCamera().getName());
        RuntimeTestUtil.assertEquals("camera.parent.name", "Main Camera Carrier", rs.getMainCamera().getCarrier().getName());
        RuntimeTestUtil.assertEquals("camera.parent.name", "rechts 2", rs.getMainCamera().getCarrier().getParent().getName());

        // zurueck auf Anfang
        rs.controller.stepTo(rs.controller.viewpointList.size() - 1);

        //4.11.19 auch mal setMesh hier testen
        Mesh earthmesh = rs.earth.getMesh();
        RuntimeTestUtil.assertNotNull("earth.mesh", earthmesh);
        rs.earth.setMesh(earthmesh);
        //12.11.19: und auch getLayer()
        int layer = rs.hiddencube.getTransform().getLayer();
        RuntimeTestUtil.assertEquals("hiddencube.layer", rs.HIDDENCUBELAYER, layer);
        layer = rs.deferredcamera.getLayer();
        RuntimeTestUtil.assertEquals("deferredcamera.layer", rs.HIDDENCUBELAYER, layer);
    }

    public static void testRayFromFarAway(Dimension dim, ReferenceScene rs) {
        logger.info("testRayFromFarAway");
        rs.controller.stepTo(6);
        //Nicht per Rayhelper, der ist ja in allen Platformen der gleiche
        //RayHelper rayhelper = new RayHelper(rs.getMainCamera().getNativeCamera());
        Ray pickingray = rs.getMainCamera().buildPickingRay(rs.getMainCamera().getCarrier().getTransform(), new Point(dim.width / 2, dim.height / 2));

        // die direction muss mich bei richiger LAenge wieder in den Ursparung fuehren.
        Vector3 camposition = rs.getMainCamera().getCarrierPosition();
        // bin ich ueberhaupt richtig
        logger.debug("camposition=" + camposition);
        RuntimeTestUtil.assertVector3("", new Vector3(50000, 30000, 20000), camposition);
        Vector3 camworldposition = rs.getMainCamera().getWorldModelMatrix().transform(camposition);
        //22.3.18: Keine Ahnung warum die worldmodel matrix anders ist
        logger.debug("camworldposition=" + camworldposition);
        RuntimeTestUtil.assertVector3("", new Vector3(71236f, 65941, -25357), camworldposition, 0.5f);
        //TODO und wsarum komme ich nicht nach 0,0,0? Und Riesentoleranz fuer ThreeJS? Aber alle Platformen sind sich da einig.
        Vector3 target = camworldposition.add(pickingray.getDirection().multiply(camworldposition.length()));
        RuntimeTestUtil.assertVector3("target", new Vector3(-10758, 18599, -58545), target, 2000f);
        // zurueck auf Anfang
        logger.debug("Stepping back");
        // TODO 8.22 mving a carrier

        rs.controller.stepTo(rs.controller.viewpointList.size() - 1);
        logger.debug("Stepped back");
    }


    public static void testPyramideBackLeftFront(SceneNode pyramideblf) {
        logger.info("testPyramideBackLeftFront");
        //Referenzwerte aus ThreeJS
        Matrix4 pyramidworldmatrix = (pyramideblf.getTransform().getWorldModelMatrix());
        logger.debug("pyramid worldmatrix=\n" + pyramidworldmatrix.dump("\n"));
        Matrix4 expectedpyramidworldmatrix = new Matrix4(
                0, -1, 0, 1.5f,
                0, 0, 1, 0,
                -1, 0, 0, 3,
                0, 0, 0, 1);
        RuntimeTestUtil.assertMatrix4(expectedpyramidworldmatrix, pyramidworldmatrix);

        // Die local Matrix ist gleich der world matrix.
        Matrix4 pyramidlocalmatrix = (pyramideblf.getTransform().getLocalModelMatrix());
        logger.debug("pyramid localmatrix=\n" + pyramidlocalmatrix.dump("\n"));
        Matrix4 expectedpyramidlocalmatrix = new Matrix4(
                0, -1, 0, 1.5f,
                0, 0, 1, 0,
                -1, 0, 0, 3,
                0, 0, 0, 1);
        RuntimeTestUtil.assertMatrix4(expectedpyramidlocalmatrix, pyramidlocalmatrix);

        Quaternion pyramidrotation = pyramideblf.getTransform().getRotation();
        logger.debug("pyramid rotation=\n" + pyramidrotation.dump(" "));
        Quaternion expectedrotation = new Quaternion(-0.5f, 0.5f, 0.5f, 0.5f);
        RuntimeTestUtil.assertQuaternion("pyramidrotation", expectedrotation, pyramidrotation);
    }

    public static boolean isUnity() {
        boolean isunity = Platform.getInstance().getName().equals("Unity");
        return isunity;
    }

    public static boolean isWebGl() {
        boolean isWebGl = Platform.getInstance().getName().equals("WebGL");
        return isWebGl;
    }

    public static void testFind(ReferenceScene rs, SceneNode movebox) {
        logger.info("testFind");
        SceneNode mb = new SceneNode(Platform.getInstance().findSceneNodeByName(ReferenceScene.MOVEBOXNAME).get(0));
        RuntimeTestUtil.assertNotNull("find movebox", mb);
    }

    public static void testGetParent(ReferenceScene referenceScene, SceneNode movingbox) {
        logger.info("testGetParent");
        SceneNode parent = movingbox.getTransform().getParent().getSceneNode();
        parent = parent.getTransform().getParent().getSceneNode();
        RuntimeTestUtil.assertEquals("parent name", "rechts 0", parent.getName());
        parent = parent.getTransform().getParent().getSceneNode();
        RuntimeTestUtil.assertEquals("parent name", "World", parent.getName());
        Transform tparent = parent.getTransform().getParent();
        RuntimeTestUtil.assertNull("world parent", tparent);
    }

    /**
     * Test, ob die Platform das mit den Childs richtig macht (z.B. jme meshholder)
     * 20.8.24: There is a custom finder in SceneNode and a finder by platform. Test both.
     * @param referenceScene
     */
    public static void testFindNodeByName(ReferenceScene referenceScene) {
        logger.info("testFindNodeByName");
        // Einfach erstmal etwas suchen, was es nicht gibt). Vorher einen Dump.
        String graph = ReferenceScene.getCurrent().dumpSceneGraph();
        logger.debug("\n" + graph);
        RuntimeTestUtil.assertTrue("World", StringUtils.startsWith(graph, "World"));
        for (SceneNode n : referenceScene.towerrechts) {
            n.findNodeByName("xxxccvv");
        }
        for (SceneNode n : referenceScene.tower2) {
            n.findNodeByName("xxxccvv");
        }

        List<NativeSceneNode> nodes = SceneNode.findByName("models/loc.gltf");
        RuntimeTestUtil.assertEquals("number loc", 1, nodes.size());
        // children are two level below
        NativeSceneNode childNode = nodes.get(0).getTransform().getChildren().get(0).getSceneNode();
        RuntimeTestUtil.assertEquals("node name","gltfroot", childNode.getName());
        childNode = childNode.getTransform().getChildren().get(0).getSceneNode();
        RuntimeTestUtil.assertEquals("node name","Locomotive", childNode.getName());
        childNode = childNode.getTransform().getChildren().get(0).getSceneNode();
        //6.8.24 TODO check baseblock without name? RuntimeTestUtil.assertEquals("node name","baseblock", childNode.getName());
        RuntimeTestUtil.assertEquals("number loc children", 8, childNode.getTransform().getChildren().size());

        SceneNode rechts1 = referenceScene.towerrechts.get(0).findNodeByName("rechts 1").get(0);
        RuntimeTestUtil.assertNotNull("", rechts1);
        RuntimeTestUtil.assertEquals("", "rechts 1", rechts1.getName());
        RuntimeTestUtil.assertNotNull("", rechts1.getTransform().getParent());
        // same test via platform finder
        rechts1 = new SceneNode(Platform.getInstance().findNodeByName("rechts 1", referenceScene.towerrechts.get(0).nativescenenode).get(0));
        RuntimeTestUtil.assertNotNull("", rechts1);
        RuntimeTestUtil.assertEquals("", "rechts 1", rechts1.getName());
        RuntimeTestUtil.assertNotNull("", rechts1.getTransform().getParent());


        // wenn das mesh und name in der Original Node enthalten ist, muss es auch in der find Instanz sein.
        RuntimeTestUtil.assertNotNull("mesh", referenceScene.towerrechts.get(1).getMesh());
        RuntimeTestUtil.assertEquals("name", "rechts 1", referenceScene.towerrechts.get(1).getName());
        RuntimeTestUtil.assertEquals("children rechts 1", 1, referenceScene.towerrechts.get(1).getTransform().getChildren().size());
        RuntimeTestUtil.assertNotNull("mesh", rechts1.getMesh());
        RuntimeTestUtil.assertEquals("name", "rechts 1", rechts1.getName());
        RuntimeTestUtil.assertEquals("children", 1, rechts1.getTransform().getChildren().size());

    }

    /**
     * GWT JsonParser doesn't like line breaks. TODO add test?
     */
    public static void testJson() {
        logger.info("testJson");
        String jsonString = "{" +
                JsonHelper.buildProperty("a", "b") + "," +
                JsonHelper.buildProperty("c", "\"d") +
                "}";

        logger.debug("parsing " + jsonString);
        NativeJsonValue parsed = Platform.getInstance().parseJson(jsonString);
        NativeJsonObject o = parsed.isObject();
        RuntimeTestUtil.assertNotNull("json.isObject", o);
        logger.debug("parsed a:" + parsed.isObject().get("a").isString().stringValue());
        logger.debug("parsed c:" + parsed.isObject().get("c").isString().stringValue());
        RuntimeTestUtil.assertEquals("property a", "b", parsed.isObject().get("a").isString().stringValue());
        RuntimeTestUtil.assertEquals("property c", "\"d", parsed.isObject().get("c").isString().stringValue());
    }

    public static void testLayer(ReferenceScene rs) {
        logger.info("testLayer");
        // inventory child 0 is the text "1884"
        Transform area1884transform = rs.inventory.getTransform().getChild(0);

        RuntimeTestUtil.assertEquals("hiddencube.layer", rs.HIDDENCUBELAYER, rs.hiddencube.getTransform().getLayer());
        RuntimeTestUtil.assertEquals("hiddencube.child.layer", rs.HIDDENCUBELAYER, rs.hiddencube.getTransform().getChild(0).getLayer());
        // Tests have probably been failing always in unity
        if (!isUnity()) {
            RuntimeTestUtil.assertEquals("deferredcamera.layer", rs.HIDDENCUBELAYER, rs.deferredcamera.getLayer());
            RuntimeTestUtil.assertEquals("deferredcamera.carrier.layer", rs.HIDDENCUBELAYER, rs.deferredcamera.getCarrier().getTransform().getLayer());

            Camera fovCamera = FovElement.getDeferredCamera(null);
            RuntimeTestUtil.assertEquals("fovCamera.layer", 1, fovCamera.getLayer());
            RuntimeTestUtil.assertEquals("fovCamera.carrier.layer", 1, fovCamera.getCarrier().getTransform().getLayer());
            RuntimeTestUtil.assertEquals("inventory.layer", rs.HIDDENCUBELAYER, rs.inventory.getTransform().getLayer());
            RuntimeTestUtil.assertEquals("inventory.child.layer", rs.HIDDENCUBELAYER, area1884transform.getLayer());

            RuntimeTestUtil.assertEquals("inventory.area1884.name", "area1884", area1884transform.getSceneNode().getName());
            // unity needs higher tolerance
            RuntimeTestUtil.assertEquals("inventory.z", -4.1, rs.inventory.getTransform().getPosition().getZ());
            RuntimeTestUtil.assertEquals("inventory.child.z", 0.001, area1884transform.getPosition().getZ());
        }
        RuntimeTestUtil.assertEquals("controlMenu.layer", FovElement.LAYER, rs.controlMenu.getTransform().getLayer());

        // difficult to calculate world expected reference value
        // TestUtil.assertEquals("inventory.child.world.z", rs.INITIAL_CAMERA_POSITION.getZ() - 4.0 + 0.01, rs.inventory.getTransform().getChild(0).getWorldModelMatrix().extractPosition().getZ());
        SceneNode area1884 = new SceneNode(SceneNode.findByName("area1884").get(0));

    }

    public static void testFirstPersonTransform(ReferenceScene rs) {
        logger.info("FirstPerson");
        SceneNode sn = new SceneNode();
        Transform transform = sn.getTransform();
        Degree firstAngle = new Degree(30);
        transform.rotateY(firstAngle);
        Vector3 forward = transform.getLocalModelMatrix().getForward();
        RuntimeTestUtil.assertVector3(new Vector3(sin(firstAngle), 0, cos(firstAngle)), forward);

        // move really forward (not like a camera)
        Transform.moveForward(transform, 2.0);

        // forward vector shouldn't change
        RuntimeTestUtil.assertVector3(new Vector3(sin(firstAngle), 0, cos(firstAngle)), transform.getLocalModelMatrix().getForward());
        RuntimeTestUtil.assertVector3(new Vector3(2 * sin(firstAngle), 0, 2 * cos(firstAngle)), transform.getPosition());
    }

    /**
     * Used when menu opens/closes, no regular test.
     */
    public static void testCycledMenu(SceneNode[] expectedChildren) {
        logger.info("test FOV camera");
        Camera cameraOfSecondMenu = FovElement.getDeferredCamera(null);
        Transform carrierTransform = cameraOfSecondMenu.getCarrier().getTransform();
        for (Transform child : carrierTransform.getChildren()) {
            logger.debug("fov camera child: " + child.getSceneNode().getName());
        }
        RuntimeTestUtil.assertEquals("FOV camera.children.count", expectedChildren.length, cameraOfSecondMenu.getCarrier().getTransform().getChildCount());
        // 'Hud" and "controlMenu" are always attached to the FOV camera.
        RuntimeTestUtil.assertEquals("FOV camera.hud", "Hud", carrierTransform.getChild(0).getSceneNode().getName());
        RuntimeTestUtil.assertEquals("FOV camera.controlmenu", "ControlIcon", carrierTransform.getChild(1).getSceneNode().getName());
    }
}

