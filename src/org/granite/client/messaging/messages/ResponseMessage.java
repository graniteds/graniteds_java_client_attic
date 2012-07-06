package org.granite.client.messaging.messages;


public interface ResponseMessage extends Message, MessageChain<ResponseMessage> {

	String getCorrelationId();
	void setCorrelationId(String correlationId);
	
	ResponseMessage copy(String correlationId);
}
