package de.yard.threed.engine.platform.common;

import de.yard.threed.core.platform.AsyncJobDelegate;

/**
 * In Anlehnung an SwingUtilities.invokeLater() mit derselben Intention. Läuft im selben Thread.
 * Der invoked Aufruf selber ist nicht async.
 * <p>
 * 20.5.20: Modernerer Ersatz fuer {@link AsyncJob}?
 */
public abstract class AsyncInvoked<T> {
    AsyncJobDelegate<T> asyncJobDelegate;

    public AsyncInvoked(AsyncJobDelegate<T> asyncJobDelegate) {
        this.asyncJobDelegate = asyncJobDelegate;
    }

    public abstract T execute(/*20.5.20 brauchts nicht P param*/);

    public void run() {
        asyncJobDelegate.completed(execute());
    }


}
