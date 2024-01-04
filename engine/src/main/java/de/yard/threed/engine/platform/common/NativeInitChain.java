package de.yard.threed.engine.platform.common;

/**
 * After preload ConnectLoopCallback and then "renderer.setAnimationLoop()" is triggered.
 */
public abstract class NativeInitChain {

    public InitExecutor executor;
    int cnt = 0;

    public void execute() {
        // processFutures() and processInvokeLaters() is done in execute().
        executor = executor.execute();
        if (executor != null) {
            invokeLater(this, 0);
        } else {
            AbstractSceneRunner.getInstance().startRenderLoop();
        }
    }

    public abstract void invokeLater(NativeInitChain nativeInitChain, int delay);
}
