/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

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

package org.granite.client.tide;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;

import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.FaultMessage.Code;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.server.ExceptionHandler;
import org.granite.client.tide.server.ServerSession;
import org.granite.client.tide.server.SimpleTideResponder;
import org.granite.client.tide.server.TideFaultEvent;
import org.granite.client.tide.server.TideResponder;
import org.granite.client.tide.server.TideResultEvent;


public abstract class BaseIdentity extends ComponentImpl implements Identity, ExceptionHandler {
	
	private boolean loggedIn;
	private String username;
	

	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public void setLoggedIn(boolean loggedIn) {
		if (loggedIn == this.loggedIn)
			return;
		
		this.loggedIn = loggedIn;
		if (loggedIn)
			getServerSession().afterLogin();
		else
			this.username = null;	
	}

	public String getUsername() {
		return username;
	}
	
    
    public BaseIdentity(final ServerSession serverSession) {
    	super(serverSession);
    	
    	loggedIn = false;
    }
    
    /**
     * 	Triggers a remote call to check is user is currently logged in
     *  Can be used at application startup to handle browser refresh cases
     * 
     *  @param tideResponder a responder for the remote call
     *  @return future result returning the username if logged in or null
     */
    public Future<String> checkLoggedIn(final TideResponder<String> tideResponder) {
    	return super.call("isLoggedIn", new SimpleTideResponder<String>() {
			@Override
			public void result(TideResultEvent<String> event) {
				if (event.getResult() != null) {
					BaseIdentity.this.username = event.getResult();
					setLoggedIn(true);
				}
				else if (isLoggedIn()) {
					setLoggedIn(false);
					
					// Session expired, directly mark the channel as logged out
					getServerSession().sessionExpired();
				}
				
				if (tideResponder != null)
					tideResponder.result(event);
			}
			
			@Override
			public void fault(TideFaultEvent event) {
				if (event.getFault().getCode() == Code.ACCESS_DENIED) {
					// Not in role for the destination
					setLoggedIn(false);
					
					getServerSession().logout(null);
				}
				
				if (tideResponder != null)
					tideResponder.fault(event);
			}
    	});
    }
    
    
    public void login(final String username, String password, final TideResponder<String> tideResponder) {
    	getServerSession().login(username, password);
    	
    	checkLoggedIn(tideResponder);
    }
    
    
    public void logout(final TideResponder<Void> tideResponder) {
    	final Observer observer = new Observer() {
			@SuppressWarnings("unchecked")
			@Override
			public void update(Observable logout, Object event) {
		        setLoggedIn(false);
		        
		        if (tideResponder != null) {
					if (event instanceof TideResultEvent)
				        tideResponder.result((TideResultEvent<Void>)event);
					else if (event instanceof TideFaultEvent)
				        tideResponder.fault((TideFaultEvent)event);
		        }
			}
    	};
    	
    	getServerSession().logout(observer);
    }

    
	@Override
	public boolean accepts(FaultMessage emsg) {
		return emsg.getCode() == Code.NOT_LOGGED_IN;
	}

	@Override
	public void handle(Context context, FaultMessage emsg, TideFaultEvent faultEvent) {
		if (isLoggedIn()) {
			setLoggedIn(false);
			
			// Session expired, directly mark the channel as logged out
			getServerSession().sessionExpired();
		}
	}

}
