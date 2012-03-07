package org.granite.tide.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.granite.logging.Logger;
import org.granite.rpc.AsyncResponder;
import org.granite.rpc.AsyncToken;
import org.granite.rpc.events.FaultEvent;
import org.granite.rpc.events.ResultEvent;


public class FutureResult<T> implements Future<T> {
    
    private static final Logger log = Logger.getLogger(FutureResult.class);
    
    private Semaphore sem = new Semaphore(1);
    
    private boolean done = false;
    private T result;
    
    public FutureResult(AsyncToken token) {
        token.addResponder(new AsyncResponder() {
            @SuppressWarnings("unchecked")
            @Override
            public void result(ResultEvent event) {
                done = true;
                result = (T)event.getResult();
                sem.release();
            }

            @Override
            public void fault(FaultEvent event) {
                done = true;
                sem.release();
            }            
        });
        try {
            sem.acquire();
        }
        catch (InterruptedException e) {
            log.error(e, "Could not acquire initial lock");
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        sem.acquire();
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean acquired = sem.tryAcquire(timeout, unit);
        if (!acquired)
            throw new TimeoutException("Could not get result");
        return result;
    }

}
