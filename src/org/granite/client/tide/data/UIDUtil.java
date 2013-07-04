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

package org.granite.client.tide.data;

import org.granite.client.persistence.Persistence;
import org.granite.util.UUIDUtil;

/**
 * @author William DRAI
 */
public abstract class UIDUtil {

    public static String getUid(Object uidObject) {
    	if (Persistence.hasUid(uidObject)) {
	    	String uid = Persistence.getUid(uidObject);
	    	if (uid == null) {
	    		uid = UUIDUtil.randomUUID();
	    		Persistence.setUid(uidObject, uid);
	    	}
	    	return uid;
    	}
    	Object id = Persistence.getId(uidObject);
    	if (id != null)
    		return uidObject.getClass().getSimpleName() + ":" + id.toString();
    	return uidObject.getClass().getSimpleName() + "::" + System.identityHashCode(uidObject);
    }
    
    public static String getCacheKey(Object uidObject) {
    	return uidObject.getClass().getName() + ":" + getUid(uidObject);
    }
    
}
