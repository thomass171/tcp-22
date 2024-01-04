package de.yard.threed.engine.platform.common;


public abstract class InitExecutor {
    public InitExecutor execute() {
        // futures for http get. Needed in browser and non browser.
        AbstractSceneRunner.getInstance().processFutures();
        // invokelaters for BundleLoadDelegates. Needed in browser and non browser.
        AbstractSceneRunner.getInstance().processInvokeLaters();
        return run();
    }

    public abstract InitExecutor run();
}
