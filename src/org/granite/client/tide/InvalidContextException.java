package org.granite.client.tide;


public class InvalidContextException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private String contextId;

    public InvalidContextException(String contextId, String message) {
        super(message);
    }
    
    public String getContextId() {
        return contextId;
    }

}
