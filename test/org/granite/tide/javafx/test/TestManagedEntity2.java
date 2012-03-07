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


@SuppressWarnings("unchecked")
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
}
