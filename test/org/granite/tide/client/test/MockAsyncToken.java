package org.granite.tide.client.test;

import org.granite.rpc.AsyncToken;
import org.granite.tide.impl.ComponentResponderImpl;


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
