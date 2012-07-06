package org.granite.tide.client.test;

import org.granite.client.rpc.AsyncToken;
import org.granite.client.tide.impl.ComponentResponderImpl;


public class MockAsyncToken extends AsyncToken {
    
    private ComponentResponderImpl componentResponder;
    
    public MockAsyncToken(ComponentResponderImpl componentResponder) {
        super(null);
        
        this.componentResponder = componentResponder;
    }
    
    public ComponentResponderImpl getComponentResponder() {
        return componentResponder;
    }

}
