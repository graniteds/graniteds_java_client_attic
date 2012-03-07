package org.granite.tide.client.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

//import org.granite.example.addressbook.entity.Person;
//import org.granite.example.addressbook.entity.embed.Document;
import org.granite.rpc.AsyncToken;
import org.granite.rpc.events.FaultEvent;
import org.granite.rpc.events.MessageEvent;
import org.granite.rpc.events.ResultEvent;
import org.granite.tide.Component;
import org.granite.tide.Context;
import org.granite.tide.ContextManager;
import org.granite.tide.data.EntityManagerImpl;
import org.granite.tide.impl.ContextManagerImpl;
import org.granite.tide.javafx.JavaFXDataManager;
import org.junit.Assert;
import org.junit.Test;

import flex.messaging.messages.AcknowledgeMessage;


public class TestSimpleCall {

//    @Test
//    public void testSimpleCall() throws Exception {
//        
//        ContextManager contextManager = new ContextManagerImpl();
//        Context context = contextManager.getContext(null);
//        context.setComponentRegistry(new TestComponentRegistry());
//        context.setEntityManager(new EntityManagerImpl("", null, null, null));
//        MockComponent personService = context.byName("personService");
//        personService.setResponseBuilder(new ResponseBuilder() {
//            @Override
//            public MessageEvent buildResponseEvent(AsyncToken token, Component component, String operation, Object[] args) {
//                if (!component.getName().equals("personService")) {
//                    return new FaultEvent(token, new IllegalArgumentException("Destination not found"));
//                }
//                if (operation.equals("findAllPersons") && args.length == 0) {
//                    List<Person> list = new ArrayList<Person>();
//                    list.add(new Person());
//                    list.add(new Person());
//                    AcknowledgeMessage msg = new AcknowledgeMessage();
//                    msg.setBody(list);
//                    return new ResultEvent(token, msg);
//                }
//                else if (operation.equals("createPerson") && args.length == 1) {
//                    Person person = (Person)args[0];
//                    Person p = new Person();
//                    p.setFirstName(person.getFirstName());
//                    p.setLastName(person.getLastName());
//                    AcknowledgeMessage msg = new AcknowledgeMessage();
//                    msg.setBody(p);
//                    return new ResultEvent(token, msg);
//                }
//                return null;
//            }
//        });
//        
//        Future<List<Person>> fpersons = personService.call("findAllPersons");
//        List<Person> persons = fpersons.get();
//        
//        System.out.println("findAllPersons result: " + persons);
//        Assert.assertEquals("Persons result", 2, persons.size());
//        
//        // Create a new Person entity.
//        Person person  = new Person();
//        person.setFirstName("Franck");
//        person.setLastName("Wolff");
//        Document document = new Document();
//        document.setContentType("text/plain");
//        document.setName("test");
//        document.setContent(new byte[0]);
//        person.setDocument(document);
//        
//        // Call the createPerson method on the destination (PersonService) with
//        // the new person as its parameter.
//        Future<Person> fperson = personService.call("createPerson", person);
//        person = fperson.get();
//
//        System.out.println("createPerson result: " + person);
//        Assert.assertEquals("Person", "Wolff", person.getLastName());
//        
//        System.out.println("Done.");
//    }
}
