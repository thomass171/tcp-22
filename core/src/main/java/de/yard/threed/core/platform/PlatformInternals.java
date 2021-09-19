package de.yard.threed.core.platform;

import de.yard.threed.core.GeneralHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Those elements of a platform that are for internal use (by SceneRunner) but not for the application.
 * 14.9.21:But this doesn't really help hiding it? Why not put it into either Platform or SceneRunner?
 * 02.08.21
 */
public class PlatformInternals {
    // for actions to be done before rendering
    public List<GeneralHandler> beforeFrameHandler = new ArrayList<GeneralHandler>();
}
