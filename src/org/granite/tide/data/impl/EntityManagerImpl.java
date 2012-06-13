package org.granite.tide.data.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.granite.logging.Logger;
import org.granite.persistence.LazyableCollection;
import org.granite.tide.Component;
import org.granite.tide.Context;
import org.granite.tide.Expression;
import org.granite.tide.SyncMode;
import org.granite.tide.TrackingContext;
import org.granite.tide.collections.ManagedPersistentAssociation;
import org.granite.tide.collections.ManagedPersistentCollection;
import org.granite.tide.collections.ManagedPersistentMap;
import org.granite.tide.data.Conflict;
import org.granite.tide.data.DataConflictListener;
import org.granite.tide.data.DataMerger;
import org.granite.tide.data.DirtyCheckContext;
import org.granite.tide.data.EntityManager;
import org.granite.tide.data.Identifiable;
import org.granite.tide.data.Lazyable;
import org.granite.tide.data.RemoteInitializer;
import org.granite.tide.data.RemoteValidator;
import org.granite.tide.data.EntityManager.Function;
import org.granite.tide.data.EntityManager.Propagation;
import org.granite.tide.data.EntityManager.Update;
import org.granite.tide.data.EntityManager.UpdateKind;
import org.granite.tide.data.impl.UIDWeakSet.Matcher;
import org.granite.tide.data.impl.UIDWeakSet.Operation;
import org.granite.tide.data.spi.DataManager;
import org.granite.tide.data.spi.EntityRef;
import org.granite.tide.data.spi.ExpressionEvaluator;
import org.granite.tide.data.spi.MergeContext;
import org.granite.tide.data.spi.PersistenceManager;
import org.granite.tide.data.spi.DataManager.ChangeKind;
import org.granite.tide.data.spi.DataManager.TrackingHandler;
import org.granite.tide.data.spi.ExpressionEvaluator.Value;
import org.granite.tide.impl.ObjectUtil;
import org.granite.tide.server.ServerSession;
import org.granite.util.ClassUtil;
import org.granite.util.WeakIdentityHashMap;


public class EntityManagerImpl implements EntityManager {
    
    private static final Logger log = Logger.getLogger(EntityManagerImpl.class);
    
    private String id;
    private boolean active = false;
    private ExpressionEvaluator expressionEvaluator = null;
    private DataManager dataManager = null;
    private TrackingContext trackingContext = null;
    private DirtyCheckContext dirtyCheckContext = null;
    private UIDWeakSet entitiesByUid = new UIDWeakSet();
    private WeakIdentityHashMap<Object, List<Object>> entityReferences = new WeakIdentityHashMap<Object, List<Object>>();
    
    private DataMerger[] customMergers = null;
    
//    private RemoteInitializer remoteInitializer = null;
//    private RemoteValidator remoteValidator = null;
    

    public EntityManagerImpl(String id, DataManager dataManager, TrackingContext trackingContext, ExpressionEvaluator expressionEvaluator) {
        this.id = id;
        this.active = true;
        this.dataManager = dataManager != null ? dataManager : new DefaultDataManager();
        this.dataManager.setTrackingHandler(new DefaultTrackingHandler());
        this.trackingContext = trackingContext != null ? trackingContext : new TrackingContext();
        this.dirtyCheckContext = new DirtyCheckContextImpl(this.dataManager, this.trackingContext);
        // TODO
//        this.dirtyCheckContext.addEventListener(DIRTY_CHANGE, dirtyChangeHandler, false, 0, true);
        this.expressionEvaluator = expressionEvaluator;
    }
    
    
    /**
     *  Return the entity manager id
     * 
     *  @return the entity manager id
     */
    public String getId() {
        return id;
    }
    
    /**
     *  {@inheritdoc}
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     *  Clear the current context
     *  Destroys all components/context variables
     */
    public void clear() {
        // TODO: clear entities
//        for each (var e:Object in _entitiesByUID.data) {
//            if (e is IEntity)
//                Managed.setEntityManager(IEntity(e), null);
//        }
        entitiesByUid.clear();
        entityReferences.clear();
        dirtyCheckContext.clear(false);
        dataManager.clear();
        trackingContext.clear();
        active = true;
    }
    
    /**
     *  Clears entity cache
     */ 
    public void clearCache() {
       // _mergeContext.clear();
    }

    
    public DataManager getDataManager() {
    	return dataManager;
    }

    
    /**
     *  Setter for the array of custom mergers
     * 
     *  @param customMergers array of mergers
     */
    public void setCustomMergers(DataMerger[] customMergers) {
        if (customMergers != null && customMergers.length > 0)
            this.customMergers = customMergers;
        else
            this.customMergers = null;
    }


    private boolean uninitializeAllowed = true;
    
    @Override
    public void setUninitializeAllowed(boolean uninitializeAllowed) {
        this.uninitializeAllowed = uninitializeAllowed;
    }

    @Override
    public boolean isUninitializeAllowed() {
        return uninitializeAllowed;
    }


    private Propagation entityManagerPropagation = null;
        
    /**
     *  Setter for the propagation manager
     * 
     *  @param propagation propagation function that will visit child entity managers
     */
    public void setEntityManagerPropagation(Propagation propagation) {
        this.entityManagerPropagation = propagation;
    }
    
    /**
     *  Setter for active flag
     *  When EntityManager is not active, dirty checking is disabled
     * 
     *  @param active state
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     *  Setter for dirty check context implementation
     * 
     *  @param dirtyCheckContext dirty check context implementation
     */
    public void setDirtyCheckContext(DirtyCheckContext dirtyCheckContext) {
        if (dirtyCheckContext == null)
            throw new IllegalArgumentException("Dirty check context cannot be null");
        
//        if (dirtyCheckContext != null)
//            dirtyCheckContext.removeEventListener(DIRTY_CHANGE, dirtyChangeHandler);
            
        this.dirtyCheckContext = dirtyCheckContext;
        this.dirtyCheckContext.setTrackingContext(trackingContext);
//        this.dirtyCheckContext.addEventListener(DIRTY_CHANGE, dirtyChangeHandler, false, 0, true);
        
        // _mergeContext = new MergeContext(this, dirtyCheckContext);
    }

    
    private static int tmpEntityManagerId = 1;
    
    /**
     *  Create a new temporary entity manager
     */
    public EntityManager newTemporaryEntityManager() {
        return new EntityManagerImpl("$$TMP$$" + (tmpEntityManagerId++), dataManager, trackingContext, expressionEvaluator);
    }
//    
//    /**
//     *  @private
//     *  Allow uninitialize of persistent collections
//     *
//     *  @param allowed allow uninitialize of collections
//     */
//    public void setUninitializeAllowed(boolean allowed) {
//        _mergeContext.uninitializeAllowed = allowed;
//    }
//    
//    /**
//     *  @private
//     *  @return allow uninitialize of collections
//     */
//    public function get uninitializeAllowed():Boolean {
//        return _mergeContext.uninitializeAllowed;
//    }
//    
//    /**
//     *  @private
//     *  Force uninitialize of persistent collections
//     * 
//     *  @param uninitializing force uninitializing of collections during merge
//     */
//    public function set uninitializing(uninitializing:Boolean):void {
//        _mergeContext.uninitializing = uninitializing;
//    }
    
    
//    /**
//     *  Entity manager is dirty when any entity/collection/map has been modified
//     *
//     *  @return is dirty
//     */
//    [Bindable(event="dirtyChange")]
//    public function get dirty():Boolean {
//        return _dirtyCheckContext.dirty;
//    }
//    
//    /**
//     *  Internal handler for dirty flag changes. Redispaches event from the dirty check context
//     * 
//     *  @param event dirty change event
//     */
//    private function dirtyChangeHandler(event:PropertyChangeEvent):void {
//        dispatchEvent(event);
//    }
//    
//    
//    /**
//     *  List of conflicts detected during last merge operation
//     * 
//     *  @return conflicts list 
//     */
//    public Conflicts getMergeConflicts() {
//        return _mergeContext.mergeConflicts;
//    }
    
    
    /**
     *  @private
     *  Attach an entity to this context
     * 
     *  @param entity an entity
     */
    public void attachEntity(Identifiable entity) {
        attachEntity(entity, true);
    }
    
    /**
     *  @private
     *  Attach an entity to this context
     * 
     *  @param entity an entity
     *  @param putInCache put entity in cache
     */
    public void attachEntity(Identifiable entity, boolean putInCache) {
        EntityManager em = PersistenceManager.getEntityManager(entity);
        if (em != null && em != this && !em.isActive()) {
            throw new Error("The entity instance " + entity
                + " cannot be attached to two contexts (current: " + em.getId()
                + ", new: " + id + ")");
        }
        
        PersistenceManager.setEntityManager(entity, this);
        if (putInCache)
            entitiesByUid.put(entity);         
    }
    
    /**
     *  @private
     *  Detach an entity from this context only if it's not persistent
     * 
     *  @param entity an entity
     */
    public void detachEntity(Identifiable entity) {
        detachEntity(entity, true);
    }
        
    
    /**
     *  @private
     *  Detach an entity from this context only if it's not persistent
     * 
     *  @param entity an entity
     *  @param removeFromCache remove entity from cache
     */
    public void detachEntity(Identifiable entity, boolean removeFromCache) {
        dirtyCheckContext.markNotDirty(entity, entity);
        
        PersistenceManager.setEntityManager(entity, null);
        if (removeFromCache)
            entitiesByUid.remove(entity.getClass().getName() + ":" + entity.getUid());
    }
    
    
    /**
     *  {@inheritdoc}
     */
    public boolean isSaved(Identifiable entity) {
        EntityDescriptor desc = PersistenceManager.getEntityDescriptor(entity);
        if (desc.getVersionPropertyName() != null && dataManager.getProperty(entity, desc.getVersionPropertyName()) != null)
            return true;
        return false;
    }

