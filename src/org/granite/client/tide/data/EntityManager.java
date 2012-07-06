package org.granite.client.tide.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.client.tide.Context;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.data.spi.MergeContext;
import org.granite.client.tide.server.ServerSession;
import org.granite.tide.Expression;


/**
 *  EntityManager is the interface for entity management (!)
 *  It is implemented by the Tide context
 *
 *  @author William DRAI
 */
public interface EntityManager {
    
    /**
     *  Return the entity manager id
     * 
     *  @return the entity manager id
     */
    public String getId();
    
    /**
     *  Return the entity manager state
     * 
     *  @return the entity manager state
     */
    public boolean isActive();
    
    /**
     *  Clear entity cache
     */ 
    public void clearCache();
    
    /**
     *  Clear the current context
     *  Destroys all components/context variables
     */
    public void clear();
    
    public DataManager getDataManager();
    
    /**
     *  Allow uninitialize of persistent collections
     *
     *  @param allowed allow uninitialize of collections
     */
    public void setUninitializeAllowed(boolean allowed);
    
    /**
     *  @return allow uninitialize of collections
     */
    public boolean isUninitializeAllowed();
    
    
    public static interface Propagation {
        
        public void propagate(Identifiable entity, Function func);
    }
    
    public static interface Function {
        
        public void execute(EntityManager entityManager, Identifiable entity);
    }
    
    /**
     *  Setter for the propagation manager
     * 
     *  @param propagation propagation function that will visit child entity managers
     */
    public void setEntityManagerPropagation(Propagation propagation);
    
    /**
     *  Setter for the remote initializer implementation
     * 
     *  @param remoteInitializer instance of IRemoteInitializer
     */
    public void setRemoteInitializer(RemoteInitializer remoteInitializer);
    
    /**
     *  Setter for the remote validator implementation
     * 
     *  @param remoteValidator instance of IRemoteValidator
     */
    public void setRemoteValidator(RemoteValidator remoteValidator);
    
    /**
     *  Create a new temporary entity manager
     * 
     *  @return a temporary entity manager
     */
    public EntityManager newTemporaryEntityManager();
    
    
    /**
     *  Intercept a property getter
     * 
     *  @param entity intercepted entity
     *  @param propName intercepted property name
     *  @param value current value
     */
    public Object getEntityProperty(Identifiable entity, String propName, Object value);
    
    /**
     *  Intercept a property setter
     * 
     *  @param entity intercepted entity
     *  @param propName intercepted property name
     *  @param oldValue old value
     *  @param newValue new value
     */
    public void setEntityProperty(Identifiable entity, String propName, Object oldValue, Object newValue);
    
    /**
     *  Register a reference to the provided object with either a parent or res
     * 
     *  @param entity an entity
     *  @param parent the parent entity
     *  @param propName name of the parent entity property that references the entity
     *  @param res the context expression
     */ 
    public void addReference(Object entity, Object parent, String propName, Expression expr);
    
    /**
     *  Remove a reference on the provided object
     *
     *  @param obj an entity
     *  @param parent the parent entity to dereference
     *  @param propName name of the parent entity property that references the entity
     *  @param res expression to remove
     */ 
    public void removeReference(Object entity, Object parent, String propName, Expression exp);
    
    /**
     *  Retrieves context expression path for the specified entity (internal implementation)
     *   
     *  @param obj an entity
     *  @param recurse should recurse until 'real' context path, otherwise object reference can be returned
     *  @param cache graph visitor cache
     * 
     *  @return the path from the entity context (or null is no path found)
     */
    public Expression getReference(Object entity, boolean recurse, Set<Object> cache);
    
    /**
     *  Entity manager is dirty when any entity/collection/map has been modified
     *
     *  @return is dirty
     */
    public boolean isDirty();
    
    /**
     *  Indicates if the entity is persisted on the server (id/version not null/NaN)
     *
     *  @param entity an entity
     *  @return true if saved
     */
    public boolean isSaved(Object entity);
    
    /**
     *  @private 
     *  Retrieve an entity in the cache from its uid
     *   
     *  @param object an entity
     *  @param nullIfAbsent return null if entity not cached in context
     */
    public Object getCachedObject(Object object, boolean nullIfAbsent);
    
    public MergeContext initMerge();
    
