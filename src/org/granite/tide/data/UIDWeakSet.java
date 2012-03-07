package org.granite.tide.data;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;




/**
 *  Implementation of HashSet that holds weak references to UID entities 
 *  
 *  @author Franck WOLFF
 */
public class UIDWeakSet {
    
    private WeakHashMap<Object, Object>[] table;
    
    
    public UIDWeakSet() {
        this(64);
    }
    
    @SuppressWarnings("unchecked")
    public UIDWeakSet(int capacity) {
        table = new WeakHashMap[capacity];  
    }
    
    public void clear() {
        for (int i = 0; i < table.length; i++)
            table[i] = null;
    }
    
    public Identifiable put(Identifiable uidObject) {
        int h = hash(uidObject.getClass().getName() + ":" + uidObject.getUid());
        
        WeakHashMap<Object, Object> dic = table[h];
        if (dic == null) {
            dic = new WeakHashMap<Object, Object>();
            table[h] = dic;
        }
        
        Identifiable old = null;
        for (Object o : dic.keySet()) {
            if (o == uidObject)
                return (Identifiable)o;
            
            if (((Identifiable)o).getUid() == uidObject.getUid() && o.getClass().getName().equals(uidObject.getClass().getName())) {
                old = (Identifiable)o;
                dic.remove(o);
                break;
            }
        }
        
        dic.put(uidObject, null);
        
        return old;
    }
    
    public Identifiable get(String uid) {
        int h = hash(uid);
        
        Identifiable uidObject = null;
        
        WeakHashMap<Object, Object> dic = table[h];
        if (dic != null) {
            for (Object o : dic.keySet()) {
                if ((o.getClass().getName() + ":" + ((Identifiable)o).getUid()).equals(uid)) {
                    uidObject = (Identifiable)o;
                    break;
                }
            }
        }
        
        return uidObject;
    }

    public static interface Matcher {
        
        public boolean match(Object o);
    }
    
    public Object find(Matcher matcher) {
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null) {
                for (Object o : dic.keySet()) {
                    if (matcher.match(o))
                        return o;
                }
            }
        }
        return null;
    }

    public static interface Operation {
        
        public boolean apply(Object o);
    }
    
    public void apply(Operation operation) {
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null) {
                for (Object o : dic.keySet())
                    operation.apply(o);
            }
        }
    }
    
    public Identifiable remove(String uid) {
        int h = hash(uid);
        
        Identifiable uidObject = null;
        
        WeakHashMap<Object, Object> dic = table[h];
        if (dic != null) {
            for (Object o : dic.keySet()) {
                if ((o.getClass().getName() + ":" + ((Identifiable)o).getUid()).equals(uid)) {
                    uidObject = (Identifiable)o;
                    dic.remove(o);
                    break;
                }
            }
        }
        
        return uidObject;
    }
    
    public int size() {
        int size = 0;
        
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null)
                size += dic.size();
        }
        
        return size;
    }
    
    public List<Object> data() {
        List<Object> d = new ArrayList<Object>();
        
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null)
                d.addAll(dic.keySet());
        }
        return d;
    }
    
    private int hash(String uid) {
        int h = 0;
        int max = uid.length();
        for (int i = 0; i < max; i++)
            h = (31 * h) + uid.charAt(i);
        return (Math.abs(h) % table.length);
    }
}