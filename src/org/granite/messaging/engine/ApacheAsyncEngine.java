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

package org.granite.messaging.engine;

import java.net.URI;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.granite.messaging.amf.AMF0Message;

/**
 * @author Franck WOLFF
 */
<<<<<<< HEAD
public class ApacheAsyncEngine extends AbstractEngine {
=======
public class ApacheAsyncEngine extends AbstractHttpClientEngine {
	
	private static final Logger log = Logger.getLogger(ApacheAsyncEngine.class);
>>>>>>> 6e5c8c0... Tide implementation for Java client

	protected HttpAsyncClient httpClient = null;

	@Override
	public synchronized void start() {
		super.start();
		
		try {
			httpClient = new DefaultHttpAsyncClient();
			httpClient.start();
			
			final long timeout = System.currentTimeMillis() + 10000L; // 10sec.
			while (httpClient.getStatus() != IOReactorStatus.ACTIVE) {
				if (System.currentTimeMillis() > timeout)
					throw new TimeoutException("HttpAsyncClient start process two long");
				Thread.sleep(100);
			}
		}
		catch (Exception e) {
			super.stop();
			
			httpClient = null;
			
			exceptionHandler.handle(new EngineException("Could not start Apache HttpAsyncClient", e));
		}
	}

	@Override
	public synchronized boolean isStarted() {
		return (super.isStarted() && httpClient != null && httpClient.getStatus() == IOReactorStatus.ACTIVE);
	}

	@Override
	public synchronized void send(final URI uri, final AMF0Message message, final EngineResponseHandler handler) {
		
		if (!isStarted()) {
			exceptionHandler.handle(new EngineException("Apache HttpAsyncClient not started"));
			return;
		}
		
		PublicByteArrayOutputStream os = new PublicByteArrayOutputStream();
		
		try {
			serialize(message, os);
		}
		catch (Exception e) {
			exceptionHandler.handle(new EngineException("Could not serialize AMF0 message", e));
			return;
		}
		
		final HttpPost request = new HttpPost(uri);
		request.setHeader("Content-Type", CONTENT_TYPE);
		request.setEntity(new ByteArrayEntity(os.getBytes()));
		
		httpClient.execute(request, new FutureCallback<HttpResponse>() {

            public void completed(final HttpResponse response) {
            	AMF0Message responseMessage = null;
            	try {
                	HttpEntity entity = response.getEntity();
            		responseMessage = deserialize(entity.getContent());
				}
            	catch (Exception e) {
            		handler.failed(e);
            		exceptionHandler.handle(new EngineException("Could not deserialize AMF0 message", e));
            		return;
				}
            	handler.completed(responseMessage);
            }

            public void failed(final Exception e) {
            	handler.failed(e);
        		exceptionHandler.handle(new EngineException("Request failed", e));
            }

            public void cancelled() {
            	handler.cancelled();
            }
        });
	}

	@Override
	public synchronized void stop() {
		super.stop();
		
		try {
			httpClient.shutdown();
		}
		catch (InterruptedException e) {
			exceptionHandler.handle(new EngineException("Could not stop Apache HttpAsyncClient", e));
		}
		finally {
			httpClient = null;
		}
	}
}
