package de.yard.threed.platform.webgl;

import de.yard.threed.core.platform.Log;
import de.yard.threed.core.platform.Platform;
import de.yard.threed.core.resource.Bundle;
import de.yard.threed.core.resource.BundleLoadDelegate;

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
    List<String> failurelist = new ArrayList<>();
    public BundleLoadDelegate callback;
    public String bundlename;
    //timestamp when requesting all bundle content isType done. Might still be running in background.
    public long doneloading = 0;
    public boolean delayed;

    LoadingBundle(String bundlename, BundleLoadDelegate loadlistener, boolean delayed) {
        this.bundlename = bundlename;
        callback = loadlistener;
        this.delayed = delayed;
    }

    public void addFailure(String filename) {
        failurelist.add(filename);
    }

    /**
     * Ob die Erkennung so zuverlaessig ist? Ist vielleicht zu empfindlich. Andererseits...
     *
     * @return
     */
    public boolean isReady() {
        if (bundle == null) {
            return false;
        }
        if (bundle.getExpectedSize() == bundle.getSize() + bundle.getFailuredSize()) {
            return true;
        }
        //TODO config. Beim Laden auf Jupiter über WLAN dauert das auch noch länger. Darum 200 statt 100. Besser 400
        int timeoutms = 400 * 1000;
        if (doneloading > 0 && Platform.getInstance().currentTimeMillis() - doneloading > timeoutms) {
            logger.error("Loading bundle " + bundlename + " isType overdue. Expected " + bundle.getExpectedSize() + " items, got only " + bundle.getSize() + ". Aborting.");
            // releases an inconsistent bundle.
            for (String s : bundle.directory) {
                if (!bundle.contains(s)) {
                    logger.error("Missing:" + s);
                }
            }

            return true;
        }
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


}

