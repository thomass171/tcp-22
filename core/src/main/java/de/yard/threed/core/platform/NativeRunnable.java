package de.yard.threed.core.platform;

/**
 * A delegate/function/operation to be executed later in the 'same thread'. So, explicitly no concurrent execution
 * to the main thread (the thread that renders).
 * Like SwingUtilities.invokeLater or AnimationScheduler.requestAnimationFrame();
 *
 * Executed by platform natively, not by AsyncHelper.
 * Currently only used for initing initing.
 * Just a placeholder for comment. Its hard to implement asnyc callbacks like it is in javascript inside platforms like JME and Unity.
 */
/*public interface NativeRunnable {
}*/
