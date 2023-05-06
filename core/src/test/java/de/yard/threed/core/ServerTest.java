package de.yard.threed.core;


import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.testutil.CoreTestFactory;
import de.yard.threed.core.testutil.PlatformFactoryTestingCore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * <p>
 */
public class ServerTest {
    static Platform platform = CoreTestFactory.initPlatformForTest(new PlatformFactoryTestingCore(),null);

    @Test
    public void test1() {

        Server server = new Server("ubuntu-server.udehlavj1efjeuqv.myfritz.net:443:/sceneserver/5890");

        Assertions.assertEquals( "ubuntu-server.udehlavj1efjeuqv.myfritz.net", server.getHost());
        Assertions.assertEquals( 443, server.getPort());
        Assertions.assertEquals( "/sceneserver/5890", server.getPath());
    }
}
