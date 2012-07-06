package org.granite.client.tide.server;

import org.granite.client.rpc.AsyncResponder;
import org.granite.client.rpc.events.FaultEvent;
import org.granite.client.rpc.events.ResultEvent;
import org.granite.client.tide.Context;


/**
 * @author William DRAI
 */
public interface ComponentResponder extends AsyncResponder {
    
    public String getOperation();
    
    public Object[] getArgs();
    public void setArgs(Object[] args);
    
    public Context getSourceContext();
    
    public Component getComponent();
    
    
    public static interface Handler {
        
        public void result(Context context, ResultEvent event, Object info, String componentName, String operation, TideResponder<?> tideResponder, ComponentResponder componentResponder);
        
        public void fault(Context context, FaultEvent event, Object info, String componentName, String operation, TideResponder<?> tideResponder, ComponentResponder componentResponder);
    }
}
