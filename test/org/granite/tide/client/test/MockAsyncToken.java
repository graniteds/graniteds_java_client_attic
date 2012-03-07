package org.granite.tide.client.test;

import org.granite.rpc.AsyncToken;
import org.granite.tide.rpc.ComponentResponder;


public class MockAsyncToken extends AsyncToken {
    
    private ComponentResponder componentResponder;
    
    public MockAsyncToken(ComponentResponder componentResponder) {
        super(null);
        
        this.componentResponder = componentResponder;
    }
    
    public ComponentResponder getComponentResponder() {
        return componentResponder;
    }

}
