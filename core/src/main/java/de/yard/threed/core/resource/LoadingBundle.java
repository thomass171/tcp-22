package de.yard.threed.core.resource;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * A bundle currently being loaded.
 */
public class LoadingBundle {
    Log logger = Platform.getInstance().getLog(LoadingBundle.class);
    // set after directory load
    public Bundle bundle;
    //public int failure = 0;
    private List<String> failurelist = new ArrayList<String>();
    public List<BundleLoadDelegate> callbacks = new ArrayList<BundleLoadDelegate>();
    public String bundlename;
    //timestamp when requesting all bundle content isType done. Might still be running in background.
    public long doneloading = 0;
    public boolean delayed;
    public long started = Platform.getInstance().currentTimeMillis();

    public LoadingBundle(String bundlename, BundleLoadDelegate loadlistener, boolean delayed) {
        this.bundlename = bundlename;
        callbacks.add(loadlistener);
        this.delayed = delayed;
    }

    public void addFailure(String filename) {
        failurelist.add(filename);
    }

    /**
     * Is this really reliable? Until now there is no evidence it isn't.
     */
    public boolean isReady() {

        if (bundle == null) {
            logger.debug("isReady false");
            return false;
        }
        if (bundle.getExpectedSize() == bundle.getSize() + bundle.getFailuredSize()) {
            logger.debug("isReady true");
            return true;
        }
        // Give it a maximum of 400 seconds. But why? The app very likely will fail later. Needs a retry.
        // And which user will wait that long time? Needs some improvement.
        int timeoutms = 400 * 1000;
        if (doneloading > 0 && Platform.getInstance().currentTimeMillis() - doneloading > timeoutms) {
            String msg = "Loading bundle " + bundlename + " is overdue. Expected " + bundle.getExpectedSize() + " items, got only " + bundle.getSize() + ". Aborting.";
            logger.error(msg);
            Platform.getInstance().fatal(msg);
            // releases an inconsistent bundle.
            for (String s : bundle.directory) {
                if (!bundle.contains(s)) {
                    logger.error("Missing:" + s);
                }
            }

            return true;
        }
        //logger.debug("isReady false: "+ bundle.getExpectedSize() +","+ bundle.getSize() + "," + bundle.getFailuredSize());
        return false;
    }

    /**
     * Bei nur einem Failure schon wird true geliefert. Dann wird das LoadingBundle entfernt, auch wenn noch Requests laufen.
     *
     * @return
     */
    public boolean failed() {
        return failurelist.size() > 0;
    }

    public List<String> getFailurelist(){
        return failurelist;
    }

    public void addDelegate(BundleLoadDelegate bundleLoadDelegate) {
        this.callbacks.add(bundleLoadDelegate);
    }
}

