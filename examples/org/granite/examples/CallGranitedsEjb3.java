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

package org.granite.examples;

import java.net.URI;
import java.util.concurrent.Semaphore;

import org.granite.messaging.Channel;
import org.granite.messaging.engine.ApacheAsyncEngine;
import org.granite.messaging.engine.Engine;
import org.granite.messaging.engine.EngineException;
import org.granite.messaging.engine.LogEngineExceptionHandler;
import org.granite.rpc.AsyncResponder;
import org.granite.rpc.events.FaultEvent;
import org.granite.rpc.events.ResultEvent;
import org.granite.rpc.remoting.RemoteObject;

import org.granite.example.addressbook.entity.Person;
import org.granite.example.addressbook.entity.embed.Document;

/**
 * @author Franck WOLFF
 */
public class CallGranitedsEjb3 {

	public static void main(String[] args) throws Exception {
		
		URI uri = new URI("http://localhost:8080/graniteds-ejb3/graniteamf/amf");
		
		System.out.println("Connecting to: " + uri);

		final Semaphore sem = new Semaphore(1);
		
		sem.acquire();

		// Create and configure engine.
		Engine engine = new ApacheAsyncEngine();
		engine.setExceptionHandler(new LogEngineExceptionHandler() {
			@Override
			public void handle(EngineException e) {
				super.handle(e);
				sem.release();
			}
		});
		
		// Create a channel with the specified uri.
		Channel channel = new Channel(engine, "my-graniteamf", uri);

		// Create a remote object with the channel and a destination.
		RemoteObject ro = new RemoteObject(channel, "person");
		
		// Login (credentials will be sent with the first remoteObject call).
		ro.setCredentials("admin", "admin");

		// Call the findAllPersons method on the destination (PersonService).
		ro.call("findAllPersons", null, new AsyncResponder() {
			
			@Override
			public void result(ResultEvent event) {
				System.out.println("findAllPersons result: " + event.getResult());
				sem.release();
			}
			
			@Override
			public void fault(FaultEvent event) {
				System.out.println("findAllPersons fault: " + event.getMessage());
				sem.release();
			}
		});

		sem.acquire();
		
		// Create a new Person entity.
		Person person  = new Person();
		person.setFirstName("Franck");
		person.setLastName("Wolff");
		Document document = new Document();
		document.setContentType("text/plain");
		document.setName("test");
		document.setContent(new byte[0]);
		person.setDocument(document);
		
		// Call the createPerson method on the destination (PersonService) with
		// the new person as its parameter.
		ro.call("createPerson", new Object[]{person}, new AsyncResponder() {
			
			@Override
			public void result(ResultEvent event) {
				System.out.println("createPerson result: " + event.getResult());
				sem.release();
			}
			
			@Override
			public void fault(FaultEvent event) {
				System.out.println("createPerson fault: " + event.getMessage());
				sem.release();
			}
		});
		
		
		sem.acquire();

		// Call (again) the findAllPersons method on the destination (PersonService).
		ro.call("findAllPersons", null, new AsyncResponder() {
			
			@Override
			public void result(ResultEvent event) {
				System.out.println("findAllPersons result: " + event.getResult());
				sem.release();
			}
			
			@Override
			public void fault(FaultEvent event) {
				System.out.println("findAllPersons fault: " + event.getMessage());
				sem.release();
			}
		});
		
		sem.acquire();

		// Logout.
		ro.logout(new AsyncResponder() {
			@Override
			public void result(ResultEvent event) {
				System.out.println("Logout success");
				sem.release();
			}
			@Override
			public void fault(FaultEvent event) {
				System.out.println("Logout failed: " + event.getMessage());
				sem.release();
			}
		});
		
		sem.acquire();
	
		// Stop engine (must be done!)
		engine.stop();
		
		System.out.println("Done.");
	}
}
