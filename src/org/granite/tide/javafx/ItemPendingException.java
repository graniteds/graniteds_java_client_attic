package org.granite.tide.javafx;

public class ItemPendingException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	
	public ItemPendingException(String message) {
		super(message);
	}

	public void callRespondersResult(Object event) {
	}
	
	public void callRespondersFault(Object event) {
	}
	
	public boolean hasResponders() {
		return false;
	}
}
