package org.granite.client.messaging.channel.amf;

import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;

import flex.messaging.messages.AsyncMessage;

public abstract class AbstractJMSMessage implements Message {

	protected final AsyncMessage message;
	
	public AbstractJMSMessage(AsyncMessage message) {
		this.message = message;
	}

	@Override
	public void acknowledge() throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void clearBody() throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void clearProperties() throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public String getJMSCorrelationID() throws JMSException {
		return message.getCorrelationId();
	}

	@Override
	public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
		return message.getCorrelationId().getBytes();
	}

	@Override
	public int getJMSDeliveryMode() throws JMSException {
		return Message.DEFAULT_DELIVERY_MODE;
	}

	@Override
	public Destination getJMSDestination() throws JMSException {
		final String topicName = (String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER);
		return new Topic() {
			@Override
			public String getTopicName() throws JMSException {
				return topicName;
			}
		};
	}

	@Override
	public long getJMSExpiration() throws JMSException {
		return message.getTimeToLive();
	}

	@Override
	public String getJMSMessageID() throws JMSException {
		return message.getMessageId();
	}

	@Override
	public int getJMSPriority() throws JMSException {
		return Message.DEFAULT_PRIORITY;
	}

	@Override
	public boolean getJMSRedelivered() throws JMSException {
		return false;
	}

	@Override
	public Destination getJMSReplyTo() throws JMSException {
		return null;
	}

	@Override
	public long getJMSTimestamp() throws JMSException {
		return message.getTimestamp();
	}

	@Override
	public String getJMSType() throws JMSException {
		return "AMF";
	}

	@Override
	public void setJMSCorrelationID(String correlationID) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setJMSDestination(Destination destination) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setJMSExpiration(long arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setJMSMessageID(String arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setJMSPriority(int arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setJMSRedelivered(boolean arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setJMSReplyTo(Destination arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setJMSTimestamp(long arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setJMSType(String arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public boolean getBooleanProperty(String arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public byte getByteProperty(String arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public double getDoubleProperty(String arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public float getFloatProperty(String arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public int getIntProperty(String arg0) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public long getLongProperty(String name) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public Object getObjectProperty(String name) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public Enumeration<?> getPropertyNames() throws JMSException {
		return null;
	}

	@Override
	public short getShortProperty(String name) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public String getStringProperty(String name) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public boolean propertyExists(String name) throws JMSException {
		return false;
	}

	@Override
	public void setBooleanProperty(String name, boolean value) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setByteProperty(String name, byte value) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setDoubleProperty(String name, double value) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setFloatProperty(String name, float value) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setIntProperty(String name, int value) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setLongProperty(String arg0, long arg1) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setObjectProperty(String arg0, Object arg1) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setShortProperty(String arg0, short arg1) throws JMSException {
		throw new JMSException("Unsupported operation");
	}

	@Override
	public void setStringProperty(String arg0, String arg1) throws JMSException {
		throw new JMSException("Unsupported operation");
	}
}
