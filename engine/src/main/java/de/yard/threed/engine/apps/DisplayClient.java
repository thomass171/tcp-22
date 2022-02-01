package de.yard.threed.engine.apps;

import de.yard.threed.core.Point;
import de.yard.threed.core.Util;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.*;
import de.yard.threed.engine.avatar.Avatar;
import de.yard.threed.engine.ecs.*;
import de.yard.threed.engine.geometry.ShapeGeometry;
import de.yard.threed.engine.ObserverSystem;

import de.yard.threed.engine.platform.EngineHelper;
import de.yard.threed.core.platform.Log;
import de.yard.threed.core.Color;
import de.yard.threed.core.Dimension;
import de.yard.threed.engine.platform.common.Settings;


/**
 * Ein generische Scene, die dienen kann als
 *
 * - DisplayClient. Dann kennt diese Klasse keine erforderlichen Systems, sondern bekommt alle Infos vom Server.
 * - Superklasse fuer lokale Scenes. Dann kennt die ableitende Klasse die erforderlichen Systems.
 *
 * Hier gibt es nur:
 * - ein Avatar
 * - ein KeyToSystem System
 * - ein menu?
 * - evtl. nearView
 *
 * Als POC erstmal mit Railing und Sokoban.
 *
 * Workflow:
 * - Nach dem Start ein Menu mit connect Options (alternativ per cmd option)
 * - User wählt ein
 * - es wird verbunden
 * - Scene wird aufgebaut.
 *
 * 15.2.21: Nachfolger der mal angedachten GenericScene.
 * 24.1.22: Still a valid concept?
 * <p>
 * <p>
 * Created by thomass on 11.11.20.
 */
public class DisplayClient extends Scene /*, BackendAdapter/*??*/ {
    public Log logger = Platform.getInstance().getLog(DisplayClient.class);
    Avatar avatar = null;
    //EcsEntity avatarE = null;
    SceneNode ground;


    @Override
    public String[] getPreInitBundle() {
        return new String[]{"engine", "data", "railing"};
    }

    @Override
    public void initSettings(Settings settings) {
        settings.vrready = true;
        settings.aasamples = 4;
    }

    /*@Override
    public void vrDisplayPresentChange(boolean isPresenting) {
        logger.debug("vrDisplayPresentChange:isPresenting=" + isPresenting);
        if (isPresenting) {
            avatar.lowerVR();
        } else {
            avatar.raiseVR();
        }
    }*/

    @Override
    public void init(boolean forServer) {
        logger.debug("init GenericScene");
        processArguments();




        addLight();

        /*24.1.22 avatar = Avatar.buildDefault(getDefaultCamera());
        avatar.enableBody();
        addToWorld(avatar.getSceneNode());

        TeleportComponent avatartc = TeleportComponent.getTeleportComponent(avatar.avatarE);


        ObserverComponent oc = new ObserverComponent(new ProxyTransform(getDefaultCamera().getCarrier().getTransform().transform, null/*13.11.20 slave* /));
        oc.setRotationSpeed(40);
        avatar.avatarE.addComponent(oc);*/
        ObserverSystem viewingsystem = new ObserverSystem();
        SystemManager.addSystem(viewingsystem, 0);



        //23.10.19: Jetzt die Plane mal wirklich unter die Gleise
        Material goundmat = Material.buildLambertMaterial(Color.GREEN);
        double planewidth = 160;
        double planeheight = 640;
        SceneNode ground = new SceneNode(new Mesh(ShapeGeometry.buildPlane(planewidth, planeheight, 1, 1), goundmat, true, true));
        ground.getTransform().setPosition(new Vector3(planewidth / 2, 0, -planeheight / 2));
        ground.setName("Ground");
        addToWorld(ground);

        InputToRequestSystem inputToRequestSystem = new InputToRequestSystem();
        inputToRequestSystem.addKeyMapping( KeyCode.C, UserSystem.USER_REQUEST_LOGIN);
        SystemManager.addSystem(inputToRequestSystem);

        SystemManager.addSystem(new ClientSystem());

        //1.4.21 Player.init(avatar);

    }

    protected void processArguments() {
        if (EngineHelper.isEnabled("argv.enableNearView")) {

        }
    }

    @Override
    public Dimension getPreferredDimension() {
        return new Dimension(800, 600);
    }

    protected void addLight() {
        DirectionalLight light = new DirectionalLight(Color.WHITE, new Vector3(0, 30000000, 20000000));
        addLightToWorld(light);
        light = new DirectionalLight(Color.WHITE, new Vector3(0, -30000000, -20000000));
        addLightToWorld(light);
    }

    @Override
    public void update() {
        double tpf = getDeltaTime();

        Util.nomore();
        //11.5.21avatar.update();
        Point mouselocation = Input.getMouseClick();



    }
}

