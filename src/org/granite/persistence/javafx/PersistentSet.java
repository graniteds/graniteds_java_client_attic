package org.granite.tide.javafx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.granite.logging.Logger;
import org.granite.messaging.amf.RemoteClass;
import org.granite.persistence.LazyableCollection;


@RemoteClass("org.granite.messaging.persistence.ExternalizablePersistentSet")
public class PersistentSet<T> implements Set<T>, ObservableList<T>, LazyableCollection, Externalizable {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(PersistentSet.class);

    @SuppressWarnings("unused")
	private boolean initializing = false;
    private boolean initialized = false;
    private String metadata = null;
    private boolean dirty = false;
    
    private ObservableList<T> oset;
   
    private ListChangeListener<T> listener = new ListChangeListener<T>() {
        public void onChanged(ListChangeListener.Change<? extends T> change) {
            if (!initialized)
                return;
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved() || change.wasReplaced() || change.wasPermutated()) {
                    dirty = true;
                    break;
                }
            }
        }
    };

    
    public PersistentSet() {
        this.oset = FXCollections.observableArrayList();
        this.initialized = true;
        this.oset.addListener(listener);
    }

    public PersistentSet(Set<T> set) {
        this.oset = FXCollections.observableArrayList(set);
        this.initialized = true;
        this.oset.addListener(listener);
    }
    
    public PersistentSet(boolean initialized) {
        this.oset = FXCollections.observableArrayList();
        this.initialized = initialized;         
        if (initialized)
            this.oset.addListener(listener);
    }


    public final boolean isInitialized() {
        return initialized;
    }

    public void initializing() {
        clear();
        initializing = true;
        dirty = false;
        oset.removeListener(listener);
    }

    public void initialize() {
        initializing = false;
        initialized = true;
        dirty = false;
        oset.addListener(listener);
    }

    public void uninitialize() {
        oset.removeListener(listener);
        initialized = false;
        clear();
        dirty = false;
    }
    
    public PersistentSet<T> clone(boolean uninitialize) {
        PersistentSet<T> coll = new PersistentSet<T>(initialized && !uninitialize);
        coll.metadata = metadata;
        if (initialized) {
            for (T obj : this)
                coll.add(obj);
        }
        coll.dirty = dirty;
        return coll; 
    }
    
    public void addListener(InvalidationListener listener) {
        oset.addListener(listener);
    }

    public void removeListener(InvalidationListener listener) {
        oset.removeListener(listener);
    }
    
    private Map<ListChangeListener<? super T>, ListChangeListener<? super T>> listenerWrappers = new IdentityHashMap<ListChangeListener<? super T>, ListChangeListener<? super T>>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void addListener(ListChangeListener<? super T> listener) {
        ListChangeListener<? super T> listenerWrapper = new ListChangeListenerWrapper(this, listener);
        listenerWrappers.put(listener, listenerWrapper);
        oset.addListener(listenerWrapper);
    }

    public void removeListener(ListChangeListener<? super T> listener) {
        ListChangeListener<? super T> listenerWrapper = listenerWrappers.remove(listener);
        if (listenerWrapper != null)
        	oset.removeListener(listenerWrapper);
    }
    
    public boolean isEmpty() {
        return oset.isEmpty();
    }

    public boolean contains(Object o) {
        return oset.contains(o);
    }

    public Iterator<T> iterator() {
        return oset.iterator();
    }

    public boolean add(T e) {
        return oset.add(e);
    }

    public boolean remove(Object o) {
        return oset.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return oset.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c) {
        List<T> toAdd = new ArrayList<T>(c);
        toAdd.removeAll(oset);
        return oset.addAll(toAdd);
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        List<T> toAdd = new ArrayList<T>(c);
        toAdd.removeAll(oset);
        return oset.addAll(index, toAdd);
    }

    public boolean removeAll(Collection<?> c) {
        return oset.removeAll(c);
    }

    public boolean equals(Object o) {
        return oset.equals(o);
    }

    public int hashCode() {
        return oset.hashCode();
    }

    public T get(int index) {
        return oset.get(index);
    }

    public void add(int index, T element) {
        oset.add(index, element);
    }

    public boolean addAll(T... elements) {
        List<T> toAdd = Arrays.asList(elements);
        toAdd.removeAll(oset);
        return oset.addAll(toAdd);
    }

    public void clear() {
        oset.clear();
    }

    public int indexOf(Object o) {
        return oset.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return oset.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return oset.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return oset.listIterator(index);
    }

    public void remove(int arg0, int arg1) {
        oset.remove(arg0, arg1);
    }

    public T remove(int index) {
        return oset.remove(index);
    }

    public boolean removeAll(T... elements) {
        return oset.removeAll(elements);
    }

    public boolean retainAll(Collection<?> c) {
        return oset.retainAll(c);
    }

    public boolean retainAll(T... elements) {
        return oset.retainAll(elements);
    }

    public T set(int index, T element) {
        return oset.set(index, element);
    }

    public boolean setAll(Collection<? extends T> coll) {
        return oset.setAll(coll);
    }

    public boolean setAll(T... arg0) {
        return oset.setAll(arg0);
    }

    public int size() {
        return oset.size();
    }

    public Object[] toArray() {
        return oset.toArray();
    }

    @SuppressWarnings("hiding")
	public <T> T[] toArray(T[] a) {
        return oset.toArray(a);
    }

    public List<T> subList(int fromIndex, int toIndex) {
        return oset.subList(fromIndex, toIndex);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + (initialized ? "" : " (uninitialized)") + (dirty ? " (dirty)" : "") + ":" + oset.toString();
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        initialized = ((Boolean)input.readObject()).booleanValue();
        metadata = (String)input.readObject();
        if (initialized) {
            dirty = ((Boolean)input.readObject()).booleanValue();
            T[] array = (T[])input.readObject();
            oset = FXCollections.observableArrayList(array);
        }
    }

    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeObject(Boolean.valueOf(initialized));
        output.writeObject(metadata);
        if (initialized) {
            output.writeObject(Boolean.valueOf(dirty));
            output.writeObject(oset.toArray());
        }
    }
}
