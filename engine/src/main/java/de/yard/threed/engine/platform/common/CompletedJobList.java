package de.yard.threed.engine.platform.common;

import java.util.ArrayList;
import java.util.List;

/**
 * 19.5.20: Ist das speziell fuer Animationen?
 * Ich setz das mal auf deprecated, weil AsnyHelper sowas aehnliches bietet. War das hier fuer Animations?
 *
 * Created by thomass on 27.04.16.
 */
@Deprecated
public class CompletedJobList {
    // muss synced sein
    private List<CompletedJob> ascyncallbacklist = new ArrayList<CompletedJob>();
    private Object lockobject = new Object();

    public void add(CompletedJob completedJob) {
        synchronized (lockobject){
            ascyncallbacklist.add(completedJob);
        }
    }

    /**
     * Liefert threadsafe die Liste der beendeten Jobs.
     * @return
     */
    public  List<CompletedJob> getCompletedJobs(){
        synchronized (lockobject){
            List<CompletedJob> oldlist = ascyncallbacklist;
            ascyncallbacklist = new ArrayList<CompletedJob>();
            return oldlist;
        }
    }

}
