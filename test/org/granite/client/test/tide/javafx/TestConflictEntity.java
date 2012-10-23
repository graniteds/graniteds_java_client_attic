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

package org.granite.client.test.tide.javafx;

import java.util.Arrays;

import org.granite.client.persistence.javafx.PersistentSet;
import org.granite.client.test.tide.MockInstanceStoreFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextManager;
import org.granite.client.tide.data.Conflicts;
import org.granite.client.tide.data.DataConflictListener;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.EntityManager.Update;
import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.javafx.JavaFXPlatform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestConflictEntity {

    private ContextManager contextManager;
    private Context ctx;
    @SuppressWarnings("unused")
	private DataManager dataManager;
    private EntityManager entityManager;
    
    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new JavaFXPlatform());
        contextManager.setInstanceStoreFactory(new MockInstanceStoreFactory());
        ctx = contextManager.getContext("");
        entityManager = ctx.getEntityManager();
        dataManager = ctx.getDataManager();
    }
    
    @Test
    public void testEntityCollectionRefs() {
        Person p = new Person(1L, 0L, "P01", null, null);
        p.setContacts(new PersistentSet<Contact>());
        Contact c1 = new Contact(1L, 0L, "C01", null);
        c1.setPerson(p);
        p.getContacts().add(c1);
        p = (Person)entityManager.mergeExternalData(p);

        Person np = new Person(1L, 0L, "P01", null, null);
        np.setContacts(new PersistentSet<Contact>(false));
        Contact nc = new Contact(1L, 0L, "C01", null);
        nc.setPerson(np);

        MergeContext mergeContext = entityManager.initMerge();
        entityManager.handleUpdates(mergeContext, "SID", Arrays.asList(new Update(UpdateKind.REMOVE, nc)));

        Assert.assertEquals("Person contacts empty", 0, p.getContacts().size());
    }

    @Test
    public void testEntityCollectionMultiRefs() {
        PersonUniDir p1 = new PersonUniDir(1L, 0L, "P01", null, null);
        p1.setContacts(new PersistentSet<Contact2>());
        Contact2 c1 = new Contact2(1L, 0L, "C01", null);
        p1.getContacts().add(c1);
        p1 = (PersonUniDir)entityManager.mergeExternalData(p1);
        
        PersonUniDir p2 = new PersonUniDir(2L, 0L, "P02", null, null);
        p2.setContacts(new PersistentSet<Contact2>());
        Contact2 c1b = new Contact2(1L, 0L, "C01", null);
        p2.getContacts().add(c1b);
        Contact2 c2b = new Contact2(2L, 0L, "C02", null);
        p2.getContacts().add(c2b);
        p2 = (PersonUniDir)entityManager.mergeExternalData(p2);

        Contact2 nc = new Contact2(1L, 0L, "C01", null);

        MergeContext mergeContext = entityManager.initMerge();
        entityManager.handleUpdates(mergeContext, "SID", Arrays.asList(new Update(UpdateKind.REMOVE, nc)));

        Assert.assertEquals("Person 1 contacts empty", 0, p1.getContacts().size());
        Assert.assertEquals("Person 2 one contact left", 1, p2.getContacts().size());
    }


     @Test
     public void testEntityCollectionRemoveConflictServer() {
         Person p = new Person(1L, 0L, "P01", null, null);
         p.setContacts(new PersistentSet<Contact>());
         Contact c1 = new Contact(1L, 0L, "C01", null);
         c1.setPerson(p);
         p.getContacts().add(c1);
         p = (Person)entityManager.mergeExternalData(p);

         // Contact is locally modified
         c1.setEmail("toto@toto.org");

         Person np = new Person(1L, 0L, "P01", null, null);
         np.setContacts(new PersistentSet<Contact>());
         Contact nc = new Contact(1L, 0L, "C01", null);
         nc.setPerson(np);
         
         final Conflicts[] conflicts = new Conflicts[1];

         entityManager.addListener(new DataConflictListener() {           
             @Override
             public void onConflict(EntityManager em, Conflicts cs) {
                 conflicts[0] = cs;
             }
         });
         
         // Receive an external removal event
         MergeContext mergeContext = entityManager.initMerge();
         entityManager.handleUpdates(mergeContext, "SID", Arrays.asList(new Update(UpdateKind.REMOVE, nc)));

         Assert.assertEquals("Conflict detected", 1, conflicts[0].getConflicts().size());

         conflicts[0].acceptAllServer();

         Assert.assertEquals("Person contacts empty", 0, p.getContacts().size());
     }

     @Test
     public void testEntityCollectionRemoveConflictClient() {
         Person p = new Person(1L, 0L, "P01", null, null);
         p.setContacts(new PersistentSet<Contact>());
         Contact c1 = new Contact(1L, 0L, "C01", null);
         c1.setPerson(p);
         p.getContacts().add(c1);
         p = (Person)entityManager.mergeExternalData(p);

         // Contact is locally modified
         c1.setEmail("toto@toto.org");

         Person np = new Person(1L, 0L, "P01", null, null);
         np.setContacts(new PersistentSet<Contact>());
         Contact nc = new Contact(1L, 0L, "C01", null);
         nc.setPerson(np);
         
         final Conflicts[] conflicts = new Conflicts[1];

         entityManager.addListener(new DataConflictListener() {           
             @Override
             public void onConflict(EntityManager em, Conflicts cs) {
                 conflicts[0] = cs;
             }
         });
         
         // Receive an external removal event
         MergeContext mergeContext = entityManager.initMerge();
         entityManager.handleUpdates(mergeContext, "SID", Arrays.asList(new Update(UpdateKind.REMOVE, nc)));

         Assert.assertEquals("Conflict detected", 1, conflicts[0].getConflicts().size());

         conflicts[0].acceptAllClient();

         Assert.assertEquals("Person contacts not empty", 1, p.getContacts().size());
     }
}