    public Object mergeExternal(final MergeContext mergeContext, Object obj, Object previous, Expression expr, Object parent, String propertyName, String setter, boolean forceUpdate);
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *  @param obj external object
     *  @param prev existing local object to merge with
     *  @param externalDataSessionId sessionId from which the data is coming (other user/server), null if local or current user session
     *  @param removals array of entities to remove from the entity manager cache
     *
     *  @return merged object (should === previous when previous not null)
     */
    public Object mergeExternalData(Object obj, Object prev, String externalDataSessionId, List<Object> removals);
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *  @param obj external object
     *
     *  @return merged object
     */
    public Object mergeExternalData(ServerSession serverSession, Object obj);
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *  @param obj external object
     *  @param prev existing local object to merge with
     *  @param externalDataSessionId sessionId from which the data is coming (other user/server), null if local or current user session
     *  @param removals array of entities to remove from the entity manager cache
     *
     *  @return merged object (should === previous when previous not null)
     */
    public Object mergeExternalData(ServerSession serverSession, Object obj, Object prev, String externalDataSessionId, List<Object> removals);
    
    /**
     *  Merge an object coming from a remote location (in general from a service) in the local context
     *
     *  @param obj external object
     *
     *  @return merged object
     */
    public Object mergeExternalData(Object obj);
    
    public Object internalMergeExternalData(MergeContext mergeContext, Object obj, Object prev, List<Object> removals);
    
    /**
     *  @private 
     *  Merge an object coming from another entity manager (in general in the global context) in the local context
     *
     *  @param sourceEntityManager source context of incoming data
     *  @param obj external object
     *  @param externalDataSessionId is merge from external data
     *
     *  @return merged object (should === previous when previous not null)
     */
    public Object mergeFromEntityManager(EntityManager sourceEntityManager, Object obj, String externalDataSessionId, boolean uninitializing);
    
    /**
     *  Merge conversation entity manager context variables in global entity manager 
     *  Only applicable to conversation contexts 
     * 
     *  @param entityManager conversation entity manager
     */
    public void mergeInEntityManager(EntityManager entityManager);
    
    /**
     *  Discard changes of entity from last version received from the server
     *
     *  @param entity entity to restore
     *  @param cache reset cache
     */ 
    public void resetEntity(Identifiable entity);
    
    /**
     *  Discard changes of all cached entities from last version received from the server
     * 
     *  @param cache reset cache
     */ 
    public void resetAllEntities();
    
    /**
     *  Current map of saved properties for the specified entity
     *  @param entity an entity
     * 
     *  @return saved properties for this entity
     */
    public Map<String, Object> getSavedProperties(Object entity);
    
    
    public static enum UpdateKind {
        PERSIST,
        UPDATE,
        REMOVE;
        
        public static UpdateKind forName(String kind) {
            if ("PERSIST".equals(kind))
                return PERSIST;
            else if ("UPDATE".equals(kind))
                return UPDATE;
            else if ("REMOVE".equals(kind))
                return REMOVE;
            throw new IllegalArgumentException("Unknown update kind " + kind);
        }
    }
    
    public static class Update {
        
        private final UpdateKind kind;
        private Object entity;
        
        public Update(UpdateKind kind, Object entity) {
            this.kind = kind;
            this.entity = entity;
        }
        
        public UpdateKind getKind() {
            return kind;
        }
        
        public Object getEntity() {
            return entity;
        }

        public void setEntity(Object entity) {
            this.entity = entity;
        }
        
        public static Update forUpdate(String kind, Object entity) {
            return new Update(UpdateKind.forName(kind), entity);
        }
    }
    
    /**
     *  @private
     *  Handle data updates
     *
     *  @param sourceSessionId sessionId from which data updates come (null when from current session) 
     *  @param updates list of data updates
     */
    public void handleUpdates(MergeContext mergeContext, String sourceSessionId, List<Update> updates);
    
	public void raiseUpdateEvents(Context context, List<EntityManager.Update> updates);
	
    
    public void addListener(DataConflictListener listener);
    
    public void removeListener(DataConflictListener listener);
    
    /**
     *  Accept values for conflict
     * 
     *  @param conflict conflict
     *  @param acceptClient true: keep client changes, false: override with server changes
     */
    public void acceptConflict(Conflict conflict, boolean acceptClient);
    
    /**
     *  Trigger remote initialization of lazy-loaded objects
     * 
     *  @param object a lazy-loaded object
     * 
     *  @return true if initialization triggered
     */
    public boolean initializeObject(ServerSession serverSession, Object object);
    
    /**
     *  Trigger remote validation of objects
     * 
     *  @param object a lazy-loaded object
     * 
     *  @return true if validation triggered
     */
    public boolean validateObject(Object object, String property, Object value);
    
    
    public static interface PropagationPolicy {
        
        public void propagate();
    }
}