    /**
     *  @private 
     *  Retrives an entity in the cache from its uid
     *   
     *  @param object an entity
     *  @param nullIfAbsent return null if entity not cached in context
     */
    public Object getCachedObject(Object object, boolean nullIfAbsent) {
        Object entity = null;
        if (object instanceof Identifiable) {
            entity = entitiesByUid.get(object.getClass().getName() + ":" + ((Identifiable)object).getUid());
        }
        else if (object instanceof EntityRef) {
            entity = entitiesByUid.get(((EntityRef)object).getClassName() + ":" + ((EntityRef)object).getUid());
        }

        if (entity != null)
            return entity;
        if (nullIfAbsent)
            return null;

        return object;
    }

    /** 
     *  @private 
     *  Retrives the owner entity of the provided object (collection/map/entity)
     *   
     *  @param object an entity
     */
    public Object[] getOwnerEntity(Object object) {
        List<Object> refs = entityReferences.get(object);
        if (refs == null)
            return null;
        
        for (int i = 0; i < refs.size(); i++) {
            if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0] instanceof String)
                return new Object[] { entitiesByUid.get((String)((Object[])refs.get(i))[0]), ((Object[])refs.get(i))[1] };
        }
        return null;
    }

    /**
     *  @private
     *  Retrives the owner entity of the provided object (collection/map/entity)
     *
     *  @param object an entity
     */
    public List<Object[]> getOwnerEntities(Object object) {
        List<Object> refs = entityReferences.get(object);
        if (refs == null)
            return null;

        List<Object[]> owners = new ArrayList<Object[]>();
        for (int i = 0; i < refs.size(); i++) {
            if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0] instanceof String) {
            	Object owner = entitiesByUid.get((String)((Object[])refs.get(i))[0]);
            	if (owner != null)	// May have been garbage collected
            		owners.add(new Object[] { owner, ((Object[])refs.get(i))[1] });
            }
        }
        return owners;
    }

    
    /**
     *  {@inheritdoc}
     */
    public Expression getReference(Object obj, boolean recurse, Set<Object> cache) {
        if (cache != null) {
            if (cache.contains(obj))    // We are in a graph loop, no reference can be found from this path
                return null;
            cache.add(obj);
        }
        else if (recurse)
            throw new Error("Cache must be provided to get reference recursively");
        
        List<Object> refs = entityReferences.get(obj);
        if (refs == null)
            return null;
        
        for (int i = 0; i < refs.size(); i++) {
            // Return first context expression reference that is remote enabled
            if (refs.get(i) instanceof Expression && expressionEvaluator != null && expressionEvaluator.getRemoteSync(refs.get(i)) != SyncMode.NONE)
                return (Expression)refs.get(i);
        }
        
        if (recurse) {
            Object ref = null;
            for (int i = 0; i < refs.size(); i++) {
                if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0] instanceof String) {
                    ref = entitiesByUid.get((String)((Object[])refs.get(i))[0]);
                    if (ref != null) {
                        ref = getReference(ref, recurse, cache);
                        if (ref != null)
                            return (Expression)ref;
                    }
                }
                else if (refs.get(i) instanceof Object[] && !(refs.get(i) instanceof Expression)) {
                    ref = ((Object[])refs.get(i))[0];
                    if (ref != null) {
                        ref = getReference(ref, recurse, cache);
                        if (ref != null)
                            return (Expression)ref;
                    } 
                }
            }
        }
        return null;
    }
    
    /**
     *  @private
     *  Init references array for an object
     *   
     *  @param obj an entity
     */
    private List<Object> initRefs(Object obj) {
        List<Object> refs = entityReferences.get(obj);
        if (refs == null) {
            refs = new ArrayList<Object>();
            entityReferences.put(obj, refs);
        }
        return refs;
    }
    

    /**
     *  Registers a reference to the provided object with either a parent or res
     * 
     *  @param obj an entity
     *  @param parent the parent entity
     *  @param propName name of the parent entity property that references the entity
     *  @param res the context expression
     */ 
    public void addReference(Object obj, Object parent, String propName, Expression res) {
        if (obj instanceof Identifiable)
            attachEntity((Identifiable)obj);
        
        dataManager.startTracking(obj, parent);

        if (obj instanceof ManagedPersistentAssociation)
            obj = ((ManagedPersistentAssociation)obj).getCollection();
        
        List<Object> refs = entityReferences.get(obj);
        if (!(obj instanceof LazyableCollection) && res != null) {
            refs = initRefs(obj);
            boolean found = false;
            for (int i = 0; i < refs.size(); i++) {
                if (!(refs.get(i) instanceof Expression))
                    continue; 
                Expression r = (Expression)refs.get(i);
                if (r.getComponentName().equals(res.getComponentName()) 
                        && ((r.getExpression() == null && res.getExpression() == null) || (r.getExpression() != null && r.getExpression().equals(res.getExpression())))) {
                    found = true;
                    break;
                }
            }
            if (!found)
                refs.add(res);
        }
        boolean found = false;
        if (parent instanceof Identifiable) {
            String ref = parent.getClass().getName() + ":" + ((Identifiable)parent).getUid();
            if (refs == null)
                refs = initRefs(obj);
            else {
                for (int i = 0; i < refs.size(); i++) {
                    if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0].equals(ref)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
                refs.add(new Object[] { ref, propName });
        }
        else if (parent != null) {
            if (refs == null)
                refs = initRefs(obj);
            else {
                for (int i = 0; i < refs.size(); i++) {
                    if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0].equals(parent)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
                refs.add(new Object[] { parent, propName });
        }
    }
    
    /**
     *  Removes a reference on the provided object
     *
     *  @param obj an entity
     *  @param parent the parent entity to dereference
     *  @param propName name of the parent entity property that references the entity
     *  @param res expression to remove
     */ 
    public void removeReference(Object obj, Object parent, String propName, Expression res) {
        if (obj instanceof ManagedPersistentAssociation)
            obj = ((ManagedPersistentAssociation)obj).getCollection();
        
        List<Object> refs = entityReferences.get(obj);
        if (refs == null)
            return;
        
        int idx = -1;
        if (parent instanceof Identifiable) {
            for (int i = 0; i < refs.size(); i++) {
                if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0].equals(parent.getClass().getName() + ":" + ((Identifiable)parent).getUid())) {
                    idx = i;
                    break;                    
                }
            }
        }
        else if (parent != null) {
            for (int i = 0; i < refs.size(); i++) {
                if (refs.get(i) instanceof Object[] && ((Object[])refs.get(i))[0].equals(parent)) {
                    idx = i;
                    break;                    
                }
            }
        }
        else if (res != null) {
            for (int i = 0; i < refs.size(); i++) {
                if (refs.get(i) instanceof Expression && ((Expression)refs.get(i)).getPath().equals(res.getPath())) {
                    idx = i;
                    break;
                }
            }
        }
        if (idx >= 0)
            refs.remove(idx);
        
        if (refs.size() == 0) {
            entityReferences.remove(obj);
            
            if (obj instanceof Identifiable)
                detachEntity((Identifiable)obj, true);
            
            dataManager.stopTracking(obj, parent);
        }
        
        if (obj instanceof Iterable<?>) {
            for (Object elt : (Iterable<?>)obj)
                removeReference(elt, parent, propName, null);
        }
        else if (obj != null && obj.getClass().isArray()) {
            for (int i = 0; i < Array.getLength(obj); i++)
                removeReference(Array.get(obj, i), parent, propName, null);
        }
        else if (obj instanceof Map<?, ?>) {
            for (Entry<?, ?> me : ((Map<?, ?>)obj).entrySet()) {
                removeReference(me.getKey(), parent, propName, null);
                removeReference(me.getValue(), parent, propName, null);
            }
        }
    }
    
    
    public MergeContext initMerge() {
        return new MergeContext(this, dirtyCheckContext, null);
    }

    /**
     *  Merge an object coming from the server in the context
     *
     *  @param obj external object
     *  @param previous previously existing object in the context (null if no existing object)
     *  @param expr current path from the context
     *  @param parent parent object for collections
     *  @param propertyName property name of the current object in the parent object
     *  @param setter setter function to update the private property
     *  @param forceUpdate force update of property (used for externalized properties)
     *
     *  @return merged object (should === previous when previous not null)
     */
    @SuppressWarnings("unchecked")
    public Object mergeExternal(final MergeContext mergeContext, Object obj, Object previous, Expression expr, Object parent, String propertyName, String setter, boolean forceUpdate) {

        mergeContext.initMerge();
        
        boolean saveMergeUpdate = mergeContext.isMergeUpdate();
        boolean saveMerging = mergeContext.isMerging();
        
        try {
            mergeContext.setMerging(true);
            
            boolean addRef = false;
            boolean fromCache = false;
            Object prev = mergeContext.getFromCache(obj);
            Object next = obj;
            if (prev != null) {
                next = prev;
                fromCache = true;
            }
            else {
                // Give a chance to intercept received value so we can apply changes on private values
                // TODO
//                if (_mergeContext.proxyGetter != null && parent != null && propertyName != null)
//                    next = obj = _mergeContext.proxyGetter(obj, parent, propertyName);

                // Clear change tracking
                dataManager.stopTracking(previous, parent); 
                
                if (obj == null) {
                    next = null;
                }
                else if (((obj instanceof LazyableCollection && !((LazyableCollection)obj).isInitialized()) 
                    || (obj instanceof LazyableCollection && !(previous instanceof LazyableCollection))) && parent instanceof Identifiable && propertyName != null) {
                    next = mergePersistentCollection(mergeContext, (LazyableCollection)obj, previous, null, (Identifiable)parent, propertyName);
                    addRef = true;
                }
                else if (obj instanceof List<?>) {
                    next = mergeCollection(mergeContext, (List<Object>)obj, previous, parent == null ? expr : null, parent, propertyName);
                    addRef = true;
                }
//                else if (obj != null && obj.getClass().isArray()) {
//                    next = mergeArray(mergeContext, obj, previous, parent == null ? expr : null, parent, propertyName);
//                    addRef = true;
//                }
                else if (obj instanceof Map<?, ?>) {
                    next = mergeMap(mergeContext, (Map<Object, Object>)obj, previous, parent == null ? expr : null, parent, propertyName);
                    addRef = true;
                }
                else if (obj instanceof Identifiable) {
                    next = mergeEntity(mergeContext, obj, previous, expr, parent, propertyName);
                    addRef = true;
                }
                else {
                    boolean merged = false;
                    if (customMergers != null) {
                        for (DataMerger merger : customMergers) {
                            if (merger.accepts(obj)) {
                                next = merger.merge(mergeContext, obj, previous, parent == null ? expr : null, parent, propertyName);

                                // Keep notified of collection updates to notify the server at next remote call
                                dataManager.startTracking(previous, parent);
                                merged = true;
                                addRef = true;
                            }
                        }
                    }
                    if (!merged && !ObjectUtil.isSimple(obj) && !(obj instanceof Value || obj instanceof byte[])) {
                        next = mergeEntity(mergeContext, obj, previous, expr, parent, propertyName);
                        addRef = true;
                    }
                }
            }
            
            if (next != null && !fromCache && addRef
                && (expr != null || (prev == null && parent != null))) {
                // Store reference from current object to its parent entity or root component expression
                // If it comes from the cache, we are probably in a circular graph 
                addReference(next, parent, propertyName, expr);
            }
            
            mergeContext.setMergeUpdate(saveMergeUpdate);
            
            if ((mergeContext.isMergeUpdate() || forceUpdate) && setter != null && parent != null && propertyName != null && parent instanceof Identifiable && next != previous) {
                if (!mergeContext.isResolvingConflict() || !propertyName.equals(PersistenceManager.getEntityDescriptor(parent).getVersionPropertyName())) {
                    // dataManager.setInternalProperty(parent, propertyName, next);
                    dataManager.setProperty(parent, propertyName, previous, next);
                }
            }
            
            if (entityManagerPropagation != null && (mergeContext.isMergeUpdate() || forceUpdate) && !fromCache && obj instanceof Identifiable) {
                // Propagate to existing conversation contexts where the entity is present
                entityManagerPropagation.propagate((Identifiable)obj, new Function() {
                    public void execute(EntityManager entityManager, Identifiable entity) {
                        if (entityManager == mergeContext.getSourceEntityManager())
                            return;
                        if (entityManager.getCachedObject(entity, true) != null)
                            entityManager.mergeFromEntityManager(entityManager, entity, mergeContext.getExternalDataSessionId(), mergeContext.isUninitializing());
                    }
                });
            }
            
            return next;
        }
        catch (Exception e) {
        	log.error(e, "Merge error");
        	return null;
        }
        finally {
            mergeContext.setMerging(saveMerging);
        }
    }


    /**
     *  @private 
     *  Merge an entity coming from the server in the context
     *
     *  @param obj external entity
     *  @param previous previously existing object in the context (null if no existing object)
     *  @param expr current path from the context
     *  @param parent parent object for collections
     *  @param propertyName propertyName from the owner object
     *
     *  @return merged entity (=== previous when previous not null)
     */ 
    private Object mergeEntity(MergeContext mergeContext, final Object obj, Object previous, Expression expr, Object parent, String propertyName) {
        if (obj != null || previous != null)
            log.debug("mergeEntity: %s previous %s%s", ObjectUtil.toString(obj), ObjectUtil.toString(previous), obj == previous ? " (same)" : "");
        
        Object dest = obj;
        Object p = null;
        if (obj instanceof Lazyable && !((Lazyable)obj).isInitialized()) {
            // If entity is uninitialized, try to lookup the cached instance by its class name and id (only works with Hibernate proxies)
            final EntityDescriptor desc = PersistenceManager.getEntityDescriptor(obj);
            if (desc.getIdPropertyName() != null) {
                p = entitiesByUid.find(new Matcher() {
                    public boolean match(Object o) {
                        return o.getClass().getName().equals(obj.getClass().getName()) && 
                            dataManager.getProperty(obj, desc.getIdPropertyName()).equals(dataManager.getProperty(o, desc.getIdPropertyName()));
                    }
                });

                if (p != null) {
                    previous = p;
                    dest = previous;
                }
            }
        }
        else if (obj instanceof Identifiable) {
            p = entitiesByUid.get(obj.getClass().getName() + ":" + ((Identifiable)obj).getUid());
            if (p != null) {
                // Trying to merge an entity that is already cached with itself: stop now, this is not necessary to go deeper in the object graph
                // it should be already instrumented and tracked
                if (obj == p)
                    return obj;
                
                previous = p;
                dest = previous;
            }
        }
        if (dest != previous && previous != null && (PersistenceManager.objectEquals(dataManager, previous, obj)
            || (parent != null && !(previous instanceof Identifiable))))    // GDS-649 Case of embedded objects 
            dest = previous;
        
        if (dest == obj && p == null && obj != null && mergeContext.getSourceEntityManager() != null) {
            // When merging from another entity manager, ensure we create a new copy of the entity
            // An instance can exist in only one entity manager at a time 
            try {
                dest = ClassUtil.newInstance(obj.getClass(), Object.class);
                if (obj instanceof Identifiable)
                    ((Identifiable)dest).setUid(((Identifiable)obj).getUid());
            }
            catch (Exception e) {
                throw new RuntimeException("Could not create class " + obj.getClass(), e);
            }
        }

        if (obj instanceof Lazyable && !((Lazyable)obj).isInitialized() && PersistenceManager.objectEquals(dataManager, previous, obj)) {
            EntityDescriptor desc = PersistenceManager.getEntityDescriptor(obj);
            // Don't overwrite existing entity with an uninitialized proxy when optimistic locking is defined
            if (desc.getVersionPropertyName() != null) {
                log.debug("ignored received uninitialized proxy");
                // Should we mark the object not dirty as we only received a proxy ??
                dirtyCheckContext.markNotDirty(previous, null);
                return previous;
            }
        }
        
        if (dest instanceof Lazyable && !((Lazyable)dest).isInitialized())
            log.debug("initialize lazy entity: %s", dest.toString());
        
        if (dest != null && dest instanceof Identifiable && dest == obj) {
            log.debug("received entity %s used as destination (ctx: %s)", obj.toString(), this.id);
        }
        
        boolean fromCache = (p != null && dest == p); 
        
        if (!fromCache && dest instanceof Identifiable)
            entitiesByUid.put((Identifiable)dest);            
        
        mergeContext.putInCache(obj, dest);
        
        boolean ignore = false;
        if (dest instanceof Identifiable) {
            EntityDescriptor desc = PersistenceManager.getEntityDescriptor(dest);
            
            // If we are in an uninitialing temporary entity manager, try to reproxy associations when possible
            if (mergeContext.isUninitializing() && parent instanceof Identifiable && propertyName != null) {
                if (desc.getVersionPropertyName() != null && dataManager.getProperty(obj, desc.getVersionPropertyName()) != null 
                        && PersistenceManager.getEntityDescriptor(parent).isLazy(propertyName)) {
                    if (defineProxy(desc, dest, obj))   // Only if entity can be proxied (has a detachedState)
                        return dest;
                }
            }
            
            // Associate entity with the current context
            attachEntity((Identifiable)dest, false);
            
            if (previous != null && dest == previous) {
                // Check version for optimistic locking
                if (desc.getVersionPropertyName() != null && !mergeContext.isResolvingConflict()) {
                    Number newVersion = (Number)dataManager.getProperty(obj, desc.getVersionPropertyName());
                    Number oldVersion = (Number)dataManager.getProperty(dest, desc.getVersionPropertyName());
                    if ((newVersion != null && oldVersion != null && newVersion.longValue() < oldVersion.longValue() 
                            || (newVersion == null && oldVersion != null))) {
                        log.warn("ignored merge of older version of %s (current: %d, received: %d)", 
                            dest.toString(), oldVersion, newVersion);
                        ignore = true;
                    }
                    else if ((newVersion != null && oldVersion != null && newVersion.longValue() > oldVersion.longValue()) 
                            || (newVersion != null && oldVersion == null)) {
                        // Handle changes when version number is increased
                        mergeContext.markVersionChanged(dest);
                        
                        if (mergeContext.getExternalDataSessionId() != null && dirtyCheckContext.isEntityChanged((Identifiable)dest)) {
                            // Conflict between externally received data and local modifications
                            log.error("conflict with external data detected on %s (current: %d, received: %d)",
                                dest.toString(), oldVersion, newVersion);
                            
                            // Check incoming values and local values
                            if (dirtyCheckContext.checkAndMarkNotDirty((Identifiable)dest, (Identifiable)obj)) {
                                // Incoming data is different from local data
                                mergeContext.addConflict((Identifiable)dest, (Identifiable)obj);
                                
                                ignore = true;
                            }
                            else
                                mergeContext.setMergeUpdate(true);
                        }
                        else
                            mergeContext.setMergeUpdate(true);
                    }
                    else {
                        // Data has been changed locally and not persisted, don't overwrite when version number is unchanged
                        if (dirtyCheckContext.isEntityChanged((Identifiable)dest))
                            mergeContext.setMergeUpdate(false);
                        else
                            mergeContext.setMergeUpdate(true);
                    }
                }
                else if (!mergeContext.isResolvingConflict())
                    mergeContext.markVersionChanged(dest);
            }
            else
                mergeContext.markVersionChanged(dest);
            
            if (!ignore)
                defaultMerge(mergeContext, obj, dest, expr, parent, propertyName);
        }
        else
            defaultMerge(mergeContext, obj, dest, expr, parent, propertyName);
        
        /*  GDS-863
        if (previous && obj !== previous && previous is Identifiable && _dirtyCheckContext.isSaved(previous)) {
            var pce:PropertyChangeEvent = new PropertyChangeEvent(PropertyChangeEvent.PROPERTY_CHANGE,
                false, false, PropertyChangeEventKind.UPDATE, null, previous, previous);
            previous.dispatchEvent(pce);
        }
        */

        if (dest != null && !ignore && !mergeContext.isResolvingConflict()) {
            // Force or check non-dirty state of local entity that has just been merged
            if (mergeContext.isMergeUpdate() && ((dest instanceof Identifiable && mergeContext.hasVersionChanged(dest)) 
            		|| (!(dest instanceof Identifiable) && parent instanceof Identifiable && mergeContext.hasVersionChanged(parent))))
                dirtyCheckContext.markNotDirty(dest, dest instanceof Identifiable ? null : (Identifiable)parent);
            else if (dest instanceof Identifiable && obj instanceof Identifiable)
                dirtyCheckContext.checkAndMarkNotDirty((Identifiable)dest, (Identifiable)obj);
        }
        
        if (dest != null)
            log.debug("mergeEntity result: %s", dest.toString());
        
        // Keep notified of collection updates to notify the server at next remote call
        dataManager.startTracking(dest, parent);
        
        return dest;
    }
    
    
    private boolean defineProxy(EntityDescriptor desc, Object dest, Object obj) {
        if (desc.getDetachedStateField() == null)
            return false;

        try {
            if (obj != null) {
                if (desc.getDetachedStateField().get(obj) == null)
                    return false;
                dataManager.setProperty(dest, desc.getIdPropertyName(), null, 
                        dataManager.getProperty(obj, desc.getIdPropertyName()));
                desc.getDetachedStateField().set(dest, desc.getDetachedStateField().get(obj));
            }
            desc.getInitializedField().set(dest, false);
            return true;
        }
        catch (Exception e) {
            throw new RuntimeException("Could not proxy class " + obj.getClass());
        }
    }
    

    /**
     *  @private 
     *  Merge a collection coming from the server in the context
     *
     *  @param coll external collection
     *  @param previous previously existing collection in the context (can be null if no existing collection)
     *  @param expr current path from the context
     *  @param parent owner object for collections
     *  @param propertyName property name in owner object
     * 
     *  @return merged collection (=== previous when previous not null)
     */ 
    @SuppressWarnings("unchecked")
    private List<?> mergeCollection(MergeContext mergeContext, List<Object> coll, Object previous, Expression expr, Object parent, String propertyName) {
        log.debug("mergeCollection: %s previous %s", ObjectUtil.toString(coll), ObjectUtil.toString(previous));
        
        if (mergeContext.isUninitializing() && parent instanceof Identifiable && propertyName != null && PersistenceManager.getEntityDescriptor(parent).isLazy(propertyName)) {
            if (previous instanceof LazyableCollection && ((LazyableCollection)previous).isInitialized()) {
                log.debug("uninitialize lazy collection %s", ObjectUtil.toString(previous));
                mergeContext.putInCache(coll, previous);
                ((LazyableCollection)previous).uninitialize();
                return (List<?>)previous;
            }
        }

        if (previous != null && previous instanceof LazyableCollection && !((LazyableCollection)previous).isInitialized()) {
            log.debug("initialize lazy collection %s", ObjectUtil.toString(previous));
            mergeContext.putInCache(coll, previous);
            
            ((LazyableCollection)previous).initializing();
            
            List<Object> added = new ArrayList<Object>(coll.size());
            for (int i = 0; i < coll.size(); i++) {
                Object obj = coll.get(i);

                obj = mergeExternal(mergeContext, obj, null, null, propertyName != null ? parent : null, propertyName, null, false);
                added.add(obj);
            }
            
            ((LazyableCollection)previous).initialize();
            ((Collection<Object>)previous).addAll(added);

            // Keep notified of collection updates to notify the server at next remote call
            dataManager.startTracking(previous, parent);

            return (List<?>)previous;
        }

        boolean tracking = false;
        
        List<?> nextList = null;
        List<Object> list = null;
        if (previous != null && previous instanceof List<?>)
            list = (List<Object>)previous;
        else if (mergeContext.getSourceEntityManager() != null) {
            try {
                list = coll.getClass().newInstance();
            }
            catch (Exception e) {
                throw new RuntimeException("Could not create class " + coll.getClass());
            }
        }
        else
            list = (List<Object>)coll;
                        
        mergeContext.putInCache(coll, list);

        List<Object> prevColl = list != coll ? list : null;
        List<Object> destColl = prevColl;
        // TODO ??
//      // Restore collection sort/filter state
//        if (destColl is ListCollectionView && (ListCollectionView(destColl).sort != null || ListCollectionView(destColl).filterFunction != null))
//            destColl = ListCollectionView(destColl).list;
//        else if (destColl is ICollectionView && coll is ICollectionView) {
//            ICollectionView(coll).sort = ICollectionView(destColl).sort;
//            ICollectionView(coll).filterFunction = ICollectionView(destColl).filterFunction;
//            ICollectionView(coll).refresh();
//        }

        if (prevColl != null && mergeContext.isMergeUpdate()) {
            // Enable tracking before modifying collection when resolving a conflict
            // so the dirty checking can save changes
            if (mergeContext.isResolvingConflict()) {
                dataManager.startTracking(prevColl, parent);
                tracking = true;
            }
            
            for (int i = 0; i < destColl.size(); i++) {
                Object obj = destColl.get(i);
                boolean found = false;
                for (int j = 0; j < coll.size(); j++) {
                    Object next = coll.get(j);
                    if (PersistenceManager.objectEquals(dataManager, next, obj)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    destColl.remove(i);
                    i--;
                }
            }
        }
        for (int i = 0; i < coll.size(); i++) {
            Object obj = coll.get(i);
            if (destColl != null) {
                boolean found = false;
                for (int j = i; j < destColl.size(); j++) {
                    Object prev = destColl.get(j);
                    if (i < destColl.size() && PersistenceManager.objectEquals(dataManager, prev, obj)) {
                        obj = mergeExternal(mergeContext, obj, prev, propertyName != null ? expr : null, propertyName != null ? parent : null, propertyName, null, false);
                        
                        if (j != i) {
                            destColl.remove(j);
                            if (i < destColl.size())
                                destColl.add(i, obj);
                            else
                                destColl.add(obj);
                            if (i > j)
                                j--;
                        }
                        else if (obj != prev)
                            destColl.set(i, obj);
                        
                        found = true;
                    }
                }
                if (!found) {
                    obj = mergeExternal(mergeContext, obj, null, propertyName != null ? expr : null, propertyName != null ? parent : null, propertyName, null, false);
                    
                    if (mergeContext.isMergeUpdate()) {
                        if (i < prevColl.size())
                            destColl.add(i, obj);
                        else
                            destColl.add(obj);
                    }
                }
            }
            else {
                Object prev = obj;
                obj = mergeExternal(mergeContext, obj, null, propertyName != null ? expr : null, propertyName != null ? parent : null, propertyName, null, false);
                if (obj != prev)
                    coll.set(i, obj);
            }
        }
        if (destColl != null && mergeContext.isMergeUpdate()) {
            if (!mergeContext.isResolvingConflict())
                dirtyCheckContext.markNotDirty(previous, (Identifiable)parent);
            
            nextList = prevColl;
        }
        else
            nextList = coll;
        
        // Wrap persistent collections
        if (parent instanceof Identifiable && propertyName != null && nextList instanceof LazyableCollection && !(nextList instanceof ManagedPersistentCollection)) {
            log.debug("create initialized persistent collection from %s", ObjectUtil.toString(nextList));
            
            nextList = dataManager.newPersistentCollection((Identifiable)parent, propertyName, (LazyableCollection)nextList);
        }
        else
            log.debug("mergeCollection result: %s", ObjectUtil.toString(nextList));
        
        mergeContext.putInCache(coll, nextList);
        
        if (!tracking)
            dataManager.startTracking(nextList, parent);

        return nextList;
    }
    
//    /**
//     *  @private 
//     *  Merge an array coming from the server in the context
//     *
//     *  @param array external collection
//     *  @param previous previously existing array in the context (can be null if no existing array)
//     *  @param expr current path from the context
//     *  @param parent owner objects
//     *  @param propertyName property name in owner object
//     * 
//     *  @return merged array
//     */ 
//    private Object mergeArray(MergeContext mergeContext, Object array, Object previous, Expression expr, Object parent, String propertyName) {
//        log.debug("mergeArray: %s previous %s", ObjectUtil.toString(array), ObjectUtil.toString(previous));
//        
//        Object prevArray = previous.getClass().isArray() && Array.getLength(array) == Array.getLength(previous) && mergeContext.getSourceEntityManager() == null 
//            ? previous : Array.newInstance(previous.getClass().getComponentType(), Array.getLength(array));
////        if (Array.getLength(prevArray) > 0 && prevArray !== array)
////            Arrays.prevArray.splice(0, prevArray.length);
//        mergeContext.putInCache(array, prevArray);
//        
//        for (int i = 0; i < Array.getLength(array); i++) {
//            Object obj = Array.get(array, i);
//            obj = mergeExternal(mergeContext, obj, null, propertyName != null ? expr : null, propertyName != null ? parent : null, propertyName, null, false);
//            
//            if (mergeContext.isMergeUpdate())
//                Array.set(prevArray, i, obj);
//        }
//        
//        log.debug("mergeArray result: %s", ObjectUtil.toString(prevArray));
//        
//        return prevArray;
//    }

    /**
     *  @private 
     *  Merge a map coming from the server in the context
     *
     *  @param map external map
     *  @param previous previously existing map in the context (null if no existing map)
     *  @param expr current path from the context
     *  @param parent owner object for the map if applicable
     * 
     *  @return merged map (=== previous when previous not null)
     */ 
    @SuppressWarnings("unchecked")
    private Map<?, ?> mergeMap(MergeContext mergeContext, Map<Object, Object> map, Object previous, Expression expr, Object parent, String propertyName) {
        log.debug("mergeMap: %s previous %s", ObjectUtil.toString(map), ObjectUtil.toString(previous));
        
        if (mergeContext.isUninitializing() && parent instanceof Identifiable && propertyName != null && PersistenceManager.getEntityDescriptor(parent).isLazy(propertyName)) {
            if (previous instanceof LazyableCollection && ((LazyableCollection)previous).isInitialized()) {
                log.debug("uninitialize lazy map %s", ObjectUtil.toString(previous));
                mergeContext.putInCache(map, previous);
                ((LazyableCollection)previous).uninitialize();
                return (Map<?, ?>)previous;
            }
        }

        if (previous != null && previous instanceof LazyableCollection && !((LazyableCollection)previous).isInitialized()) {
            log.debug("initialize lazy map %s", ObjectUtil.toString(previous));
            mergeContext.putInCache(map, previous);
            
            ((LazyableCollection)previous).initializing();
            
            for (Entry<?, ?> me : map.entrySet()) {
                Object key = mergeExternal(mergeContext, me.getKey(), null, null, propertyName != null ? parent: null, propertyName, null, false);
                Object value = mergeExternal(mergeContext, me.getValue(), null, null, propertyName != null ? parent : null, propertyName, null, false);
                ((Map<Object, Object>)previous).put(key, value);
            }
            
            ((LazyableCollection)previous).initialize();

            // Keep notified of collection updates to notify the server at next remote call
            dataManager.startTracking(previous, parent);

            return (Map<?, ?>)previous;
        }
        
        boolean tracking = false;
        
        Map<Object, Object> nextMap = null;
        Map<Object, Object> m = null;
        if (previous != null && previous instanceof Map<?, ?>)
            m = (Map<Object, Object>)previous;
        else if (mergeContext.getSourceEntityManager() != null) {
            try {
                m = (Map<Object, Object>)ClassUtil.newInstance(map.getClass(), Map.class);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not create class " + map.getClass());
            }
        }
        else
            m = map;
        mergeContext.putInCache(map, m);
        
        Map<Object, Object> prevMap = m != map ? m : null;
        
        if (prevMap != null) {
            if (mergeContext.isResolvingConflict()) {
                dataManager.startTracking(prevMap, parent);
                tracking = true;
            }
            
            if (map != prevMap) {
                for (Entry<?, ?> me : map.entrySet()) {
                    Object newKey = mergeExternal(mergeContext, me.getKey(), null, null, parent, propertyName, null, false);
                    Object prevValue = prevMap.get(newKey);
                    Object value = mergeExternal(mergeContext, me.getValue(), prevValue, null, parent, propertyName, null, false);
                    if (mergeContext.isMergeUpdate() || prevMap.containsKey(newKey))
                        prevMap.put(newKey, value);
                }
                
                if (mergeContext.isMergeUpdate()) {
                    Iterator<Object> imap = prevMap.keySet().iterator();
                    while (imap.hasNext()) {
                        Object key = imap.next();
                        boolean found = false;
                        for (Object k : map.keySet()) {
                            if (PersistenceManager.objectEquals(dataManager, k, key)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                            imap.remove();
                    }
                }
            }
            
            if (mergeContext.isMergeUpdate() && !mergeContext.isResolvingConflict())
                dirtyCheckContext.markNotDirty(previous, (Identifiable)parent);
            
            nextMap = prevMap;
        }
        else {
            List<Object[]> addedToMap = new ArrayList<Object[]>();
            for (Entry<?, ?> me : map.entrySet()) {
                Object value = mergeExternal(mergeContext, me.getValue(), null, null, parent, propertyName, null, false);
                Object key = mergeExternal(mergeContext, me.getKey(), null, null, parent, propertyName, null, false);
                addedToMap.add(new Object[] { key, value });
            }
            map.clear();
            for (Object[] obj : addedToMap)
                map.put(obj[0], obj[1]);
            
            nextMap = map;
        }
            
        if (parent instanceof Identifiable && propertyName != null && nextMap instanceof LazyableCollection && !(nextMap instanceof ManagedPersistentMap)) {
            log.debug("create initialized persistent map from %s", ObjectUtil.toString(nextMap));
            nextMap = dataManager.newPersistentMap((Identifiable)parent, propertyName, (LazyableCollection)nextMap);
        }
        else
            log.debug("mergeMap result: %s", ObjectUtil.toString(nextMap));
        
        if (!tracking)
            dataManager.startTracking(nextMap, parent);
        
        return nextMap;
    } 


    /**
     *  @private 
     *  Wraps a persistent collection to manage lazy initialization
     *
     *  @param coll the collection to wrap
     *  @param previous the previous existing collection
     *  @param expr the path expression from the context
     *  @param parent the owner object
     *  @param propertyName owner property
     * 
     *  @return the wrapped persistent collection
     */ 
    protected Object mergePersistentCollection(MergeContext mergeContext, LazyableCollection coll, Object previous, Expression expr, Identifiable parent, String propertyName) {
        if (previous instanceof ManagedPersistentCollection<?>) {
            mergeContext.putInCache(coll, previous);
            if (((LazyableCollection)previous).isInitialized()) {
                if (mergeContext.isUninitializeAllowed() && mergeContext.hasVersionChanged(parent)) {
                    log.debug("uninitialize lazy collection %s", ObjectUtil.toString(previous));
                    ((LazyableCollection)previous).uninitialize();
                }
                else
                    log.debug("keep initialized collection %s", ObjectUtil.toString(previous));
            }
            dataManager.startTracking(previous, parent);
            return previous;
        }
        else if (previous instanceof ManagedPersistentMap<?, ?>) {
            mergeContext.putInCache(coll, previous);
            if (((LazyableCollection)previous).isInitialized()) {
                if (mergeContext.isUninitializeAllowed() && mergeContext.hasVersionChanged(parent)) {
                    log.debug("uninitialize lazy map %s", ObjectUtil.toString(previous));
                    ((LazyableCollection)previous).uninitialize();
                }
                else
                    log.debug("keep initialized map %s", ObjectUtil.toString(previous));
            }
            dataManager.startTracking(previous, parent);
            return previous;
        }
        
        if (coll instanceof Map<?, ?>) {
            ManagedPersistentMap<Object, Object> pmap = dataManager.newPersistentMap(parent, propertyName, 
                (coll instanceof ManagedPersistentMap<?, ?>
                        ? ((ManagedPersistentMap<?, ?>)coll).getCollection().clone(mergeContext.isUninitializing()) 
                        : ((LazyableCollection)coll)));
            pmap.setServerSession(mergeContext.getServerSession());
            mergeContext.putInCache(coll, pmap);
            if (pmap.isInitialized()) {
                List<Object> keys = new ArrayList<Object>(pmap.keySet());
                for (Object key : keys) {
                    Object value = pmap.remove(key);
                    key = mergeExternal(mergeContext, key, null, null, parent, propertyName, null, false);
                    value = mergeExternal(mergeContext, value, null, null, parent, propertyName, null, false);
                    pmap.put(key, value);
                }
                dataManager.startTracking(pmap, parent);
            }
            else if (parent instanceof Identifiable && propertyName != null)
                PersistenceManager.getEntityDescriptor(parent).setLazy(propertyName);
            return pmap;
        }
        
        ManagedPersistentCollection<Object> pcoll = dataManager.newPersistentCollection(parent, propertyName,
            (coll instanceof ManagedPersistentCollection<?>
                    ? ((ManagedPersistentCollection<?>)coll).getCollection().clone(mergeContext.isUninitializing()) 
                    : (LazyableCollection)coll));
        pcoll.setServerSession(mergeContext.getServerSession());
        mergeContext.putInCache(coll, pcoll);
        if (pcoll.isInitialized()) {
            for (int i = 0; i < pcoll.size(); i++) {
                Object obj = mergeExternal(mergeContext, pcoll.get(i), null, null, parent, propertyName, null, false);
                if (obj != pcoll.get(i)) 
                    pcoll.set(i, obj);
            }
            dataManager.startTracking(pcoll, parent);
        }
        else if (parent instanceof Identifiable && propertyName != null)
            PersistenceManager.getEntityDescriptor(parent).setLazy(propertyName);
        return pcoll;
    }
    
    
    /**
     *  @private 
     *  Merge an object coming from another entity manager (in general in the global context) in the local context
     *
     *  @param sourceEntityManager source context of incoming data
     *  @param obj external object
     *  @param externalDataSessionId is merge from external data
     *  @param uninitializing true to force folding of loaded lazy associations
     *
     *  @return merged object
     */
    public Object mergeFromEntityManager(EntityManager sourceEntityManager, Object obj, String externalDataSessionId, boolean uninitializing) {
        try {
            MergeContext mergeContext = new MergeContext(this, dirtyCheckContext, null);
            mergeContext.setSourceEntityManager(sourceEntityManager);
            mergeContext.setUninitializing(uninitializing);
            mergeContext.setExternalDataSessionId(externalDataSessionId);        
            
            Object next = externalDataSessionId != null
                ? internalMergeExternalData(mergeContext, obj, null, null) // Force handling of external data
                : mergeExternal(mergeContext, obj, null, null, null, null, null, false);
            
            return next;
        }
        finally {
            MergeContext.destroy(this);
        }
    }
    
    
    /**
     *  @private 
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *  @param obj external object
     *
     *  @return merged object (should === previous when previous not null)
     */

    public Object mergeExternalData(Object obj) {
        return mergeExternalData(null, obj, null, null, null);
    }
    
    public Object mergeExternalData(ServerSession serverSession, Object obj) {
        return mergeExternalData(serverSession, obj, null, null, null);
    }

    public Object mergeExternalData(Object obj, Object prev, String externalDataSessionId, List<Object> removals) {
    	return mergeExternalData(null, obj, prev, externalDataSessionId, removals);
    }
    
    /**
     *  @private 
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *  @param obj external object
     *  @param prev existing local object to merge with
     *  @param externalDataSessionId sessionId from which the data is coming (other user/server), null if local or current user session
     *  @param removals array of entities to remove from the entity manager cache
     *
     *  @return merged object (should === previous when previous not null)
     */
    public Object mergeExternalData(ServerSession serverSession, Object obj, Object prev, String externalDataSessionId, List<Object> removals) {
        try {
            MergeContext mergeContext = new MergeContext(this, dirtyCheckContext, null);
            mergeContext.setServerSession(serverSession);
            mergeContext.setExternalDataSessionId(externalDataSessionId);
            
            return internalMergeExternalData(mergeContext, obj, prev, removals);
        }
        finally {
            MergeContext.destroy(this);
        }
    }
    
    /**
     *  @private 
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *  @param obj external object
     *  @param prev existing local object to merge with
     *  @param externalDataSessionId sessionId from which the data is coming (other user/server), null if local or current user session
     *  @param removals array of entities to remove from the entity manager cache
     *
     *  @return merged object (should === previous when previous not null)
     */
    public Object internalMergeExternalData(MergeContext mergeContext, Object obj, Object prev, List<Object> removals) {
        Map<String, Object> savedContext = null;
        
        try {
            if (mergeContext.getExternalDataSessionId() != null)
                savedContext = trackingContext.saveAndResetContext();
            
            Object next = mergeExternal(mergeContext, obj, prev, null, null, null, null, false);
            
            if (removals != null)
                handleRemovals(mergeContext, removals);
            
            if (mergeContext.getExternalDataSessionId() != null) {
                handleMergeConflicts(mergeContext);         
                clearCache();
            }
            
            return next;
        }
        finally {               
            if (mergeContext.getExternalDataSessionId() != null)
                trackingContext.restoreContext(savedContext);
        }           
    }
    
    
    /**
     *  Merge conversation entity manager context variables in global entity manager 
     *  Only applicable to conversation contexts 
     * 
     *  @param entityManager conversation entity manager
     */
    public void mergeInEntityManager(final EntityManager entityManager) {
        final Set<Object> cache = new HashSet<Object>();
        final EntityManager sourceEntityManager = this;
        entitiesByUid.apply(new UIDWeakSet.Operation() {
            public boolean apply(Object obj) {
                // Reset local dirty state, only server state can safely be merged in global context
                if (obj instanceof Identifiable)
                    resetEntity((Identifiable)obj, cache);
                entityManager.mergeFromEntityManager(sourceEntityManager, obj, null, false);
                return true;
            }
        });
    }


    @Override
    public boolean isDirty() {
        return dataManager.isDirty();
    }

    @Override
    public boolean isSaved(Object entity) {
        return dirtyCheckContext.getSavedProperties(entity) != null;
    }
    

    /**
     *  Remove elements from cache and managed collections
     *
     *  @param removals array of entity instances to remove from the entity manager cache
     */
    public void handleRemovals(MergeContext mergeContext, List<Object> removals) {
        for (Object removal : removals) {
            Object entity = getCachedObject(removal, true);
            if (entity == null) // Not found in local cache, cannot remove
                continue;

            if (mergeContext.getExternalDataSessionId() != null && !mergeContext.isResolvingConflict() 
                    && dirtyCheckContext.isEntityChanged(entity)) {
                // Conflict between externally received data and local modifications
                log.error("conflict with external data removal detected on %s", ObjectUtil.toString(entity));

                mergeContext.addConflict((Identifiable)entity, null);
            }
            else {
                List<Object[]> owners = getOwnerEntities(entity);
                if (owners != null) {
                    for (Object[] owner : owners) {
                        Object val = dataManager.getProperty(owner[0], (String)owner[1]);
                        if (val instanceof LazyableCollection && !((LazyableCollection)val).isInitialized())
                            continue;
                        if (val instanceof List<?>) {
                            int idx = ((List<?>)val).indexOf(entity);
                            if (idx >= 0)
                                ((List<?>)val).remove(idx);
                        }
                        // TODO
//                        else if (val != null && val.getClass().isArray()) {
//                            int idx = Arrays.val.indexOf(entity);
//                            if (idx >= 0)
//                                val.splice(idx, 1);
//                        }
                        else if (val instanceof Map<?, ?>) {
                            Map<?, ?> map = (Map<?, ?>)val;
                            if (map.containsKey(entity))
                                map.remove(entity);

                            for (Iterator<?> ikey = map.keySet().iterator(); ikey.hasNext(); ) {
                                Object key = ikey.next();
                                if (PersistenceManager.objectEquals(dataManager, map.get(key), entity))
                                    ikey.remove();
                            }
                        }
                    }
                }

                /* May not be necessary, should be cleaned up by weak reference */
                Map<String, Object> pvalues = dataManager.getPropertyValues(entity, false, true);
                for (Object val : pvalues.values()) {
                    if (val instanceof List<?> || val instanceof Map<?, ?> || (val != null && val.getClass().isArray()))
                        entityReferences.remove(val);
                }

                detachEntity((Identifiable)entity);
            }
        }
    }
    
    
    private List<DataConflictListener> dataConflictListeners = new ArrayList<DataConflictListener>();
    
    public void addListener(DataConflictListener listener) {
        dataConflictListeners.add(listener);
    }
    
    public void removeListener(DataConflictListener listener) {
        dataConflictListeners.remove(listener);
    }

    /**
     *  Dispatch an event when last merge generated conflicts 
     */
    public void handleMergeConflicts(MergeContext mergeContext) {
        // Clear thread cache so acceptClient/acceptServer can work inside the conflicts handler
        mergeContext.clearCache();
        mergeContext.initMergeConflicts();

        for (DataConflictListener listener : dataConflictListeners)
            listener.onConflict(this, mergeContext.getMergeConflicts());
    }
    
    /**
     *  Resolve merge conflicts
     * 
     *  @param modifiedEntity the received entity
     *  @param localEntity the locally cached entity
     *  @param resolving true to keep client state
     */
    public void resolveMergeConflicts(MergeContext mergeContext, Object modifiedEntity, Object localEntity, boolean resolving) {
        try {
            mergeContext.setResolvingConflict(resolving);
            
            if (modifiedEntity == null)
                handleRemovals(mergeContext, Collections.singletonList(localEntity));
            else
                mergeExternal(mergeContext, modifiedEntity, localEntity, null, null, null, null, false);
    
            mergeContext.checkConflictsResolved();
        }
        finally {
            mergeContext.setResolvingConflict(false);
        }
    }
    
    
//    /**
//     *  Enables or disabled dirty checking in this context
//     *  
//     *  @param enabled
//     */
//    public void setDirtyCheckEnabled(boolean enabled) {
//        _mergeContext.merging = !enabled;
//    }
    
    
    /**
     *  {@inheritDoc}
     */
    public Map<String, Object> getSavedProperties(Object entity) {
        Object localEntity = getCachedObject(entity, true);
        if (localEntity == null)
            return null;
        return dirtyCheckContext.getSavedProperties(localEntity);
    }
    
    
    /**
     *  Default implementation of entity merge for simple ActionScript beans with public properties
     *  Can be used to implement Tide managed entities with simple objects
     *
     *  @param em the context
     *  @param obj source object
     *  @param dest destination object
     *  @param expr current path of the entity in the context (mostly for internal use)
     *  @param parent owning object
     *  @param propertyName property name of the owning object
     */ 
    public void defaultMerge(MergeContext mergeContext, Object obj, Object dest, Expression expr, Object parent, String propertyName) {
        // Merge internal state
        try {
            EntityDescriptor desc = PersistenceManager.getEntityDescriptor(obj);
            if (desc.getInitializedField() != null)
                desc.getInitializedField().set(dest, desc.getInitializedField().get(obj));
            if (desc.getDetachedStateField() != null)
                desc.getDetachedStateField().set(dest, desc.getDetachedStateField().get(obj));
        }
        catch (Exception e) {
            log.error(e, "Could not merge internal state of object " + ObjectUtil.toString(obj));
        }
        
        Map<String, Object> pval = dataManager.getPropertyValues(obj, false, false);
        List<String> rw = new ArrayList<String>();
        
        boolean isEmbedded = parent instanceof Identifiable && !(obj instanceof Identifiable);
        for (Entry<String, Object> mval : pval.entrySet()) {
            String propName = mval.getKey();
            Object o = mval.getValue();
            Object d = dataManager.getProperty(dest, propName);
            o = mergeExternal(mergeContext, o, d, expr, isEmbedded ? parent : dest, isEmbedded ? propertyName + "." + propName : propName, propName, false);
            if (o != d && mergeContext.isMergeUpdate())
                dataManager.setInternalProperty(dest, propName, o);
            rw.add(propName);
        }
        
        pval = dataManager.getPropertyValues(obj, rw, true, false);
        for (Entry<String, Object> mval : pval.entrySet()) {
            String propName = mval.getKey();
            Object o = mval.getValue();
            Object d = dataManager.getProperty(dest, propName);
            if (o instanceof Identifiable || d instanceof Identifiable)
                throw new IllegalStateException("Cannot merge the read-only property " + propName + " on bean " + obj + " with an Identifiable value, this will break local unicity and caching. Change property access to read-write.");  
            
            mergeExternal(mergeContext, o, d, expr, parent != null ? parent : dest, propertyName != null ? propertyName + '.' + propName : propName, null, false);
        }
    }
    
    
    /**
     *  Discard changes of entity from last version received from the server
     *
     *  @param entity entity to restore
     */ 
    public void resetEntity(Identifiable entity) {
        Set<Object> cache = new HashSet<Object>();
        resetEntity(entity, cache);
    }

    private void resetEntity(Identifiable entity, Set<Object> cache) {
        try {
            MergeContext mergeContext = new MergeContext(this, dirtyCheckContext, null);
            // Disable dirty check during reset of entity
            mergeContext.setMerging(true);
            dirtyCheckContext.resetEntity(mergeContext, entity, entity, cache);
        }
        finally {
            MergeContext.destroy(this);
        }
    }

    /**
     *  Discard changes of all cached entities from last version received from the server
     * 
     *  @param cache reset cache
     */ 
    public void resetAllEntities() {
        try {
            Set<Object> cache = new HashSet<Object>();
            
            MergeContext mergeContext = new MergeContext(this, dirtyCheckContext, null);
            // Disable dirty check during reset of entity
            mergeContext.setMerging(true);
            dirtyCheckContext.resetAllEntities(mergeContext, cache);
        }
        finally {
            MergeContext.destroy(this);
        }
    }
    
    /**
     *  {@inheritdoc}
     */ 
    public void acceptConflict(Conflict conflict, boolean client) {
        boolean saveTracking = trackingContext.isEnabled();
        try {
            trackingContext.setEnabled(false);
            
            Object modifiedEntity = null;
            if (client) {
                // Copy the local entity to save local changes
                modifiedEntity = ObjectUtil.copy(conflict.getLocalEntity());
            }
            else
                modifiedEntity = conflict.getReceivedEntity();
            
            try {
                MergeContext mergeContext = new MergeContext(this, dirtyCheckContext, null);
                
                // Reset the local entity to its last stable state
                resetEntity(conflict.getLocalEntity());
                
                if (client) {
                    // Merge with the incoming entity (to update version, id and all)
                    if (conflict.getReceivedEntity() != null)
                        mergeExternal(mergeContext, conflict.getReceivedEntity(), conflict.getLocalEntity(), null, null, null, null, false);
                }
                
                // Finally reapply local changes on merged received result
                resolveMergeConflicts(mergeContext, modifiedEntity, conflict.getLocalEntity(), client);
            }
            finally {
                MergeContext.destroy(this);
            }
        }
        finally {
            trackingContext.setEnabled(saveTracking);
        }
    }
    
    
    private RemoteInitializer remoteInitializer = null;
    
    @Override
    public void setRemoteInitializer(RemoteInitializer remoteInitializer) {
    	this.remoteInitializer = remoteInitializer;
    }
    
    /**
     *  {@inheritdoc}
     */
    public boolean initializeObject(ServerSession serverSession, Object object) {
        boolean initialize = false;
        if (remoteInitializer != null) {
            boolean saveTracking = trackingContext.isEnabled();
            try {
                trackingContext.setEnabled(false);
                initialize = remoteInitializer.initializeObject(serverSession, object);
            }
            finally {
                trackingContext.setEnabled(saveTracking);
            }
        }
        return initialize;
    }
//    
//    /**
//     *  {@inheritdoc}
//     */
//    public boolean validateObject(Object object, String property, Object value) {
//        boolean validate = false;
//        if (remoteValidator != null) {
//            boolean saveTracking = trackingContext.isEnabled();
//            try {
//                trackingContext.setEnabled(false);
//                validate = remoteValidator.validateObject(object, property, value);
//            }
//            finally {
//                trackingContext.setEnabled(saveTracking);
//            }
//        }
//        return validate;
//    }

    /**
     *  @private 
     *  Interceptor for managed entity setters
     *
     *  @param entity entity to intercept
     *  @param propName property name
     *  @param oldValue old value
     *  @param newValue new value
     */ 
    public void setEntityProperty(Identifiable entity, String propName, Object oldValue, Object newValue) {
        if (newValue != oldValue) {
            if (oldValue != null) {
                removeReference(oldValue, entity, propName, null);
                dataManager.stopTracking(oldValue, entity);
            }
            
            if (newValue instanceof Identifiable || newValue instanceof List<?> || newValue instanceof Map<?, ?>) {
                addReference(newValue, entity, propName, null);
                dataManager.startTracking(newValue, entity);
            }
        }
        
        MergeContext mergeContext = MergeContext.get(PersistenceManager.getEntityManager(entity));
        if (mergeContext == null)
            return;
        
        if (!mergeContext.isMerging() || mergeContext.isResolvingConflict())
            dirtyCheckContext.entityPropertyChangeHandler(entity, entity, propName, oldValue, newValue);
        
        addUpdates(entity);
    }


    /**
     *  @private 
     *  Interceptor for managed entity getters
     *
     *  @param entity entity to intercept
     *  @param propName property name
     *  @param value value
     * 
     *  @return value
     */ 
    public Object getEntityProperty(Identifiable entity, String propName, Object value) {
        if (value instanceof Identifiable || value instanceof List<?> || value instanceof Map<?, ?> || value instanceof ManagedPersistentAssociation)
            addResults(entity, propName);
        
        EntityDescriptor desc = PersistenceManager.getEntityDescriptor(entity);
        if (desc != null && propName.equals(desc.getDirtyPropertyName()))
            return dirtyCheckContext.isEntityChanged(entity);
        
        return value;
    }

    
    public class DefaultTrackingHandler implements DataManager.TrackingHandler {
        
        /**
         *  @private 
         *  Property event handler to save changes on embedded objects
         *
         *  @param event collection event
         */ 
        public void entityPropertyChangeHandler(Object target, String property, Object oldValue, Object newValue) {
            MergeContext mergeContext = MergeContext.get(PersistenceManager.getEntityManager(target));
            if ((mergeContext != null && mergeContext.getSourceEntityManager() == this) || !isActive())
                return;
            
            if (newValue != oldValue) {
                if (oldValue instanceof Identifiable || oldValue instanceof List<?> || oldValue instanceof Map<?, ?>) {
                    removeReference(oldValue, target, property, null);
                    dataManager.stopTracking(oldValue, target);
                }
                
                if (newValue instanceof Identifiable || newValue instanceof List<?> || newValue instanceof Map<?, ?>) {
                    addReference(newValue, target, property, null);
                    dataManager.startTracking(newValue, target);
                }
            }
            
            log.debug("property changed: %s %s", ObjectUtil.toString(target));
            
            if (mergeContext == null || !mergeContext.isMerging() || mergeContext.isResolvingConflict()) {
                Object owner = target instanceof Identifiable ? null : getOwnerEntity(target);
                if (owner == null)
                    dirtyCheckContext.entityPropertyChangeHandler(target, target, property, oldValue, newValue);
                else if (owner instanceof Object[] && ((Object[])owner)[0] instanceof Identifiable)
                    dirtyCheckContext.entityPropertyChangeHandler(((Object[])owner)[0], target, property, oldValue, newValue);
            }
            
            // TODO
    //        PropertyChangeEvent pce = new PropertyChangeEvent("entityEmbeddedChange", event.property, event.oldValue, event.newValue, event.source);
    //        dispatchEvent(pce);
        }
        
        /**
         *  @private 
         *  Collection event handler to save changes on collections
         *
         *  @param event collection event
         */ 
        public void collectionChangeHandler(ChangeKind kind, Object target, int location, Object[] items) {
            MergeContext mergeContext = MergeContext.get(PersistenceManager.getEntityManager(target));
            if ((mergeContext != null && mergeContext.getSourceEntityManager() == this) || !isActive())
                return;
            
            if (target instanceof Component)
                return;
            
            if (kind == ChangeKind.ADD || kind == ChangeKind.REMOVE || kind == ChangeKind.REPLACE)
                addUpdates(target);
        }
        
        /**
         *  @private 
         *  Collection event handler to save changes on managed collections
         *
         *  @param event collection event
         */ 
        public void entityCollectionChangeHandler(ChangeKind kind, Object target, int location, Object[] items) {
            MergeContext mergeContext = MergeContext.get(PersistenceManager.getEntityManager(target));
            if ((mergeContext != null && mergeContext.getSourceEntityManager() == this) || !isActive())
                return;
            
            int i = 0;
            
            Object[] parent = null;
            if (kind == ChangeKind.ADD && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                for (i = 0; i < items.length; i++) {
                    if (items[i] instanceof Identifiable) {
                        if (parent != null)
                            addReference((Identifiable)items[i], parent[0], (String)parent[1], null);
                        else
                            attachEntity((Identifiable)items[i]);
                        dataManager.startTracking((Identifiable)items[i], parent != null ? parent[0] : null);
                    }
                }
            }
            else if (kind == ChangeKind.REMOVE && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                if (parent != null) {
                    for (i = 0; i < items.length; i++) {
                        if (items[i] instanceof Identifiable)
                            removeReference((Identifiable)items[i], parent[0], (String)parent[1], null);
                    }
                }
            }
            else if (kind == ChangeKind.REPLACE && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                for (i = 0; i < items.length; i++) {
                    Object newValue = ((Object[])items[i])[1];
                    if (newValue instanceof Identifiable) {
                        if (parent != null)
                            addReference((Identifiable)newValue, parent[0], (String)parent[1], null);
                        else
                            attachEntity((Identifiable)newValue);
                        dataManager.startTracking((Identifiable)newValue, parent != null ? parent[0] : null);
                    }
                }
            }
            
            if (!(kind == ChangeKind.ADD || kind == ChangeKind.REMOVE || kind == ChangeKind.REPLACE))
                return;
            
            log.debug("collection changed: %s %s", kind, ObjectUtil.toString(target));
            
            if (mergeContext == null || !mergeContext.isMerging() || mergeContext.isResolvingConflict()) {
                if (parent == null)
                    log.warn("Owner entity not found for collection %s, cannot process dirty checking", ObjectUtil.toString(target));
                else
                    dirtyCheckContext.entityCollectionChangeHandler(parent[0], (String)parent[1], kind, location, items);
            }
            
            if (items != null && items.length > 0 && items[0] instanceof Identifiable)
                addUpdates(target);
            else if (kind == ChangeKind.UPDATE && items != null && items.length > 0) {
                PropertyChange pc = (PropertyChange)items[0];
                if (pc.getObject() instanceof Identifiable)
                    addUpdates(target);
            }
        }
        
        /**
         *  @private 
         *  Map event handler to save changes on maps
         *
         *  @param event map event
         */ 
        public void mapChangeHandler(ChangeKind kind, Object target, int location, Object[] items) {
            MergeContext mergeContext = MergeContext.get(PersistenceManager.getEntityManager(target));
            if ((mergeContext != null && mergeContext.getSourceEntityManager() == this) || !isActive())
                return;
            
            if (target instanceof Component)
                return;
            
            if (kind == ChangeKind.ADD || kind == ChangeKind.REMOVE || kind == ChangeKind.REPLACE)
                addUpdates(target);
        }
        
        /**
         *  @private 
         *  Collection event handler to save changes on managed maps
         *
         *  @param event map event
         */ 
        public void entityMapChangeHandler(ChangeKind kind, Object target, int location, Object[] items) {
            MergeContext mergeContext = MergeContext.get(PersistenceManager.getEntityManager(target));
            if ((mergeContext != null && mergeContext.getSourceEntityManager() == this) || !isActive())
                return;
            
            Object[] parent = null;
            if (kind == ChangeKind.ADD && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                for (int i = 0; i < items.length; i++) {
                    if (items[i] instanceof Identifiable) {
                        if (parent != null)
                            addReference((Identifiable)items[i], parent[0], (String)parent[1], null);
                        else
                            attachEntity((Identifiable)items[i]);
                        dataManager.startTracking((Identifiable)items[i], parent != null ? parent[0] : null);
                    }
                    else if (items[i] instanceof Object[]) {
                        Object[] obj = (Object[])items[i];
                        if (obj[0] instanceof Identifiable) {
                            if (parent != null)
                                addReference((Identifiable)obj[0], parent[0], (String)parent[1], null);
                            else
                                attachEntity((Identifiable)obj[0]);
                            dataManager.startTracking((Identifiable)obj[0], parent != null ? parent[0] : null);
                        }
                        if (obj[1] instanceof Identifiable) {
                            if (parent != null)
                                addReference((Identifiable)obj[1], parent[0], (String)parent[1], null);
                            else
                                attachEntity((Identifiable)obj[1]);
                            dataManager.startTracking((Identifiable)obj[1], parent != null ? parent[0] : null);
                        }
                    }
                }
            }
            else if (kind == ChangeKind.REMOVE && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                if (parent != null) {
                    for (int i = 0; i < items.length; i++) {
                        if (items[i] instanceof Identifiable) {
                            removeReference((Identifiable)items[i], parent[0], (String)parent[1], null);
                        }
                        else if (items[i] instanceof Object[]) {
                            Object[] obj = (Object[])items[i];
                            if (obj[0] instanceof Identifiable) {
                                removeReference((Identifiable)obj[0], parent[0], (String)parent[1], null);
                            }
                            if (obj[1] instanceof Identifiable) {
                                removeReference((Identifiable)obj[1], parent[0], (String)parent[1], null);
                            }
                        }
                    }
                }
            }
            else if (kind == ChangeKind.REPLACE && items != null && items.length > 0) {
                parent = getOwnerEntity(target);
                for (int i = 0; i < items.length; i++) {
                    Object[] item = (Object[])items[i];
                    if (item[1] instanceof Identifiable) {
                        if (parent != null)
                            removeReference((Identifiable)item[1], parent[0], (String)parent[1], null);
                    }
                    if (item[2] instanceof Identifiable) {
                        if (parent != null)
                            addReference((Identifiable)item[2], parent[0], (String)parent[1], null);
                        else
                            attachEntity((Identifiable)item[2]);
                        dataManager.startTracking((Identifiable)item[2], parent != null ? parent[0] : null);
                    }
                }
            }
            
            if (!(kind == ChangeKind.ADD || kind == ChangeKind.REMOVE || kind == ChangeKind.REPLACE))
                return;
            
            log.debug("map changed: %s %s", kind, ObjectUtil.toString(target));
            
            if (mergeContext == null || !mergeContext.isMerging() || mergeContext.isResolvingConflict()) {
                if (parent == null)
                    log.warn("Owner entity not found for collection %s, cannot process dirty checking", ObjectUtil.toString(target));
                else
                    dirtyCheckContext.entityMapChangeHandler(parent[0], (String)parent[1], kind, location, items);
            }
            
            if (items != null && items.length > 0 && items[0] instanceof Object[] && ((Object[])items[0])[1] instanceof Identifiable) {
                addUpdates(target);
            }
            else if (kind == ChangeKind.UPDATE && items != null && items.length > 0) {
                if (((PropertyChange)items[0]).getObject() instanceof Identifiable)
                    addUpdates(target);
            }
        }
    }
    
    /**
     *  @private 
     *  Track updates on target object
     *
     *  @param object tracked object
     */ 
    private void addUpdates(Object object) {
        Expression ref = getReference(object, true, new HashSet<Object>());
        if (ref != null && expressionEvaluator != null) {
            Value value = expressionEvaluator.evaluate(ref);
            trackingContext.addUpdate(value.componentName, value.componentClassName, ref.getExpression(), value.value);
        }
    }
    
    /**
     *  @private 
     *  Track results on target object
     *
     *  @param object tracked object
     *  @param propName property name on tracked object
     */ 
    private void addResults(Object object, String propName) {
        Expression ref = getReference(object, true, new HashSet<Object>());
        if (ref != null && expressionEvaluator != null) {
            Value value = expressionEvaluator.getInstance(ref, object);
            trackingContext.addResult(value.componentName, value.componentClassName, 
                    ref.getExpression() != null ? ref.getExpression() + "." + propName : propName, value.instance);
        }
    }
    
    /**
     *  @private
     *  Handle data updates
     *
     *  @param sourceSessionId sessionId from which data updates come (null when from current session) 
     *  @param updates list of data updates
     */
    public void handleUpdates(MergeContext mergeContext, String sourceSessionId, List<Update> updates) {
        List<Object> merges = new ArrayList<Object>();
        List<Object> removals = new ArrayList<Object>();
        
        for (Update update : updates) {
            if (update.getKind() == UpdateKind.PERSIST || update.getKind() == UpdateKind.UPDATE)
                merges.add(update.getEntity());
            else if (update.getKind() == UpdateKind.REMOVE)
                removals.add(update.getEntity());
        }
        
        mergeContext.setExternalDataSessionId(sourceSessionId);
        internalMergeExternalData(mergeContext, merges, null, removals);
        
        for (Update update : updates)
            update.setEntity(getCachedObject(update.getEntity(), true));
    }
    
	public void raiseUpdateEvents(Context context, List<EntityManager.Update> updates) {
		List<String> refreshes = new ArrayList<String>();
		
		for (EntityManager.Update update : updates) {
			Object entity = update.getEntity();
			
			if (entity != null) {
				String entityName = entity instanceof EntityRef ? getUnqualifiedClassName(((EntityRef)entity).getClassName()) : entity.getClass().getSimpleName();
				String eventType = "org.granite.tide.data." + update.getKind().name().toLowerCase() + "." + entityName;
				context.getEventBus().raiseEvent(context, eventType, entity);
				
				if (UpdateKind.PERSIST.equals(update.getKind()) || UpdateKind.REMOVE.equals(update.getKind())) {
					if (!refreshes.contains(entityName))
						refreshes.add(entityName);
				} 
			}
		}
		
		for (String refresh : refreshes)
			context.getEventBus().raiseEvent(context, "org.granite.tide.data.refresh." + refresh);
	}
    
	private static String getUnqualifiedClassName(String className) {
		int idx = className.lastIndexOf(".");
		return idx >= 0 ? className.substring(idx+1) : className;
	}


    @Override
    public void setRemoteValidator(RemoteValidator remoteValidator) {
    }


    @Override
    public boolean validateObject(Object object, String property, Object value) {
        return false;
    }
}
