package org.granite.tide.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.granite.tide.rpc.ServerSession;


public class MergeContext {
    
    private static ThreadLocal<Map<EntityManager, MergeContext>> mergeContext = new ThreadLocal<Map<EntityManager, MergeContext>>() {
        @Override
        protected Map<EntityManager, MergeContext> initialValue() {
            return new HashMap<EntityManager, MergeContext>();
        }
    };
    
    private final EntityManager entityManager;
    @SuppressWarnings("unused")
	private final DirtyCheckContext dirtyCheckContext;
    
    private String externalDataSessionId = null;
    private EntityManager sourceEntityManager = null;
    private ServerSession serverSession = null;
    private boolean mergeUpdate = false;
    private boolean merging = false;
    private Map<Object, Object> entityCache = null;
    private Set<Object> versionChangeCache = null;
    private boolean resolvingConflict = false;
    private Conflicts mergeConflicts = null;
    private boolean uninitializing = false;
    
    
    public static MergeContext get(EntityManager entityManager) {
        return mergeContext.get().get(entityManager);
    }
    
    public static void destroy(EntityManager entityManager) {
        mergeContext.get().remove(entityManager);
    }
    
    
    public MergeContext(EntityManager entityManager, DirtyCheckContext dirtyCheckContext, ServerSession serverSession) {
        this.entityManager = entityManager;
        this.dirtyCheckContext = dirtyCheckContext;
        this.serverSession = serverSession;
        mergeContext.get().put(entityManager, this);
    }

    
    public void initMerge() {
        if (this.entityCache == null) {
            this.entityCache = new IdentityHashMap<Object, Object>();
            this.mergeUpdate = true;
        }
    }

    public void addConflict(Identifiable dest, Identifiable obj) {
        if (this.mergeConflicts == null)
            this.mergeConflicts = new Conflicts(this.entityManager);
        this.mergeConflicts.addConflict(dest, obj);
    }

    public void initMergeConflicts() {
        this.versionChangeCache = null;
        this.resolvingConflict = false;
    }

    public void checkConflictsResolved() {
        if (this.mergeConflicts != null && this.mergeConflicts.isAllResolved())
            this.mergeConflicts = null;
    }
    
    public boolean isResolvingConflict() {
        return this.resolvingConflict;
    }
    
    public void setResolvingConflict(boolean resolvingConflict) {
        this.resolvingConflict = resolvingConflict;
    }

    public Conflicts getMergeConflicts() {
        return this.mergeConflicts;
    }

    public String getExternalDataSessionId() {
        return this.externalDataSessionId;
    }

    public void setExternalDataSessionId(String externalDataSessionId) {
        this.externalDataSessionId = externalDataSessionId;
    }
    
    public void setServerSession(ServerSession serverSession) {
    	this.serverSession = serverSession;
    }
    
    public ServerSession getServerSession() {
    	return serverSession;
    }

    public void setSourceEntityManager(EntityManager sourceEntityManager) {
        this.sourceEntityManager = sourceEntityManager;
    }
    
    public EntityManager getSourceEntityManager() {
        return this.sourceEntityManager;
    }
    
    public boolean isMergeUpdate() {
        return this.mergeUpdate;
    }
    
    public void setMergeUpdate(boolean mergeUpdate) {
        this.mergeUpdate = mergeUpdate;
    }

    public boolean isMerging() {
        return this.merging;
    }
    
    public void setMerging(boolean merging) {
        this.merging = merging;
    }
    
    public Object getFromCache(Object obj) {
        if (this.entityCache == null)
            return null;
        return this.entityCache.get(obj);
    }
    
    public void putInCache(Object obj, Object dest) {
        if (this.entityCache != null)
            this.entityCache.put(obj, dest);
    }
    
    public void clearCache() {
        this.entityCache = null;
    }
    
    private Set<Object> getVersionChangeCache() {
        if (this.versionChangeCache == null)
            this.versionChangeCache = new HashSet<Object>();
        return this.versionChangeCache;
    }
    
    public void markVersionChanged(Object obj) {
        getVersionChangeCache().add(obj);
    }
    
    public boolean hasVersionChanged(Object obj) {
        return this.versionChangeCache != null ? this.versionChangeCache.contains(obj) : false;
    }

    public void setUninitializing(boolean uninitializing) {
        this.uninitializing = uninitializing;
    }

    public boolean isUninitializing() {
        return this.uninitializing;
    }

    public boolean isUninitializeAllowed() {
        return this.entityManager.isUninitializeAllowed();
    }
}
