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

package org.granite.client.test.tide;

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.Producer;
import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.test.MockRemoteService;
import org.granite.client.tide.server.ServerSession.ServiceFactory;

public final class MockServiceFactory implements ServiceFactory {
	@Override
	public RemoteService newRemoteService(RemotingChannel remotingChannel, String destination) {
		return new MockRemoteService(remotingChannel, destination);
	}

	@Override
	public Producer newProducer(MessagingChannel messagingChannel, String destination, String topic) {
		return null;
	}

	@Override
	public Consumer newConsumer(MessagingChannel messagingChannel, String destination, String topic) {
		return null;
	}
}