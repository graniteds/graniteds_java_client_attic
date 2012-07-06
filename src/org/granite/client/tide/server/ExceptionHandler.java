package org.granite.client.tide.server;

import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.tide.Context;


public interface ExceptionHandler {
    
    /**
     *  Should return true if this handler is able to manage the specified ErrorMessage
     *
     *  @param emsg an error message 
     *  @return true if ErrorMessage accepted
     */
    public boolean accepts(FaultMessage emsg);
   
    /**
     *  Handle the error
     * 
     *  @param context the context in which the error occured
     *  @param emsg the error message
     *  @param event the full fault event
     */
    public void handle(Context context, FaultMessage emsg, TideFaultEvent faultEvent);

}
