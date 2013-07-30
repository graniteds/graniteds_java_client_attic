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

import org.granite.client.messaging.messages.responses.FaultMessage.Code;

/**
 * @author William DRAI
 */
public class Fault {

    private Code faultCode;
    private String faultDescription;
    private String faultDetails;
    
    private Object content;
    
    private Object cause;
    
    public Fault(Code faultCode, String faultDescription, String faultDetails) {
        this.faultCode = faultCode;
        this.faultDescription = faultDescription;
        this.faultDetails = faultDetails;
    }
    
    public Code getCode() {
        return faultCode;
    }
    
    public String getFaultDescription() {
        return faultDescription;
    }
    
    public String getFaultDetails() {
        return faultDetails;
    }

    
    public Object getContent() {
        return content;
    }
    
    public void setContent(Object context) {
        this.content = context;
    }
    
    public Object getCause() {
        return cause;
    }
    
    public void setCause(Object cause) {
        this.cause = cause;
    }
}
