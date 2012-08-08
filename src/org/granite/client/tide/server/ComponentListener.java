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

package org.granite.client.tide.server;

import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.tide.Context;

/**
 * @author William DRAI
 */
public interface ComponentListener extends ResponseListener {
    
    public String getOperation();
    
    public Object[] getArgs();
    public void setArgs(Object[] args);
    
    public Context getSourceContext();
    
    public Component getComponent();
    
    
    public static interface Handler {
        
        public void result(Context context, ResultEvent event, Object info, String componentName, String operation, TideResponder<?> tideResponder, ComponentListener componentResponder);
        
        public void fault(Context context, FaultEvent event, Object info, String componentName, String operation, TideResponder<?> tideResponder, ComponentListener componentResponder);
    }
}
