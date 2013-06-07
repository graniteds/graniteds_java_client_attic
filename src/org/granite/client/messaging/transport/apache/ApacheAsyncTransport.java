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

package org.granite.client.messaging.transport.apache;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorStatus;
import org.apache.http.params.BasicHttpParams;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.AbstractTransport;
import org.granite.client.messaging.transport.HTTPTransport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportFuture;
import org.granite.client.messaging.transport.TransportIOException;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.logging.Logger;
import org.granite.util.PublicByteArrayOutputStream;

/**
 * @author Franck WOLFF
 */
public class ApacheAsyncTransport extends AbstractTransport implements HTTPTransport {
	
	private static final Logger log = Logger.getLogger(ApacheAsyncTransport.class);

	protected final BasicHttpParams params;
	
	protected DefaultHttpAsyncClient httpClient = null;
	protected CookieStore cookieStore = new BasicCookieStore();
	
	public ApacheAsyncTransport() {
		this(null);
	}
	
	public ApacheAsyncTransport(BasicHttpParams params) {
		this.params = params;
	}

	public void configure(DefaultHttpAsyncClient client) {
		// Can be overwritten...
	}

	@Override
	public synchronized boolean start() {
		if (httpClient != null && httpClient.getStatus() == IOReactorStatus.ACTIVE)
			return true;
		
		stop();
		
		log.info("Starting Apache HttpAsyncClient transport...");
		
		try {
			httpClient = new DefaultHttpAsyncClient();
			
			configure(httpClient);
			if (params != null)
				params.copyParams(httpClient.getParams());
			
			httpClient.setCookieStore(cookieStore);
			httpClient.start();
			
			final long timeout = System.currentTimeMillis() + 10000L; // 10sec.
			while (httpClient.getStatus() != IOReactorStatus.ACTIVE) {
				if (System.currentTimeMillis() > timeout)
					throw new TimeoutException("HttpAsyncClient start process too long");
				Thread.sleep(100);
			}
			
			log.info("Apache HttpAsyncClient transport started.");
			return true;
		}
		catch (Exception e) {
			httpClient = null;
			getStatusHandler().handleException(new TransportException("Could not start Apache HttpAsyncClient", e));

			log.error(e, "Apache HttpAsyncClient failed to start.");
			return false;
		}
	}

	@Override
	public TransportFuture send(final Channel channel, final TransportMessage message) throws TransportException {
		synchronized (this) {
		    if (httpClient == null || httpClient.getStatus() != IOReactorStatus.ACTIVE) {
		    	TransportIOException e = new TransportIOException(message, "Apache HttpAsyncClient not started");
		    	getStatusHandler().handleException(e);
		    	throw e;
			}
		}
	    
		if (!message.isConnect())
			getStatusHandler().handleIO(true);
		
		try {
		    HttpPost request = new HttpPost(channel.getUri());
			request.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
			request.setHeader("Content-Type", message.getContentType());
			request.setHeader("GDSClientType", "java");	// Notify the server that we expect Java serialization mode
			
			PublicByteArrayOutputStream os = new PublicByteArrayOutputStream(512);
			try {
				message.encode(os);
			}
			catch (IOException e) {
				throw new TransportException("Message serialization failed: " + message.getId(), e);
			}
			request.setEntity(new ByteArrayEntity(os.getBytes(), 0, os.size()));

//			request.setEntity(new DeferredInputStreamEntity(message));
			
			final Future<HttpResponse> future = httpClient.execute(request, new FutureCallback<HttpResponse>() {
	
	            public void completed(HttpResponse response) {
	            	if (!message.isConnect())
	            		getStatusHandler().handleIO(false);
	            	
	        		InputStream is = null;
	        		try {
	        			is = response.getEntity().getContent();
	        			channel.onMessage(is);
	        		}
	        		catch (Exception e) {
		            	getStatusHandler().handleException(new TransportIOException(message, "Could not deserialize message", e));
					}
	        		finally {
	        			if (is != null) try {
	        				is.close();
	        			}
	        			catch (Exception e) {
	        			}
	        		}
	            }
	
	            public void failed(Exception e) {
	            	if (!message.isConnect())
	            		getStatusHandler().handleIO(false);
	
	            	channel.onError(message, e);
	            	getStatusHandler().handleException(new TransportIOException(message, "Request failed", e));
	            }
	
	            public void cancelled() {
	            	if (!message.isConnect())
	            		getStatusHandler().handleIO(false);
	            	
	            	channel.onCancelled(message);
	            }
	        });
			
			return new TransportFuture() {
				@Override
				public boolean cancel() {
					boolean cancelled = false;
					try {
						cancelled = future.cancel(true);
					}
					catch (Exception e) {
						log.error(e, "Cancel request failed");
					}
					return cancelled;
				}
			};
		}
		catch (Exception e) {
        	if (!message.isConnect())
        		getStatusHandler().handleIO(false);
			
			TransportIOException f = new TransportIOException(message, "Request failed", e);
        	getStatusHandler().handleException(f);
			throw f;
		}
	}
	
	public synchronized void poll(final Channel channel, final TransportMessage message) throws TransportException {
		throw new TransportException("Not implemented");
	}

	@Override
	public synchronized void stop() {
		if (httpClient == null)
			return;
		
		log.info("Stopping Apache HttpAsyncClient transport...");

		super.stop();
		
		try {
			httpClient.shutdown();
		}
		catch (Exception e) {
			getStatusHandler().handleException(new TransportException("Could not stop Apache HttpAsyncClient", e));

			log.error(e, "Apache HttpAsyncClient failed to stop properly.");
		}
		finally {
			httpClient = null;
		}
		
		log.info("Apache HttpAsyncClient transport stopped.");
	}
	
//	static class DeferredInputStreamEntity extends BasicHttpEntity {
//
//		private final TransportMessage message;
//		private InputStream content = null;
//		
//		public DeferredInputStreamEntity(TransportMessage message) {
//			this.message = message;
//		}
//		
//		@Override
//		public synchronized InputStream getContent() throws IllegalStateException {
//			if (content == null) {
//				PublicByteArrayOutputStream os = new PublicByteArrayOutputStream(256);
//				try {
//					message.encode(os);
//				}
//				catch (IOException e) {
//					throw new TransportException("Message serialization failed: " + message.getId(), e);
//				}
//				content = new ByteArrayInputStream(os.getBytes(), 0, os.size());
//			}
//			return content;
//		}
//	}
}
