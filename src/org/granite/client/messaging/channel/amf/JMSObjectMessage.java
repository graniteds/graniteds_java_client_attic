package org.granite.client.messaging.channel.amf;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import flex.messaging.messages.AsyncMessage;

public class JMSObjectMessage extends AbstractJMSMessage implements ObjectMessage {

	public JMSObjectMessage(AsyncMessage message) {
		super(message);
	}

	@Override
	public Serializable getObject() throws JMSException {
		return (Serializable)message.getBody();
	}

	@Override
	public void setObject(Serializable object) throws JMSException {
		throw new JMSException("Unsupported operation");
	}
}
