package de.yard.threed.platform.webgl;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.user.client.Timer;
import de.yard.threed.core.platform.Log;
//import de.yard.threed.core.platform.NativeAsyncRunner;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.engine.platform.common.InitExecutor;
import de.yard.threed.engine.platform.common.NativeInitChain;

/**
 *
 */
public class WebGlInitChain extends NativeInitChain implements AnimationScheduler.AnimationCallback {

    Log logger = Platform.getInstance().getLog(WebGlInitChain.class);
    int cnt = 0;

    WebGlInitChain(InitExecutor preloadCallback/*, NativeAsyncRunner asyncRunner*/) {
        this.executor = preloadCallback;
        //this.asyncRunner = asyncRunner;
    }

    @Override
    public void execute(double xxv) {

        logger.debug("WebGlInitChain execute");

        //logger.info("still preloading ");// + bundleLoader.currentlyloading.size());
        //GwtUtil.showStatus("still preloading ");// + bundleLoader.currentlyloading.size() + " " + cnt);
        cnt++;

        // processFutures() and processInvokeLaters() is done in execute().
        super.execute();
    }

    @Override
    public void invokeLater(NativeInitChain runnable, int delay){
        // Prefer setTimeout over AnimationScheduler.requestAnimationFrame();
        // See https://stackoverflow.com/questions/38709923/why-is-requestanimationframe-better-than-setinterval-or-settimeout
        // 1.3.24: Switch from AnimationScheduler to timer. During init chain nothing is rendered, so it appears more
        // efficient to interrupt some time and give concurrent threads more CPU. However, its not proved.

        //The recommended way, but no for VR. 7.3.23: Hmm: what does that mean? What is special about VR here.
        //AnimationScheduler animationScheduler = AnimationScheduler.get();
        //animationScheduler.requestAnimationFrame((WebGlInitChain)runnable);

        Timer t = new Timer() {
            public void run() {
                runnable.execute();
            }
        };

        t.schedule(100);
    }
}
