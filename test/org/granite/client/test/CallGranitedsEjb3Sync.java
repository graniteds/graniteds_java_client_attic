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

package org.granite.client.test;

import java.net.URI;
import java.net.URISyntaxException;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.channel.ChannelFactory;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.channel.UsernamePasswordCredentials;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.transport.HTTPTransport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportStatusHandler.LogEngineStatusHandler;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public class CallGranitedsEjb3Sync {

	public static void main(String[] args) throws URISyntaxException {		
		
		URI uri = new URI("http://localhost:8080/graniteds-ejb3/graniteamf/amf");
		
		System.out.println("Connecting to: " + uri);

		// Create and configure a transport.
		HTTPTransport transport = new ApacheAsyncTransport();
		transport.setStatusHandler(new LogEngineStatusHandler() {
			
			@Override
			public void handleIO(boolean active) {
				//super.handleIO(active);
			}

			@Override
			public void handleException(TransportException e) {
				//super.handleException(e);
				//sem.release();
			}
		});
		transport.start();
		
		// Create a channel with the specified uri.
		ChannelFactory factory = new ChannelFactory(ContentType.JMF_AMF);
		RemotingChannel channel = factory.newRemotingChannel(transport, "my-graniteamf", uri, 2);

		// Login (credentials will be sent with the first call).
		channel.setCredentials(new UsernamePasswordCredentials("admin", "admin"));

		// Create a remote object with the channel and a destination.
		RemoteService ro = new RemoteService(channel, "person");
		
		try {
			
			System.out.println();
			System.out.println("Fetching all persons and countries at once...");
			
			ResponseMessage message = ro.newInvocation("findAllPersons").appendInvocation("findAllCountries").invoke().get();
			for (ResponseMessage response : message)
				System.out.println(response);
			
			System.out.println();
			System.out.println("Creating new person...");
			
//			// Create a new Person entity.
//			Person person  = new Person();
//			person.setSalutation(Salutation.Mr);
//			person.setFirstName("John");
//			person.setLastName("Doe");
//			
//			// Call the createPerson method on the destination (PersonService) with
//			// the new person as its parameter.
//			message = ro.newInvocation("createPerson", person).invoke().get();
//			System.out.println(message);
			
			System.out.println();
			System.out.println("Fetching all persons...");
			
			channel.setCredentials(new UsernamePasswordCredentials("admin", "admin"));

			message = ro.newInvocation("findAllPersons").invoke().get();
			System.out.println(message);
		}
		catch (Exception e) {
			System.out.println("TryCatch Failed: " + e);
		}
		finally {
			// Stop transport (must be done!)
			transport.stop();
			
			System.out.println("Done.");
		}
	}
}
