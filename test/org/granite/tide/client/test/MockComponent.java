package org.granite.tide.client.test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.granite.client.rpc.AsyncToken;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.impl.ComponentResponderImpl;
import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;


public class MockComponent extends ComponentImpl {
    
    private static final Logger log = Logger.getLogger(MockComponent.class);
    
    private Executor executor = null;
    private ResponseBuilder responseBuilder = null;
    
    public MockComponent(ServerSession serverSession) {
    	super(serverSession);
        executor = Executors.newSingleThreadExecutor();
    }
    
    public void setResponseBuilder(ResponseBuilder responseBuilder) {
        this.responseBuilder = responseBuilder;
    }
    
    public AsyncToken invoke(final ComponentResponderImpl componentResponder) {
        final AsyncToken token = new MockAsyncToken(null);
        token.addResponder(componentResponder);
        
        // TODO: should probably wait for client operations to finish so caller has the opportunity to add responders
        
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    token.callResponders(responseBuilder.buildResponseEvent(token, componentResponder.getComponent(), componentResponder.getOperation(), componentResponder.getArgs()));
                }
                catch (InterruptedException e) {
                    log.error(e, e.getMessage());
                }                
            }            
        });
        
        // remoteSession.checkWaitForLogout();            
        
        return token;
    }

}
