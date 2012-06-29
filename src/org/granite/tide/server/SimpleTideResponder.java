package org.granite.tide.server;


public abstract class SimpleTideResponder<T> implements TideMergeResponder<T> {
	
	private final T mergeWith;
	
	public SimpleTideResponder() {
		this.mergeWith = null;
	}
	
	public SimpleTideResponder(T mergeWith) {
		this.mergeWith = mergeWith;
	}

	@Override
	public void fault(TideFaultEvent event) {
		// Do nothing by default
	}
	
	@Override
	public T getMergeResultWith() {
		return mergeWith;
	}

}
