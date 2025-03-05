package de.yard.threed.engine;

import de.yard.threed.core.Matrix3;
import de.yard.threed.core.platform.NativeTexture;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.ShaderProgram;
import de.yard.threed.engine.testutil.AdvancedHeadlessPlatformFactory;
import de.yard.threed.engine.testutil.EngineTestFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
@Slf4j
public class ShaderTest {

    Platform platform = EngineTestFactory.initPlatformForTest(new String[]{"engine"}, new AdvancedHeadlessPlatformFactory());

    @Test
    public void test() throws Exception {

        Texture texture = Texture.buildBundleTexture("engine", "Iconset-LightBlue.png");

        HashMap<String, NativeTexture> map = new HashMap<String, NativeTexture>();
        map.put("texture", texture.texture);

        ShaderProgram program = ShaderPool.buildUniversalEffect();
        Matrix3 textureMatrix=new Matrix3();
        program.program.compile();

        Material mat = Material.buildCustomShaderMaterial(program, true);



    }
}
