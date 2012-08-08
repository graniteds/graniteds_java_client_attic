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

package org.granite.client.tide;

import java.util.List;

/**
 * @author William DRAI
 */
public interface ContextManager {
	
    public void setInstanceStoreFactory(InstanceStoreFactory instanceStoreFactory);
    
    public void setBeanManager(BeanManager beanManager);
    
    public Context getContext();
    
    public Context getContext(String contextId);
    
    public Context getContext(String contextId, String parentContextId, boolean create);
    
    public Context newContext(String contextId, String parentContextId);
    
    public Context retrieveContext(Context sourceContext, String contextId, boolean wasConversationCreated, boolean wasConversationEnded);
    
    public void updateContextId(String previousContextId, Context context);
    
    public void destroyContext(String contextId, boolean force);
    
    public List<Context> getAllContexts();
    
    // function forEachChildContext(parentContext:Context, callback:Function, token:Object = null):void;

    public void destroyContexts(boolean force);
    
    public void destroyFinishedContexts();
    
    public void addToContextsToDestroy(String contextId);
    
    public void removeFromContextsToDestroy(String contextId);
}
