package org.granite.tide.javafx.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;

import org.granite.tide.data.EntityManager;
import org.granite.tide.data.EntityManagerImpl;
import org.granite.tide.javafx.JavaFXDataManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestManagedEntity2 {
    
    private EntityManager entityManager;
    
    @Before
    public void setup() throws Exception {
        entityManager = new EntityManagerImpl("", new JavaFXDataManager(), null, null);
    }
    
    
    @Test
    public void testMergeMap4() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultList", FXCollections.observableArrayList());
        
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("resultCount", 100);
        map2.put("firstResult", 200);
        map2.put("maxResults", 100);
        map2.put("resultList", FXCollections.observableArrayList(new Person(1L, 0L, "P1", "A1", "B1"), new Person(3L, 0L, "P3", "A3", "B3")));
        
        entityManager.mergeExternalData(map2, map, null, null);
        
        Assert.assertEquals("Size", 4, map.size());
        Assert.assertEquals("Result count", 100, map.get("resultCount"));
        Assert.assertEquals("Result list", 2, ((List<?>)map.get("resultList")).size());
    }
    
    @Test
    public void testMergeEntityEmbedded() {
        PersonEmbed person = new PersonEmbed(1L, 0L, "P1", null, null);
        person.setAddress(new EmbeddedAddress("toto"));
        
        person = (PersonEmbed)entityManager.mergeExternalData(person);
        
        person.getAddress().setAddress("tutu");
        
        Assert.assertTrue("Context dirty", entityManager.isDirty());
        Assert.assertTrue("Person dirty", person.isDirty());
        
        PersonEmbed person2 = new PersonEmbed(1L, 1L, "P1", null, null);
        person2.setAddress(new EmbeddedAddress("tutu"));
        
        person = (PersonEmbed)entityManager.mergeExternalData(person2);
        
        Assert.assertEquals("Address reset", "tutu", person.getAddress().getAddress());
        Assert.assertFalse("Person not dirty", person.isDirty());
        Assert.assertFalse("Context dirty", entityManager.isDirty());
    }
}
