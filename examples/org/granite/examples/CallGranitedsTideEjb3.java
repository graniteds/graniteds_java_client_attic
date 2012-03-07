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
import java.util.List;
import java.util.concurrent.Future;
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
import org.granite.tide.Component;
import org.granite.tide.Context;

import org.granite.example.addressbook.entity.Person;
import org.granite.example.addressbook.entity.embed.Document;

/**
 * @author Franck WOLFF
 */
public class CallGranitedsTideEjb3 {

	public static void main(String[] args) throws Exception {
		
		URI uri = new URI("http://localhost:8080/graniteds-ejb3/graniteamf/amf");
		
		System.out.println("Connecting to: " + uri);

		Context context = new Context();
		Component personService = context.byName("person");
		
		Future<List<Person>> fpersons = personService.call("findAllPersons");
		List<Person> persons = fpersons.get();
		
		System.out.println("findAllPersons result: " + persons);
		
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
		Future<Person> fperson = personService.call("createPerson", person);
		System.out.println("createPerson result: " + fperson.get());
		
		System.out.println("Done.");
	}
}
