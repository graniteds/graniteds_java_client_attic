package org.granite.client.tide.server;

import org.granite.client.messaging.messages.responses.FaultMessage.Code;


public class Fault {

    private Code faultCode;
    private String faultDescription;
    private String faultDetails;
    
    private Object content;
    
    private Object cause;
    
    public Fault(Code faultCode, String faultDescription, String faultDetails) {
        this.faultCode = faultCode;
        this.faultDescription = faultDescription;
        this.faultDetails = faultDetails;
    }
    
    public Code getCode() {
        return faultCode;
    }
    
    public String getFaultDescription() {
        return faultDescription;
    }
    
    public String getFaultDetails() {
        return faultDetails;
    }

    
    public Object getContent() {
        return content;
    }
    
    public void setContent(Object context) {
        this.content = context;
    }
    
    public Object getCause() {
        return cause;
    }
    
    public void setCause(Object cause) {
        this.cause = cause;
    }
}
