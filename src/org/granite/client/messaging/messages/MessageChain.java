package org.granite.client.messaging.messages;


public interface MessageChain<T extends MessageChain<T>> extends Message, Iterable<T> {
	
	void setNext(T next);
	T getNext();
}
