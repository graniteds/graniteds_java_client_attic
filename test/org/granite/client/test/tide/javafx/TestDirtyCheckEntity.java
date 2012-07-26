package org.granite.client.test.tide.javafx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import org.granite.client.persistence.LazyableCollection;
import org.granite.client.persistence.javafx.PersistentMap;
import org.granite.client.persistence.javafx.PersistentSet;
import org.granite.client.test.tide.TestInstanceStoreFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextManager;
import org.granite.client.tide.data.impl.DirtyCheckContextImpl.Change;
import org.granite.client.tide.data.spi.DataManager.ChangeKind;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.javafx.JavaFXDataManager;
import org.granite.client.tide.javafx.JavaFXPlatform;
import org.granite.client.tide.server.ServerSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


@SuppressWarnings("unchecked")
public class TestDirtyCheckEntity {
    
    private ContextManager contextManager;
    private Context ctx;
    private JavaFXDataManager dataManager;
    private EntityManager entityManager;
    private ServerSession serverSession;
    
    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new JavaFXPlatform());
        contextManager.setInstanceStoreFactory(new TestInstanceStoreFactory());
        ctx = contextManager.getContext("");
        entityManager = ctx.getEntityManager();
        dataManager = (JavaFXDataManager)ctx.getDataManager();
        serverSession = new ServerSession();
        ctx.set(serverSession);
    }
    
    @Test
    public void testDirtyCheckEntity() {
        Person person = new Person(1L, 0L, "P1", null, null);
        Contact contact = new Contact(1L, 0L, "C1", null);
        contact.setPerson(person);
        person.setContacts(new PersistentSet<Contact>());
        person.getContacts().add(contact);
        
        Person person2 = new Person(2L, 0L, "P2", null, null);
        
        contact = (Contact)entityManager.mergeExternalData(contact);
        person2 = (Person)entityManager.mergeExternalData(person2);
        
        BooleanProperty personDirty = new SimpleBooleanProperty();
        personDirty.bind(person.dirtyProperty());
        BooleanProperty ctxDirty = new SimpleBooleanProperty();
        ctxDirty.bind(dataManager.dirtyProperty());
        
        contact.setEmail("toto");

        Assert.assertTrue("Contact dirty", contact.isDirty());
        
        contact.setEmail(null);
        
        Assert.assertFalse("Contact not dirty", contact.isDirty());
        
        contact.getPerson().setFirstName("toto");
        
        Assert.assertTrue("Person dirty", person.isDirty());
        Assert.assertTrue("Person dirty 2", personDirty.get());
        Assert.assertTrue("Context dirty", entityManager.isDirty());
        Assert.assertTrue("Context dirty 2", ctxDirty.get());
        Assert.assertFalse("Contact not dirty", contact.isDirty());
        
        contact.getPerson().setFirstName(null);
        
        Assert.assertFalse("Person not dirty", contact.getPerson().isDirty());
        Assert.assertFalse("Person not dirty 2", personDirty.get());
        Assert.assertFalse("Context not dirty", entityManager.isDirty());
        Assert.assertFalse("Context not dirty 2", ctxDirty.get());
        Assert.assertFalse("Contact not dirty", contact.isDirty());
        
        Contact contact2 = new Contact(2L, 0L, "C2", null);
        contact2.setPerson(person);
        person.getContacts().add(contact2);
                    
        Assert.assertTrue("Person dirty", person.isDirty());
        Assert.assertTrue("Person dirty 2", personDirty.get());
        Assert.assertTrue("Context dirty", entityManager.isDirty());
        Assert.assertTrue("Context dirty 2", ctxDirty.get());
        Assert.assertFalse("Contact not dirty", contact.isDirty());
        
        person.getContacts().remove(1);
        
        Assert.assertFalse("Person not dirty", contact.getPerson().isDirty());
        Assert.assertFalse("Person not dirty 2", personDirty.get());
        Assert.assertFalse("Context not dirty", entityManager.isDirty());
        Assert.assertFalse("Context not dirty 2", ctxDirty.get());
        Assert.assertFalse("Contact not dirty", contact.isDirty());
        
        contact.setEmail("toto");
        person2.setLastName("tutu");
        
        Assert.assertTrue("Contact dirty", contact.isDirty());
        Assert.assertTrue("Person 2 dirty", person2.isDirty());
        
        Person receivedPerson = new Person(1L, 1L, person.getUid(), null, null);
        Contact receivedContact = new Contact(1L, 1L, contact.getUid(), null);
        receivedContact.setPerson(receivedPerson);
        receivedPerson.setContacts(new PersistentSet<Contact>());
        receivedPerson.getContacts().add(receivedContact);
        
        serverSession.handleResult(ctx, null, null, null, receivedPerson, null);
        
        Assert.assertFalse("Contact not dirty", contact.isDirty());
        Assert.assertTrue("Person 2 dirty", person2.isDirty());
        Assert.assertTrue("Context dirty", entityManager.isDirty());
        
        receivedPerson = new Person(2L, 1L, person2.getUid(), null, null);
        
        serverSession.handleResult(ctx, null, null, null, receivedPerson, null);
        Assert.assertFalse("Person 2 dirty", person2.isDirty());
        Assert.assertFalse("Context dirty", entityManager.isDirty());
    }
    
    @Test
    public void testDirtyCheckEntityAddedToCollReset() {
        Person person = new Person(1L, 0L, "P1", "toto", null);
        person.setContacts(new PersistentSet<Contact>());
        person = (Person)entityManager.mergeExternalData(person);
        
        Assert.assertFalse("Context not dirty", entityManager.isDirty());
        
        Contact contact = new Contact(null, null, "C1", null);
        contact.setPerson(person);
        person.getContacts().add(contact);
        
        Assert.assertTrue("Context dirty after new item", entityManager.isDirty());
        
        contact.setEmail("test@test.com");
        
        Assert.assertTrue("Context dirty after item change", entityManager.isDirty());
        
        entityManager.resetEntity(person);
        
        Assert.assertFalse("Context not dirty after item removed", entityManager.isDirty());
    }
    
    @Test
    public void testDirtyCheckEntityMap() {
        PersonMap person = new PersonMap(1L, 0L, "P1", "toto", null);
        person.setMapEmbed(new PersistentMap<String, EmbeddedAddress>());
        person = (PersonMap)entityManager.mergeExternalData(person);
    
        Assert.assertFalse("Context not dirty", entityManager.isDirty());
    
        person.getMapEmbed().put("test", new EmbeddedAddress("bla"));
        
        Assert.assertTrue("Context dirty after put", entityManager.isDirty());
    
        entityManager.resetEntity(person);
    
        Assert.assertFalse("Context not dirty after reset", entityManager.isDirty());
    }
    
    @Test
    public void testDirtyCheckEntityMap2() {
        PersonMap person = new PersonMap(1L, 0L, "P1", "toto", null);
        person.setMapEmbed(new PersistentMap<String, EmbeddedAddress>());
        person.getMapEmbed().put("test", new EmbeddedAddress("bla"));
        person = (PersonMap)entityManager.mergeExternalData(person);
    
        Assert.assertFalse("Context not dirty", entityManager.isDirty());
    
        person.getMapEmbed().put("test", new EmbeddedAddress("blo"));
    
        Assert.assertTrue("Context dirty after put", entityManager.isDirty());
    
        entityManager.resetEntity(person);
    
        Assert.assertFalse("Context not dirty after reset", entityManager.isDirty());
        Assert.assertEquals("Map reset", "bla", person.getMapEmbed().get("test").getAddress());
    }
    
    @Test
    public void testDirtyCheckEntityBidir() {        
        Classification parent = new Classification(1L, 0L, "P1", null);
        parent.setSubclasses(new PersistentSet<Classification>());
        parent.setSuperclasses(new PersistentSet<Classification>());
        
        parent = (Classification)entityManager.mergeExternalData(parent);
        
        Classification child = new Classification(2L, 0L, "C1", null);
        child.setSubclasses(new PersistentSet<Classification>());
        child.setSuperclasses(new PersistentSet<Classification>());
        
        child = (Classification)entityManager.mergeExternalData(child);
         
        Assert.assertFalse("Classification not dirty", entityManager.isDirty());
         
        parent.getSubclasses().add(child);
        child.getSuperclasses().add(parent);
         
        Assert.assertTrue("Classification dirty", entityManager.isDirty());
        
        Classification parent2 = new Classification(1L, 1L, "P1", null);
        parent2.setSubclasses(new PersistentSet<Classification>());
        parent2.setSuperclasses(new PersistentSet<Classification>());
        Classification child2 = new Classification(2L, 0L, "C1", null);
        child2.setSubclasses(new PersistentSet<Classification>());
        child2.setSuperclasses(new PersistentSet<Classification>());
        parent2.getSubclasses().add(child2);
        child2.getSuperclasses().add(parent2);
        
        List<Classification> res = new ArrayList<Classification>();
        Collections.addAll(res, parent2, child2);
        
        entityManager.mergeExternalData(res);
         
        Assert.assertFalse("Classification merged not dirty", entityManager.isDirty());
    }
         
      
    @Test
    public void testDirtyCheckEntityBidir2() {
         Classification parent = new Classification(1L, 0L, "P1", null);
         parent.setSubclasses(new PersistentSet<Classification>());
         parent.setSuperclasses(new PersistentSet<Classification>());
         
         parent = (Classification)entityManager.mergeExternalData(parent);
         
         Classification child = new Classification(2L, 0L, "C1", null);
         child.setSubclasses(new PersistentSet<Classification>());
         child.setSuperclasses(new PersistentSet<Classification>());
         
         child = (Classification)entityManager.mergeExternalData(child);
          
         Assert.assertFalse("Classification not dirty", entityManager.isDirty());
         
         parent.setName("Test");
         parent.getSubclasses().add(child);
         child.getSuperclasses().add(parent);
         
         Assert.assertTrue("Classification dirty", entityManager.isDirty());
         
         Classification parent2 = new Classification(1L, 0L, "P1", null);
         parent2.setSubclasses(new PersistentSet<Classification>());
         parent2.setSuperclasses(new PersistentSet<Classification>());
         Classification child2 = new Classification(2L, 0L, "C1", null);
         child2.setSubclasses(new PersistentSet<Classification>());
         child2.setSuperclasses(new PersistentSet<Classification>());
         parent2.getSubclasses().add(child2);
         child2.getSuperclasses().add(parent2);
         
         List<Classification> res = new ArrayList<Classification>();
         Collections.addAll(res, parent2, child2);
         
         entityManager.mergeExternalData(res);
         
         Assert.assertTrue("Classification merged still dirty", entityManager.isDirty());
         Assert.assertTrue("Parent dirty", parent.isDirty());
         Assert.assertFalse("Child not dirty", child.isDirty());
     }
                  
     @Test
     public void testDirtyCheckEntityBidir3() {
         Classification parent = new Classification(1L, 0L, "P1", null);
         parent.setSubclasses(new PersistentSet<Classification>());
         parent.setSuperclasses(new PersistentSet<Classification>());
         
         parent = (Classification)entityManager.mergeExternalData(parent);
         
         Classification child = new Classification(2L, 0L, "C1", null);
         child.setSubclasses(new PersistentSet<Classification>());
         child.setSuperclasses(new PersistentSet<Classification>());
         
         child = (Classification)entityManager.mergeExternalData(child);
         
         Assert.assertFalse("Classification not dirty", entityManager.isDirty());
         
         parent.getSubclasses().add(child);
         child.getSuperclasses().add(parent);
         child.setName("Test");
         
         Assert.assertTrue("Classification dirty", entityManager.isDirty());
             
         Classification parent2 = new Classification(1L, 1L, "P1", null);
         parent2.setSubclasses(new PersistentSet<Classification>());
         parent2.setSuperclasses(new PersistentSet<Classification>());
         Classification child2 = new Classification(2L, 0L, "C1", null);
         child2.setSubclasses(new PersistentSet<Classification>());
         child2.setSuperclasses(new PersistentSet<Classification>());
         parent2.getSubclasses().add(child2);
         child2.getSuperclasses().add(parent2);
             
         entityManager.mergeExternalData(parent2);
         
         Assert.assertTrue("Classification merged still dirty", entityManager.isDirty());
         Assert.assertFalse("Parent dirty", parent.isDirty());
         Assert.assertTrue("Child dirty", child.isDirty());
     }
     
     @Test
     public void testDirtyCheckEntityBigNumber() {
         PersonBigNum person = new PersonBigNum(1L, 0L, "P1", null, null);
         person.setBigInt(BigInteger.valueOf(100L));
          
         BooleanProperty ctxDirty = new SimpleBooleanProperty();
         ctxDirty.bind(dataManager.dirtyProperty());
         BooleanProperty personDirty = new SimpleBooleanProperty();
         personDirty.bind(person.dirtyProperty());
         
         person = (PersonBigNum)entityManager.mergeExternalData(person);
         
         Assert.assertFalse("Person not dirty", person.isDirty());
         
         person.setBigInt(BigInteger.valueOf(200L));
         
         Assert.assertTrue("Person dirty", person.isDirty());
         Assert.assertTrue("Person dirty 2", personDirty.get());
         Assert.assertTrue("Context dirty", ctxDirty.get());
         
         person.setBigInt(BigInteger.valueOf(100L));
         
         Assert.assertFalse("Person not dirty", person.isDirty());
         Assert.assertFalse("Person not dirty 2", personDirty.get());
         Assert.assertFalse("Context not dirty", ctxDirty.get());
          
         person.setBigInt(null);
          
         Assert.assertTrue("Person dirty", person.isDirty());
         Assert.assertTrue("Person dirty 2", personDirty.get());
         Assert.assertTrue("Context dirty", ctxDirty.get());
         
         person.setBigInt(BigInteger.valueOf(100L));
         
         Assert.assertFalse("Person not dirty", person.isDirty());
         Assert.assertFalse("Person not dirty 2", personDirty.get());
         Assert.assertFalse("Context not dirty", ctxDirty.get());
     }
     
     @Test
     public void testDirtyCheckEntityByteArray() throws IOException {
         PersonByteArray person = new PersonByteArray(1L, 0L, "P1", null, null);
         
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         baos.write("JDKEK".getBytes("UTF-8"));
         byte[] pic1 = baos.toByteArray();
          
         baos = new ByteArrayOutputStream();
         baos.write("FSDLKZJH".getBytes("UTF-8"));
         byte[] pic2 = baos.toByteArray();
         
         baos = new ByteArrayOutputStream();
         baos.write("JDKEK".getBytes("UTF-8"));
         byte[] pic3 = baos.toByteArray();
         
         BooleanProperty ctxDirty = new SimpleBooleanProperty();
         ctxDirty.bind(dataManager.dirtyProperty());
         BooleanProperty personDirty = new SimpleBooleanProperty();
         personDirty.bind(person.dirtyProperty());
         
         person.setPicture(pic1);
          
         person = (PersonByteArray)entityManager.mergeExternalData(person);
          
         person.setPicture(pic2);
          
         Assert.assertTrue("Context dirty", entityManager.isDirty());
         Assert.assertTrue("Person dirty 1", personDirty.get());
         Assert.assertTrue("Person dirty 2", person.isDirty());
         
         person.setPicture(pic3);
          
         Assert.assertFalse("Context dirty", entityManager.isDirty());
         Assert.assertFalse("Person not dirty 1", personDirty.get());
         Assert.assertFalse("Person not dirty 2", person.isDirty());
     }
     
     @Test
     public void testDirtyCheckEntityCircularRef() {
         Classification parent = new Classification(1L, 0L, "P1", null);
         parent.setSubclasses(new PersistentSet<Classification>());
         parent.setSuperclasses(new PersistentSet<Classification>());
         
         parent = (Classification)entityManager.mergeExternalData(parent);
         
         Classification child = new Classification(2L, 0L, "C1", null);
         child.setSubclasses(new PersistentSet<Classification>());
         child.setSuperclasses(new PersistentSet<Classification>());
         
         child = (Classification)entityManager.mergeExternalData(child);
          
         Assert.assertFalse("Classification not dirty", entityManager.isDirty());
          
         parent.getSubclasses().add(child);
         child.getSuperclasses().add(parent);
          
         Assert.assertTrue("Classification dirty", entityManager.isDirty());
         
         Classification parent2 = new Classification(1L, 1L, "P1", null);
         parent2.setSubclasses(new PersistentSet<Classification>());
         parent2.setSuperclasses(new PersistentSet<Classification>());
         Classification child2 = new Classification(2L, 1L, "C1", null);
         child2.setSubclasses(new PersistentSet<Classification>());
         child2.setSuperclasses(new PersistentSet<Classification>());
         parent2.getSubclasses().add(child2);
         child2.getSuperclasses().add(parent2);
         
         List<Object> res = new ArrayList<Object>();
         Collections.addAll(res, parent2, child2);
          
         entityManager.mergeExternalData(res);
          
         Assert.assertFalse("Classification merged not dirty", entityManager.isDirty());
     }

     @Test
     public void testDirtyCheckEntityCollection() {
         Person person = new Person(1L, 0L, "P1", null, null);
         person.setContacts(new PersistentSet<Contact>());
         person = (Person)entityManager.mergeExternalData(person);
         
         Contact contact = new Contact(1L, 0L, "C1", "toto@tutu.com");
         contact.setPerson(person);
         person.getContacts().add(contact);
         Contact contact2 = new Contact(2L, 0L, "C2", "test@tutu.com");
         contact2.setPerson(person);
         person.getContacts().add(contact2);
          
         Assert.assertTrue("Context dirty", entityManager.isDirty());
          
         person.getContacts().remove(0);
          
         Assert.assertEquals("Saved events", 1, ((List<Change>)entityManager.getSavedProperties(person).get("contacts")).size());
         Assert.assertEquals("Saved event location", 0, ((List<Change>)entityManager.getSavedProperties(person).get("contacts")).get(0).getLocation());
     }

     @Test
     public void testDirtyCheckEntityCollection1() {
         Person person = new Person(1L, 0L, "P1", null, null);
         person.setContacts(new PersistentSet<Contact>());
         person = (Person)entityManager.mergeExternalData(person);

         Contact contact = new Contact(1L, 0L, "C1", "toto@tutu.com");
         contact.setPerson(person);
         person.getContacts().add(contact);
         Contact contact2 = new Contact(2L, 0L, "C2", "test@tutu.com");
         contact2.setPerson(person);
         person.getContacts().add(0, contact2);

         Assert.assertTrue("Context dirty", entityManager.isDirty());

         person.getContacts().remove(1);

         Assert.assertEquals("Saved events", 1, ((List<Change>)entityManager.getSavedProperties(person).get("contacts")).size());
         Assert.assertEquals("Saved event location", 0, ((List<Change>)entityManager.getSavedProperties(person).get("contacts")).get(0).getLocation());

         person.getContacts().remove(0);
         Assert.assertFalse("Context dirty", entityManager.isDirty());
     }

     @Test
     public void testDirtyCheckEntityCollection2() {
         Person person = new Person(1L, 0L, "P1", null, null);
         person.setContacts(new PersistentSet<Contact>());
         Contact contact = new Contact(1L, 0L, "C1", "toto@tutu.com");
         contact.setPerson(person);
         person.getContacts().add(contact);
         Contact contact2 = new Contact(2L, 0L, "C2", "test@tutu.com");
         contact2.setPerson(person);
         person.getContacts().add(contact2);
         person = (Person)entityManager.mergeExternalData(person);
         
         person.getContacts().remove(1);
         person.getContacts().remove(0);
          
         Contact contact3 = new Contact(2L, 0L, "C2", "test@tutu.com");
         contact3.setPerson(person);
         person.getContacts().add(0, contact3);
          
         Assert.assertTrue("Context dirty", entityManager.isDirty());
          
         Assert.assertEquals("Saved events", 1, ((List<Change>)entityManager.getSavedProperties(person).get("contacts")).size());
         Assert.assertEquals("Saved remove event location", ChangeKind.REMOVE, ((List<Change>)entityManager.getSavedProperties(person).get("contacts")).get(0).getKind());
         Assert.assertEquals("Saved remove event location", 0, ((List<Change>)entityManager.getSavedProperties(person).get("contacts")).get(0).getLocation());

         Contact contact4 = new Contact(1L, 0L, "C1", "toto@tutu.com");
         contact4.setPerson(person);
         person.getContacts().add(0, contact4);

         Assert.assertFalse("Context not dirty", entityManager.isDirty());
     }

     @Test
     public void testDirtyCheckEntityCollection3() {
         Person person = new Person(1L, 0L, "P1", null, null);
         person.setContacts(new PersistentSet<Contact>());
         Contact contact = new Contact(1L, 0L, "C1", "t1@tutu.com");
         contact.setPerson(person);
         person.getContacts().add(contact);
         Contact contact2 = new Contact(2L, 0L, "C2", "t2@tutu.com");
         contact2.setPerson(person);
         person.getContacts().add(contact2);
         Contact contact3 = new Contact(3L, 0L, "C3", "t3@tutu.com");
         contact3.setPerson(person);
         person.getContacts().add(contact3);
         Contact contact4 = new Contact(4L, 0L, "C4", "t4@tutu.com");
         contact4.setPerson(person);
         person.getContacts().add(contact4);
         Contact contact5 = new Contact(5L, 0L, "C5", "t5@tutu.com");
         contact5.setPerson(person);
         person.getContacts().add(contact5);
         Contact contact6 = new Contact(6L, 0L, "C6", "t6@tutu.com");
         contact6.setPerson(person);
         person.getContacts().add(contact6);
         person = (Person)entityManager.mergeExternalData(person);

         Contact c3 = person.getContacts().remove(2);

         Assert.assertTrue("Context dirty after remove 1", entityManager.isDirty());

         Contact c6 = person.getContacts().remove(4);

         Assert.assertTrue("Context dirty after remove 2", entityManager.isDirty());

         person.getContacts().add(2, c3);
         person.getContacts().add(5, c6);

         Assert.assertFalse("Context dirty", entityManager.isDirty());
     }

     @Test
     public void testDirtyCheckEntityCollection4() {
         Person person = new Person(1L, 0L, "P1", null, null);
         person.setContacts(new PersistentSet<Contact>());
         Contact contact = new Contact(1L, 0L, "C1", "t1@tutu.com");
         contact.setPerson(person);
         person.getContacts().add(contact);
         Contact contact2 = new Contact(2L, 0L, "C2", "t2@tutu.com");
         contact2.setPerson(person);
         person.getContacts().add(contact2);
         Contact contact3 = new Contact(3L, 0L, "C3", "t3@tutu.com");
         contact3.setPerson(person);
         person.getContacts().add(contact3);
         Contact contact4 = new Contact(4L, 0L, "C4", "t4@tutu.com");
         contact4.setPerson(person);
         person.getContacts().add(contact4);
         Contact contact5 = new Contact(5L, 0L, "C5", "t5@tutu.com");
         contact5.setPerson(person);
         person.getContacts().add(contact5);
         Contact contact6 = new Contact(6L, 0L, "C6", "t6@tutu.com");
         contact6.setPerson(person);
         person.getContacts().add(contact6);
         person = (Person)entityManager.mergeExternalData(person);

         Contact c3 = person.getContacts().remove(2);
         Contact c6 = person.getContacts().remove(4);
         
         person.getContacts().add(1, c6);
         person.getContacts().add(4, c3);

         Assert.assertTrue("Context dirty", entityManager.isDirty());
         
         person.getContacts().remove(1);
         person.getContacts().remove(3);
         
         person.getContacts().add(4, c6);
         person.getContacts().add(2, c3);
         
         Assert.assertFalse("Context not dirty", entityManager.isDirty());
     }
     
     @Test
     public void testDirtyCheckEntityEmbedded() {
         PersonEmbed person = new PersonEmbed(1L, 0L, "P1", null, null);
         person.setAddress(new EmbeddedAddress("toto"));
         
         BooleanProperty ctxDirty = new SimpleBooleanProperty();
         ctxDirty.bind(dataManager.dirtyProperty());
         BooleanProperty personDirty = new SimpleBooleanProperty();
         personDirty.bind(person.dirtyProperty());
         
         person = (PersonEmbed)entityManager.mergeExternalData(person);
         
         person.getAddress().setAddress("tutu");
         
         Assert.assertTrue("Context dirty", entityManager.isDirty());
         Assert.assertTrue("Person dirty", person.isDirty());
         Assert.assertTrue("Person dirty 2", personDirty.get());
         
         person.getAddress().setAddress("toto");
         
         Assert.assertFalse("Context dirty", entityManager.isDirty());
         Assert.assertFalse("Person not dirty", person.isDirty());
     }
     
     @Test
     public void testDirtyCheckEntityNestedEmbedded() {
         PersonEmbedNested person = new PersonEmbedNested(1L, 0L, "P1", null, null);
         person.setAddress(new EmbeddedAddress2("toto"));
         person.getAddress().setLocation(new EmbeddedLocation("test", null));
         
         BooleanProperty ctxDirty = new SimpleBooleanProperty();
         ctxDirty.bind(dataManager.dirtyProperty());
         BooleanProperty personDirty = new SimpleBooleanProperty();
         personDirty.bind(person.dirtyProperty());
         
         person = (PersonEmbedNested)entityManager.mergeExternalData(person);
         
         person.getAddress().getLocation().setCity("truc");
         
         Assert.assertTrue("Context dirty", entityManager.isDirty());
         Assert.assertTrue("Person dirty", person.isDirty());
         Assert.assertTrue("Person dirty 2", personDirty.get());
         
         person.getAddress().getLocation().setCity("test");
         
         Assert.assertFalse("Context dirty", entityManager.isDirty());
         Assert.assertFalse("Person not dirty", person.isDirty());
     }
     
     @Test
     public void testDirtyCheckEntityEnum() {
         Person person = new Person(1L, 0L, "P1", null, null);
         person.setSalutation(Salutation.Dr);
          
         BooleanProperty ctxDirty = new SimpleBooleanProperty();
         ctxDirty.bind(dataManager.dirtyProperty());
         BooleanProperty personDirty = new SimpleBooleanProperty();
         personDirty.bind(person.dirtyProperty());
         
         person = (Person)entityManager.mergeExternalData(person);
         
         Assert.assertFalse("Person not dirty", person.isDirty());
          
         person.setSalutation(Salutation.Mr);
          
         Assert.assertTrue("Person dirty", person.isDirty());
         Assert.assertTrue("Person dirty 2", personDirty.get());
         Assert.assertTrue("Context dirty", ctxDirty.get());
          
         person.setSalutation(Salutation.Dr);
          
         Assert.assertFalse("Person not dirty", person.isDirty());
         Assert.assertFalse("Person not dirty 2", personDirty.get());
         Assert.assertFalse("Context not dirty", ctxDirty.get());
         
         person.setSalutation(null);
          
         Assert.assertTrue("Person dirty", person.isDirty());
         Assert.assertTrue("Person dirty 2", personDirty.get());
         Assert.assertTrue("Context dirty", ctxDirty.get());
          
         person.setSalutation(Salutation.Dr);
          
         Assert.assertFalse("Person not dirty", person.isDirty());
         Assert.assertFalse("Person not dirty 2", personDirty.get());
         Assert.assertFalse("Context not dirty", ctxDirty.get());
     }
     
     @Test
     public void testDirtyCheckEntityGDS614() {
         Person person = new Person();
         person.setContacts(new PersistentSet<Contact>());         
         
         BooleanProperty ctxDirty = new SimpleBooleanProperty();
         ctxDirty.bind(dataManager.dirtyProperty());
         BooleanProperty personDirty = new SimpleBooleanProperty();
         personDirty.bind(person.dirtyProperty());

         entityManager.mergeExternalData(person);
          
         Assert.assertFalse("Person not dirty", person.isDirty());
          
         person.setLastName("Test");
          
         Assert.assertTrue("Person dirty", person.isDirty());
         Assert.assertTrue("Person dirty 2", personDirty.get());
         Assert.assertTrue("Context dirty", ctxDirty.get());
          
         person.setLastName(null);
          
         Assert.assertFalse("Person not dirty", person.isDirty());
         Assert.assertFalse("Person not dirty 2", personDirty.get());
         Assert.assertFalse("Context not dirty", ctxDirty.get());
          
         person.setFirstName("Toto");
          
         Assert.assertTrue("Person dirty", person.isDirty());
         Assert.assertTrue("Person dirty 2", personDirty.get());
         Assert.assertTrue("Context dirty", ctxDirty.get());
          
         person.setFirstName("");
          
         Assert.assertFalse("Person not dirty", person.isDirty());
         Assert.assertFalse("Person not dirty 2", personDirty.get());
         Assert.assertFalse("Context not dirty", ctxDirty.get());
         
         Contact contact = new Contact();
         contact.setPerson(person);
         person.getContacts().add(contact);
         
         Assert.assertTrue("Person dirty", person.isDirty());
         Assert.assertTrue("Person dirty 2", personDirty.get());
         Assert.assertTrue("Context dirty", ctxDirty.get());
         
         // The contact is now dirty too, this make the test finally fail 
         contact.setEmail("toto@example.org");
         
         Assert.assertTrue("Contact dirty", contact.isDirty()); 
         Assert.assertTrue("Context dirty", ctxDirty.get()); 
         
         // Removing the the dirty contact makes the context clean because it's not referenced any more
         person.getContacts().remove(0);
         
         Assert.assertFalse("Person not dirty", person.isDirty());
         Assert.assertFalse("Person not dirty 2", personDirty.get());
         Assert.assertFalse("Context not dirty", ctxDirty.get());
         
         // The contact is not managed any more, so dirty check does not apply
         contact.setEmail("tutu@example.org"); 
         
         // This fails if email is not cleand 
         Assert.assertFalse("Context not dirty", ctxDirty.get());
     }
     
     @Test
     public void testDirtyCheckEntityLazy() {
         Classification parent = new Classification(1L, 0L, "P1", null);
         parent.setSubclasses(new PersistentSet<Classification>());
         parent.setSuperclasses(new PersistentSet<Classification>());
         Classification child = new Classification(2L, 0L, "C1", null);
         child.setSubclasses(new PersistentSet<Classification>());
         child.setSuperclasses(new PersistentSet<Classification>());
         parent.getSubclasses().add(child);
         child.getSuperclasses().add(parent);
          
         parent = (Classification)entityManager.mergeExternalData(parent);
          
         Assert.assertFalse("Classification not dirty", entityManager.isDirty());
         
         ((LazyableCollection)child.getSuperclasses()).uninitialize();
          
         Assert.assertFalse("Classification not dirty after uninit", entityManager.isDirty());
     }
     
     @Test
     public void testDirtyCheckNewEntity() {
         Person person = new Person();
         person.setFirstName("toto");
         entityManager.mergeExternalData(person);

         Person person2 = new Person(1L, 0L, person.getUid(), "toto", null);
          
         entityManager.mergeExternalData(person2);
          
         Assert.assertFalse("Context dirty", entityManager.isDirty());
     }
      
     @Test
     public void testDirtyCheckNewEntityAddedToColl() {
         Person person = new Person(1L, 0L, "P1", "toto", null);
         person.setContacts(new PersistentSet<Contact>());
         person = (Person)entityManager.mergeExternalData(person);
          
         Assert.assertFalse("Context not dirty", entityManager.isDirty());
         
         Contact contact = new Contact();
         contact.setPerson(person);
         person.getContacts().add(contact);
      
         Assert.assertTrue("Context dirty after new item", entityManager.isDirty());
          
         contact.setEmail("test@test.com");
          
         Assert.assertTrue("Context dirty after item change", entityManager.isDirty());
          
         person.getContacts().remove(0);
          
         Assert.assertFalse("Context not dirty after item removed", entityManager.isDirty());
     }
     
     @Test
     public void testDirtyCheckNewEntityAddedToCollReset() {
         Person person = new Person(1L, 0L, "P1", "toto", null);
         person.setContacts(new PersistentSet<Contact>());
         person = (Person)entityManager.mergeExternalData(person);
          
         Assert.assertFalse("Context not dirty", entityManager.isDirty());
          
         Contact contact = new Contact();
         contact.setPerson(person);
         person.getContacts().add(contact);
          
         Assert.assertTrue("Context dirty after new item", entityManager.isDirty());
          
         contact.setEmail("test@test.com");
          
         Assert.assertTrue("Context dirty after item change", entityManager.isDirty());
         
         entityManager.resetEntity(person);
         
         Assert.assertFalse("Context not dirty after item removed", entityManager.isDirty());
     }
}
