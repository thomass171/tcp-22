package de.yard.threed.platform.jme;

import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.math.ColorRGBA;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import de.yard.threed.core.Vector3;
import de.yard.threed.core.platform.NativeLight;

import de.yard.threed.core.Color;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;


/**
 * Wie ein Mesh Komponente einer SceneNode, obwohl es in JME eigentlich eigenstaendig ist.
 * <p>
 * Created by thomass on 25.04.15.
 */
public class JmeLight implements NativeLight {
    Light light;

    // registry for all lights
    static List<JmeLight> lights = new ArrayList<>();

    private JmeLight(Light light) {
        this.light = light;
    }

    public static JmeLight buildPointLight(Color col, double range) {
        PointLight dl = new PointLight();
        dl.setColor(new ColorRGBA(col.getR(), col.getG(), col.getB(), col.getAlpha()));
        //Radius mal nur so zum testen
        //bringt scheinbar aber nichts
        dl.setRadius((float) range);
        return new JmeLight(dl);
    }

    public static JmeLight buildDirectionalLight(Color col, Vector3 direction) {
        DirectionalLight dl = new DirectionalLight();
        dl.setColor(new ColorRGBA(col.getR(), col.getG(), col.getB(), col.getAlpha()));
        // 22.3.17 Laut Doku ist die direction auch bei JME die Herkunft.(??)
        // Da hab ich aber Zweifel, denn die default direction ist (0f, -1f, 0f), also von unten nach oben(??).
        // Er scheint später dann ein negate() drauf zu machen. Crazy.
        // normalize() is later done internally.
        // 20.2.25 negating seems correct, JME seems to have a different understanding.
        dl.setDirection(new JmeVector3((float) -direction.getX(), (float) -direction.getY(), (float) -direction.getZ()).vector3);
        final int SHADOWMAP_SIZE = 1024;
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(((PlatformJme) Platform.getInstance()).jmeResourceManager.am, SHADOWMAP_SIZE, 3);
        dlsr.setLight(dl);
        JmeSceneRunner.getInstance().jmecamera.getViewPort().addProcessor(dlsr);

        JmeLight jmeLight = new JmeLight(dl);
        lights.add(jmeLight);
        return jmeLight;
    }

    public static JmeLight buildAmbientLight(Color col) {
        AmbientLight dl = new AmbientLight();
        dl.setColor(new ColorRGBA(col.getR(), col.getG(), col.getB(), col.getAlpha()));
        // The Default ambient light is quite dark. 20.2.25 Whatever that means.
        // Making it brighter is no solution as it makes it to bright (well, mult() appears not appropriate).
        // https://wiki.jmonkeyengine.org/jme3/advanced/light_and_shadow.html
        //dl.setColor(dl.getColor().mult(3));
        JmeLight jmeLight = new JmeLight(dl);
        lights.add(jmeLight);
        return jmeLight;
    }

    public void setPosition(Vector3 pos) {
        //gibt es nur bei Pointlight
        if (light instanceof PointLight) {
            ((PointLight) light).setPosition((JmeVector3.toJme(pos)));
        }
    }

    public Vector3 getPosition() {
        //gibt es nur bei Pointlight
        if (light instanceof PointLight) {
            return JmeVector3.fromJme(((PointLight) light).getPosition());
        }
        return null;
    }


    @Override
    public Color getAmbientColor() {
        if (light instanceof AmbientLight){
             return new Color((light).getColor().asIntARGB());
        }
        return null;
    }

    @Override
    public Color getDirectionalColor() {
        if (light instanceof DirectionalLight){
            return new Color((light).getColor().asIntARGB());
        }
        return null;
    }

    @Override
    public Vector3 getDirectionalDirection() {
        if (light instanceof DirectionalLight){
            return JmeVector3.fromJme(((DirectionalLight)light).getDirection()).negate();
        }
        return null;
    }
}
