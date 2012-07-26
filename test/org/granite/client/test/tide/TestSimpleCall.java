package org.granite.client.test.tide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.messages.Message;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.requests.InvocationMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.test.MockRemoteService;
import org.granite.client.test.ResponseBuilder;
import org.granite.client.test.tide.javafx.Person;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextManager;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.impl.DefaultPlatform;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestSimpleCall {
    
    private ContextManager contextManager;
    private Context ctx;
    private ServerSession serverSession;
    
    
    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new DefaultPlatform());
        contextManager.setInstanceStoreFactory(new TestInstanceStoreFactory());
        ctx = contextManager.getContext();
        serverSession = new ServerSession("test", "/test", "localhost", 8080);
        serverSession.setServiceFactory(new MockServiceFactory());
        ctx.set(serverSession);
        serverSession.start();
    }
    
    @After
    public void tearDown() throws Exception {
    	serverSession.stop();
    }

    @Test
    public void testSimpleCall() throws Exception {        
        Component personService = new ComponentImpl(serverSession);
        ctx.set("personService", personService);
        MockRemoteService.setResponseBuilder(new ResponseBuilder() {
            @Override
            public Message buildResponseMessage(RemoteService service, RequestMessage request) {
            	InvocationMessage invocation = (InvocationMessage)request;
            	
                if (!invocation.getParameters()[0].equals("personService"))
                    return new FaultMessage();
                
                String method = (String)invocation.getParameters()[2];
                Object[] args = ((Object[])invocation.getParameters()[3]);
                
                if (method.equals("findAllPersons") && args.length == 0) {
                    List<Person> list = new ArrayList<Person>();
                    list.add(new Person());
                    list.add(new Person());
                    return new ResultMessage(null, null, list);
                }
                else if (method.equals("createPerson") && args.length == 1) {
                    Person person = (Person)args[0];
                    Person p = new Person();
                    p.setFirstName(person.getFirstName());
                    p.setLastName(person.getLastName());
                    return new ResultMessage(null, null, p);
                }
                return null;
            }
        });
        
        Future<List<Person>> fpersons = personService.call("findAllPersons");
        List<Person> persons = fpersons.get();
        
        System.out.println("findAllPersons result: " + persons);
        Assert.assertEquals("Persons result", 2, persons.size());
        
        // Create a new Person entity.
        Person person  = new Person();
        person.setFirstName("Franck");
        person.setLastName("Wolff");
        
        // Call the createPerson method on the destination (PersonService) with
        // the new person as its parameter.
        Future<Person> fperson = personService.call("createPerson", person);
        person = fperson.get();

        System.out.println("createPerson result: " + person);
        Assert.assertEquals("Person", "Wolff", person.getLastName());
        
        System.out.println("Done.");
    }
}
