package org.granite.client.tide.javafx;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;

import javax.validation.ConstraintViolation;

import org.granite.client.persistence.LazyableCollection;
import org.granite.client.tide.collections.ManagedPersistentCollection;
import org.granite.client.tide.collections.ManagedPersistentMap;
import org.granite.client.tide.collections.javafx.JavaFXManagedPersistentCollection;
import org.granite.client.tide.collections.javafx.JavaFXManagedPersistentMap;
import org.granite.client.tide.data.Identifiable;
import org.granite.client.tide.data.Transient;
import org.granite.client.tide.data.impl.AbstractDataManager;
import org.granite.client.tide.data.spi.EntityDescriptor;
import org.granite.client.util.javafx.DataNotifier;
import org.granite.client.validation.javafx.ConstraintViolationEvent;
import org.granite.logging.Logger;


public class JavaFXDataManager extends AbstractDataManager {
	
	private static final Logger log = Logger.getLogger(JavaFXDataManager.class);
    
    private TrackingHandler trackingHandler;
    
    public void setTrackingHandler(TrackingHandler trackingHandler) {
        this.trackingHandler = trackingHandler;
    }
    
    public BooleanProperty dirty = new ReadOnlyBooleanWrapper(this, "dirty", false);
    
    public ReadOnlyBooleanProperty dirtyProperty() {
        return dirty;
    }
    
