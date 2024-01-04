package de.yard.threed.core.platform;

/**
 * Exposes those parts of a scene runner that are interesting from outside.
 *
 * 5.7.21: Again NativeScene instead of Scene
 * 7.7.21: Really needed?
 * 26.3.23: Reactivaed to have Scenerunner available in all platforms (like SimpleHeadlessPlatform)
 * Might help to hide AbstractSceneRunner.getInstance() to apps.
 *
 * Created by thomass on 29.04.15.
 */
public interface NativeSceneRunner {
   // needed/useful? public void runScene(NativeScene scene);

   /**
    * Executes delegate when future is complete.
    */
   <T,D> void addFuture  (NativeFuture<T> future, AsyncJobDelegate<D> asyncJobDelegate);
  
}
