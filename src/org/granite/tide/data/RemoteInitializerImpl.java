package org.granite.tide.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.granite.logging.Logger;
import org.granite.rpc.AsyncResponder;
import org.granite.rpc.events.FaultEvent;
import org.granite.rpc.events.ResultEvent;
import org.granite.rpc.remoting.RemoteObject;
import org.granite.tide.Context;
import org.granite.tide.Expression;
import org.granite.tide.collections.ManagedPersistentAssociation;
import org.granite.tide.impl.ObjectUtil;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.server.ServerSession;


public class RemoteInitializerImpl implements RemoteInitializer {
	
	private static final Logger log = Logger.getLogger(RemoteInitializerImpl.class);
	
	private final Context context;
	private boolean enabled = true;

	
	public RemoteInitializerImpl(Context context) {
		this.context = context;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	private List<Object[]> objectsInitializing = new ArrayList<Object[]>();
    
	/**
	 * 	{@inheritdoc}
	 */
    public boolean initializeObject(ServerSession serverSession, Object object) {
		if (!enabled || context.isFinished())
			return false;
		
		log.debug("initialize {0}", ObjectUtil.toString(object));
		
		if (!(object instanceof ManagedPersistentAssociation && ((ManagedPersistentAssociation)object).getOwner() instanceof Identifiable))
			return false;
		
		Object entity = ((ManagedPersistentAssociation)object).getOwner();
		EntityManager entityManager = PersistenceManager.getEntityManager(entity);
		Expression path = null;
		
		if (context.getContextId() != null && context.isContextIdFromServer())
			path = entityManager.getReference(entity, false, new HashSet<Object>());
		
		entityManager.addReference(entity, null, null, null);
		
		synchronized (objectsInitializing) {
			objectsInitializing.add(new Object[] { context, path != null ? path.getPath() : entity, ((ManagedPersistentAssociation)object).getPropertyName() });
		}
		
		context.callLater(new DoInitializeObjects(serverSession));
		return true;
	}
    
    public class DoInitializeObjects implements Runnable {
    	
    	private final ServerSession serverSession;
    	
    	public DoInitializeObjects(ServerSession serverSession) {
    		this.serverSession = serverSession;
    	}
    	
    	public void run() {
	    	Map<Object, List<String>> initMap = new HashMap<Object, List<String>>();
			
	    	synchronized (objectsInitializing) {
				for (int i = 0; i < objectsInitializing.size(); i++) {
					if (objectsInitializing.get(i)[0] != context)
						continue;
					
					List<String> propertyNames = initMap.get(objectsInitializing.get(i)[1]);
					if (propertyNames == null) {
						propertyNames = Arrays.asList((String)objectsInitializing.get(i)[2]);
						initMap.put(objectsInitializing.get(i)[1], propertyNames);
					}
					else
						propertyNames.add((String)objectsInitializing.get(i)[2]);
					
					objectsInitializing.remove(i--);
				}
	    	}
			
	    	RemoteObject ro = serverSession.getRemoteObject();
			for (Object entity : initMap.keySet()) {
				ro.call("initializeObject", new Object[] { entity, initMap.get(entity).toArray(), new InvocationCall() },
						new InitializerResponder(serverSession, entity));
			}
    	}
	}
	
    
    public class InitializerResponder implements AsyncResponder {
    	
    	private final ServerSession serverSession;
    	private final Object entity;
    	
    	public InitializerResponder(ServerSession serverSession, Object entity) {
    		this.serverSession = serverSession;
    		this.entity = entity;
    	}

		@Override
		public void result(final ResultEvent event) {
			context.callLater(new Runnable() {
				public void run() {
					EntityManager entityManager = PersistenceManager.getEntityManager(entity);
					
					boolean saveUninitializeAllowed = entityManager.isUninitializeAllowed();
					try {
						entityManager.setUninitializeAllowed(false);
						
						// Assumes objects is a PersistentCollection or PersistentMap
						serverSession.handleResult(context, null, null, (InvocationResult)event.getResult(), ((InvocationResult)event.getResult()).getResult(), null);
					}
					finally {
						entityManager.setUninitializeAllowed(saveUninitializeAllowed);
					}
				}
			});
		}

		@Override
		public void fault(final FaultEvent event) {
			context.callLater(new Runnable() {
				public void run() {
					log.error("Fault initializing collection " + ObjectUtil.toString(entity) + " " + event.toString());
					
					serverSession.handleFault(context, null, null, event.getMessage());
				}
			});
		}   	
    }
}