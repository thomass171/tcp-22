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
        // risk of stackoverflow when bundle takes too long. sleep increased from 10 to 30.
        AbstractSceneRunner.getInstance().sleepMs(30);
        nativeInitChain.execute();
    }
}
