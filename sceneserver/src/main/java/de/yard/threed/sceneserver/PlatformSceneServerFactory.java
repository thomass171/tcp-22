package de.yard.threed.sceneserver;

import de.yard.threed.core.platform.PlatformFactory;
import de.yard.threed.core.platform.PlatformInternals;
import de.yard.threed.platform.opengl.PlatformHomeBrew;

import java.util.HashMap;

/**
 * HomeBrew mit Spezialrenderer? Darum ist es kein SimpleHeadless.
 *
 * 10.9.20: Sowas gibt es jetzt auch in desktop.
 */
public class PlatformSceneServerFactory implements PlatformFactory {
    @Override
    public PlatformInternals createPlatform(HashMap<String, String> properties) {
       return PlatformHomeBrew.init(properties);

    }
}
