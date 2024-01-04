package de.yard.threed.engine.platform.common;

import de.yard.threed.core.platform.Log;
//import de.yard.threed.core.platform.NativeAsyncRunner;
//import de.yard.threed.core.platform.NativeRunnable;
import de.yard.threed.core.platform.Platform;

/**
 *
 */
public class DefaultInitChain extends NativeInitChain {

    Log logger = Platform.getInstance().getLog(DefaultInitChain.class);

    DefaultInitChain(InitExecutor preloadCallback) {
        this.executor = preloadCallback;
    }

    @Override
    public void execute() {

        logger.debug("DefaultInitChain execute");
        // processFutures() and processInvokeLaters() is done in execute().
        super.execute();
    }

    @Override
    public void invokeLater(NativeInitChain nativeInitChain, int delay) {
        AbstractSceneRunner.getInstance().sleepMs(10);
        nativeInitChain.execute();
    }
}
