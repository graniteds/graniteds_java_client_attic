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

package org.granite.client.messaging.channel.amf;

import java.net.URI;

import org.granite.client.messaging.channel.JMFChannelFactory;
import org.granite.client.messaging.codec.JMFAMF3MessagingCodec;

/**
 * @author Franck WOLFF
 */
public class JMFAMFMessagingChannel extends AMFMessagingChannel {
	
	public JMFAMFMessagingChannel(JMFChannelFactory factory, String id, URI uri) {
		super(factory, id, uri, new JMFAMF3MessagingCodec(factory.getSharedContext()));
	}
}
