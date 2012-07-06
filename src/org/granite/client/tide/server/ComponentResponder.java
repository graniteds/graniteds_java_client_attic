package org.granite.client.tide.server;

import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.tide.Context;


/**
 * @author William DRAI
 */
public interface ComponentResponder extends ResponseListener {
    
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
