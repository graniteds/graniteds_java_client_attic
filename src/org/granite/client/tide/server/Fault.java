package org.granite.client.tide.server;

import org.granite.client.messaging.messages.responses.FaultMessage.Code;


public class Fault {

    private Code faultCode;
    private String faultString;
    private String faultDetail;
    
    private Object content;
    
    private Object rootCause;
    
    public Fault(Code faultCode, String faultString, String faultDetail) {
        this.faultCode = faultCode;
        this.faultString = faultString;
        this.faultDetail = faultDetail;
    }
    
    public Code getFaultCode() {
        return faultCode;
    }
    
    public String getFaultString() {
        return faultString;
    }
    
    public String getFaultDetail() {
        return faultDetail;
    }

    
    public Object getContent() {
        return content;
    }
    
    public void setContent(Object context) {
        this.content = context;
    }
    
    public Object getRootCause() {
        return rootCause;
    }
    
    public void setRootCause(Object rootCause) {
        this.rootCause = rootCause;
    }
}
