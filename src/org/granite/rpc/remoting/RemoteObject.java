/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.rpc.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;

import org.granite.messaging.Channel;
import org.granite.rpc.AsyncResponder;
import org.granite.rpc.AsyncToken;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.RemotingMessage;

/**
 * @author Franck WOLFF
 */
public class RemoteObject {
	
	private Channel channel;
	private String destination;
    
    public RemoteObject(String destination) {
        this.destination = destination;
    }
	
	public RemoteObject(Channel channel, String destination) {
		this.channel = channel;
		this.destination = destination;
	}
	
	public void setChannel(Channel channel) {
	    this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}
	
	public String getDestination() {
		return destination;
	}
	public void setCredentials(String username, String password) {
		setCredentials(username, password, null);
	}

	public void setCredentials(String username, String password, Charset charset) {
		if (username == null || password == null)
			throw new NullPointerException("Username and password must not be null");
		channel.setCredentials(username + ':' + password, charset);
	}

	public boolean isAuthenticated() {
		return channel.isAuthenticated();
	}
	
	public void logout(AsyncResponder responder) {
		channel.logout(responder, false);
	}
	
	public AsyncToken call(String method, Object[] params, AsyncResponder responder) {
		return call(method, params, (responder != null ? new AsyncResponder[]{responder} : null));
	}
	
	public AsyncToken call(String method, Object[] params, AsyncResponder[] responders) {
		RemotingMessage message = new RemotingMessage();
		message.setBody(params != null ? params : new Object[0]);
		message.setOperation(method);
		message.setDestination(destination);
		message.setMessageId(UUIDUtil.randomUUID());
		message.setParameters((Object[])message.getBody());
		message.setTimestamp(System.currentTimeMillis());
		
        AsyncToken token = new AsyncToken(message);
        if (responders != null) {
        	for (AsyncResponder responder : responders)
        		token.addResponder(responder);
        }
		channel.send(token);
		return token;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getProxy(Class<T> clazz, final AsyncResponder responder) {
		return (T)Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				call(method.getName(), args, responder);
				return null;
			}
		});
	}
	
	public static interface Listener {
		
		public void onCall(RemoteObject ro, AsyncToken token);
	}
}