    public boolean isDirty() {
    	return dirty.get();
    }
    
    
    @Override
    public Object getProperty(Object object, String propertyName) {
        try {
            Method m = object.getClass().getMethod("get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1));
            return m.invoke(object);
        }
        catch (NoSuchMethodException e) {
            try {
                Method m = object.getClass().getMethod("is" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1));
                return m.invoke(object);
            }
            catch (Exception f) {
                throw new RuntimeException("Could not get property " + propertyName + " on object " + object, f);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not get property " + propertyName + " on object " + object, e);
        }
    }
    
    @Override
    public void setProperty(Object object, String propertyName, Object oldValue, Object newValue) {
        try {
            Method[] methods = object.getClass().getMethods();
            String setter = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            for (Method m : methods) {
                if (m.getName().equals(setter) && m.getParameterTypes().length == 1 && m.getParameterTypes()[0].isInstance(newValue)) {
                    m.invoke(object, newValue);
                    break;
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not get property " + propertyName + " on object " + object, e);
        }
    }

    @Override
    public void setInternalProperty(Object object, String propertyName, Object value) {
        Method[] methods = object.getClass().getMethods();
        String property = propertyName + "Property";
        String setter = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        boolean found = false;
        for (Method m : methods) {
        	if (m.getName().equals(property) && m.getParameterTypes().length == 0 && ObservableValue.class.isAssignableFrom(m.getReturnType())) {
                try {
	                @SuppressWarnings("unchecked")
	                Property<Object> p = (Property<Object>)m.invoke(object);
	                p.setValue(value);
	        		found = true;
                }
                catch (Exception e) {
                    throw new RuntimeException("Could not setValue on property " + propertyName + " on object " + object, e);
                }
        	}
        }
        if (!found) {
            for (Method m : methods) {
            	if (m.getName().equals(setter) && m.getParameterTypes().length == 1 && m.getParameterTypes()[0].isInstance(value)) {
            		try {
	            		m.invoke(object, value);
	            		found = true;
	            		break;
            		}
            		catch (Exception e) {
                        throw new RuntimeException("Could not call setter on property " + propertyName + " on object " + object, e);
            		}
            	}
            }
        }
        if (!found)
        	log.warn("No property found for object " + object + " name " + propertyName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getPropertyValues(Object object, boolean includeReadOnly, boolean includeTransient) {
        return getPropertyValues(object, Collections.EMPTY_LIST, includeReadOnly, includeTransient);
    }

    @Override
    public Map<String, Object> getPropertyValues(Object object, List<String> excludedProperties, boolean includeReadOnly, boolean includeTransient) {
        EntityDescriptor desc = getEntityDescriptor(object);
        
        Map<String, Object> values = new LinkedHashMap<String, Object>();
        for (Method m : object.getClass().getMethods()) {
            if (!((m.getName().endsWith("Property") && ObservableValue.class.isAssignableFrom(m.getReturnType()) 
                    || (m.getName().startsWith("get") && (Collection.class.isAssignableFrom(m.getReturnType()) || Map.class.isAssignableFrom(m.getReturnType()))))))
                continue;
            
            if (!includeTransient && m.isAnnotationPresent(Transient.class))
                continue;
            
            String pname = m.getName().startsWith("get") 
                ? Introspector.decapitalize(m.getName().substring(3))
                : m.getName().substring(0, m.getName().length()-8);
                
            if (desc.getDirtyPropertyName() != null && desc.getDirtyPropertyName().equals(pname))
                continue;
            
            try {
                if (excludedProperties.contains(pname))
                    continue;
                
                if (m.getName().endsWith("Property")) {
                    @SuppressWarnings("unchecked")
                    ReadOnlyProperty<Object> p = (ReadOnlyProperty<Object>)m.invoke(object);
                    
                    if (!includeReadOnly && !(p instanceof WritableValue))
                        continue;
                    
                    values.put(p.getName(), p.getValue());
                }
                else {
                    Object value = m.invoke(object);
                    
                    values.put(pname, value);
                }
            }
            catch (Exception e) {
                throw new RuntimeException("Could not get property " + m + " on object " + object, e);
            }
        }
        return values;
    }

    @Override
    public ManagedPersistentCollection<Object> newPersistentCollection(Identifiable parent, String propertyName, LazyableCollection nextList) {
        return new JavaFXManagedPersistentCollection<Object>(parent, propertyName, nextList);
    }

    @Override
    public ManagedPersistentMap<Object, Object> newPersistentMap(Identifiable parent, String propertyName, LazyableCollection nextMap) {
        return new JavaFXManagedPersistentMap<Object, Object>(parent, propertyName, nextMap);
    }

    
    public class EntityPropertyChangeListener<E> implements ChangeListener<E> {
        @Override
        public void changed(ObservableValue<? extends E> property, E oldValue, E newValue) {
            if (property instanceof Property<?>) {
                if (((Property<?>)property).getBean() == null)
                    throw new IllegalStateException("Property bean must be defined");
                if (((Property<?>)property).getName() == null || ((Property<?>)property).getName().trim().length() == 0)
                    throw new IllegalStateException("Property name must be defined");
                trackingHandler.entityPropertyChangeHandler(((Property<?>)property).getBean(), ((Property<?>)property).getName(), oldValue, newValue);
            }
        }
    }
    
    public class EntityListChangeListener<E> implements ListChangeListener<E> {
        @Override
        public void onChanged(Change<? extends E> change) {
            while (change.next()) {
            	if (change.wasReplaced()) {
            		Object[] replaced = new Object[change.getAddedSize()];
            		for (int i = 0; i < change.getAddedSize(); i++)
            			replaced[i] = new Object[] { change.getRemoved().get(i), change.getAddedSubList().get(i) };
                    trackingHandler.entityCollectionChangeHandler(ChangeKind.REPLACE, change.getList(), change.getFrom(), replaced);
            		return;
            	}
            	if (change.wasRemoved())
                    trackingHandler.entityCollectionChangeHandler(ChangeKind.REMOVE, change.getList(), change.getFrom(), change.getRemoved().toArray());
                if (change.wasAdded())
                    trackingHandler.entityCollectionChangeHandler(ChangeKind.ADD, change.getList(), change.getFrom(), change.getAddedSubList().toArray());
//                else if (change.wasReplaced())
//                    trackingHandler.entityCollectionChangeHandler("replace", change.getList(), change.getFrom(), change.)
            }
        }
    }
    
    public class DefaultListChangeListener<E> implements ListChangeListener<E> {
        @Override
        public void onChanged(ListChangeListener.Change<? extends E> change) {
            while (change.next()) {
            	if (change.wasReplaced()) {
            		Object[] replaced = new Object[change.getAddedSize()];
            		for (int i = 0; i < change.getAddedSize(); i++)
            			replaced[i] = new Object[] { change.getRemoved().get(i), change.getAddedSubList().get(i) };
                    trackingHandler.entityCollectionChangeHandler(ChangeKind.REPLACE, change.getList(), change.getFrom(), replaced);
            		return;
            	}
                if (change.wasRemoved())
                    trackingHandler.collectionChangeHandler(ChangeKind.REMOVE, change.getList(), change.getFrom(), change.getRemoved().toArray());
                if (change.wasAdded())
                    trackingHandler.collectionChangeHandler(ChangeKind.ADD, change.getList(), change.getFrom(), change.getAddedSubList().toArray());
            }
        }
    }
    
    public class EntityMapChangeListener<K, V> implements MapChangeListener<K, V> {
        @Override
        public void onChanged(MapChangeListener.Change<? extends K, ? extends V> change) {
            if (change.wasAdded() && change.wasRemoved())
                trackingHandler.entityMapChangeHandler(ChangeKind.REPLACE, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueRemoved(), change.getValueAdded() }});
            else if (change.wasRemoved())
                trackingHandler.entityMapChangeHandler(ChangeKind.REMOVE, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueRemoved() }});
            else if (change.wasAdded())
                trackingHandler.entityMapChangeHandler(ChangeKind.ADD, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueAdded() }});
        }
    }

    public class DefaultMapChangeListener<K, V> implements MapChangeListener<K, V> {
        @Override
        public void onChanged(MapChangeListener.Change<? extends K, ? extends V> change) {
            if (change.wasAdded() && change.wasRemoved())
                trackingHandler.mapChangeHandler(ChangeKind.REPLACE, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueRemoved(), change.getValueAdded() }});
            else if (change.wasRemoved())
                trackingHandler.mapChangeHandler(ChangeKind.REMOVE, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueRemoved() }});
            else if (change.wasAdded())
                trackingHandler.mapChangeHandler(ChangeKind.ADD, change.getMap(), 0, new Object[] { new Object[] { change.getKey(), change.getValueAdded() }});
        }
    }
 
    private ListChangeListener<Object> listChangeListener = new DefaultListChangeListener<Object>();
    private MapChangeListener<Object, Object> mapChangeListener = new DefaultMapChangeListener<Object, Object>();
    private ChangeListener<Object> entityPropertyChangeListener = new EntityPropertyChangeListener<Object>();
    private ListChangeListener<Object> entityListChangeListener = new EntityListChangeListener<Object>();
    private MapChangeListener<Object, Object> entityMapChangeListener = new EntityMapChangeListener<Object, Object>();
    
    private WeakHashMap<Object, TrackingType> trackingListeners = new WeakHashMap<Object, TrackingType>();
    
    
    @Override
    public void startTracking(Object previous, Object parent) {
        if (previous == null || trackingListeners.containsKey(previous))
            return;
        
        if (previous instanceof ObservableList<?>) {
            if (parent != null) {
                ((ObservableList<?>)previous).addListener(entityListChangeListener);
                trackingListeners.put(previous, TrackingType.ENTITY_COLLECTION);
            }
            else {
                ((ObservableList<?>)previous).addListener(listChangeListener);
                trackingListeners.put(previous, TrackingType.COLLECTION);
            }
        }
        else if (previous instanceof ObservableMap<?, ?>) {
            if (parent != null) {
                ((ObservableMap<?, ?>)previous).addListener(entityMapChangeListener);
                trackingListeners.put(previous, TrackingType.ENTITY_MAP);
            }
            else {
                ((ObservableMap<?, ?>)previous).addListener(mapChangeListener);
                trackingListeners.put(previous, TrackingType.MAP);
            }
        }
        else if (parent != null || previous instanceof Identifiable) {
            for (ObservableValue<?> property : instrospectProperties(previous))
                property.addListener(entityPropertyChangeListener);
            trackingListeners.put(previous, TrackingType.ENTITY_PROPERTY);
        }
    }

    @Override
    public void stopTracking(Object previous, Object parent) {
        if (previous == null || !trackingListeners.containsKey(previous))
            return;
        
        if (previous instanceof ObservableList<?>) {
            if (parent != null)
                ((ObservableList<?>)previous).removeListener(entityListChangeListener);
            else
                ((ObservableList<?>)previous).removeListener(listChangeListener);
        }
        else if (previous instanceof ObservableMap<?, ?>) {
            if (parent != null)
                ((ObservableMap<?, ?>)previous).removeListener(entityMapChangeListener);
            else
                ((ObservableMap<?, ?>)previous).removeListener(mapChangeListener);
        }
        else if (parent != null || previous instanceof Identifiable) {
            for (ObservableValue<?> property : instrospectProperties(previous))
                property.removeListener(entityPropertyChangeListener);
        }
        
        trackingListeners.remove(previous);
    }

    @Override
    public void clear() {
        Iterator<Object> ikey = trackingListeners.keySet().iterator();
        while (ikey.hasNext()) {
            Object obj = ikey.next();
            TrackingType type = trackingListeners.get(obj);
            switch (type) {
            case COLLECTION:
                ((ObservableList<?>)obj).removeListener(listChangeListener);
                break;
            case MAP:
                ((ObservableMap<?, ?>)obj).removeListener(mapChangeListener);
                break;
            case ENTITY_PROPERTY:
                for (ObservableValue<?> property : instrospectProperties(obj))
                    property.removeListener(entityPropertyChangeListener);
                break;
            case ENTITY_COLLECTION:
                ((ObservableList<?>)obj).removeListener(listChangeListener);
                break;
            case ENTITY_MAP:
                ((ObservableMap<?, ?>)obj).removeListener(mapChangeListener);
                break;
            }
        }
    }
    
    private List<ObservableValue<?>> instrospectProperties(Object obj) {
        List<ObservableValue<?>> properties = new ArrayList<ObservableValue<?>>();
        EntityDescriptor desc = getEntityDescriptor(obj);
        for (Method m : obj.getClass().getMethods()) {
            if (m.getParameterTypes().length != 0 || !m.getName().endsWith("Property") || !ObservableValue.class.isAssignableFrom(m.getReturnType()))
                continue;
            if (desc != null && m.getName().substring(0, m.getName().length()-8).equals(desc.getDirtyPropertyName()))
                continue;
            
            try {
                ObservableValue<?> property = (ObservableValue<?>)m.invoke(obj);
                properties.add(property);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not get property " + m.getName() + " on " + obj, e);
            }                
        }
        return properties;
    }

    @Override
    public void notifyDirtyChange(boolean oldDirty, boolean dirty) {
        this.dirty.set(dirty);
    }

    @Override
    public void notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity, boolean newDirtyEntity) {
        EntityDescriptor desc = getEntityDescriptor(entity);
        if (desc.getDirtyPropertyName() != null) {
            Method m;
            try {
                m = entity.getClass().getMethod(desc.getDirtyPropertyName() + "Property");
                BooleanProperty dirty = (BooleanProperty)m.invoke(entity);
                dirty.set(newDirtyEntity);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not get dirty property on entity " + entity, e);
            }
        }
    }


    public void notifyConstraintViolations(Object entity, Set<ConstraintViolation<?>> violations) {
		if (!(entity instanceof DataNotifier))
			return;
		ConstraintViolationEvent event = new ConstraintViolationEvent(ConstraintViolationEvent.CONSTRAINT_VIOLATION, violations);
		Event.fireEvent((DataNotifier)entity, event);
    }

}
