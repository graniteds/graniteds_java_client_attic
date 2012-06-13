package org.granite.tide.server;

import org.granite.tide.Context;

import flex.messaging.messages.ErrorMessage;


public interface ExceptionHandler {
    
    /**
     *  Should return true if this handler is able to manage the specified ErrorMessage
     *
     *  @param emsg an error message 
     *  @return true if ErrorMessage accepted
     */
    public boolean accepts(ErrorMessage emsg);
   
    /**
     *  Handle the error
     * 
     *  @param context the context in which the error occured
     *  @param emsg the error message
     *  @param event the full fault event
     */
    public void handle(Context context, ErrorMessage emsg, TideFaultEvent faultEvent);

}
