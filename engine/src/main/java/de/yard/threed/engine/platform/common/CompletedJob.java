package de.yard.threed.engine.platform.common;

import de.yard.threed.core.platform.NativeTexture;
import java.util.HashMap;

/**
 * Created by thomass on 26.04.16.
 */
public class CompletedJob {
    public AsyncJob job;
   // boolean success;
    //NativeTexture texture;
    // muss synced sein
   public String/*java.lang.Exception*/ e;
    private HashMap<AsyncJob, NativeTexture> successfulascyncallbacklist = new HashMap<AsyncJob, NativeTexture>();
    private HashMap<AsyncJob, NativeTexture> failedascyncallbacklist = new HashMap<AsyncJob, NativeTexture>();

    public CompletedJob(AsyncJob job/*, NativeTexture texture*/) {
        this.job = job;
       // this.success = success;
        //this.texture = texture;
    }

    public CompletedJob(AsyncJob job, String/*java.lang.Exception*/ e) {
        this.job = job;
        this.e = e;
        //this.texture = texture;
    }
}
