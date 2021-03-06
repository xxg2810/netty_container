/**
 * 
 */
package org.jocean.idiom;

import java.util.ConcurrentModificationException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author isdom
 *
 */
public class ConcurrentInvokeGuard {
    
    public static void enableGuard(final boolean isEnabled) {
        _GUARD_ENABLED = isEnabled;
    }
    
    public void enter(final String errorMsg) {
        if ( _GUARD_ENABLED ) {
            final long tid = this._currentThreadId.get();
            if ( Thread.currentThread().getId() == tid
               ||  -1 == tid ) {
                this._trigger.increment();
                if ( Thread.currentThread().getId() 
                    != this._currentThreadId.get() ) {
                    throw new ConcurrentModificationException(errorMsg);
                }
            }
            else {
                throw new ConcurrentModificationException(errorMsg);
            }
        }
    }
    
    public void leave(final String errorMsg) {
        if ( _GUARD_ENABLED ) {
            final long tid = this._currentThreadId.get();
            if ( Thread.currentThread().getId() == tid ) {
                this._trigger.decrement();
            }
            else {
                throw new IllegalStateException(errorMsg);
            }
        }
    }
    
    private final CountedTrigger _trigger = new CountedTrigger(new CountedTrigger.Reactor() {

        
        public void onIncrementFromZero() {
            if ( !_currentThreadId.compareAndSet(-1, 
                    Thread.currentThread().getId()) ) {
                throw new ConcurrentModificationException();
            }
        }

        
        public void onDecrementToZero() {
            if ( !_currentThreadId.compareAndSet(
                    Thread.currentThread().getId(), -1) ) {
                throw new IllegalStateException();
            }
        }});
    private final AtomicLong _currentThreadId = new AtomicLong(-1);
    private static volatile boolean _GUARD_ENABLED = false;
}
