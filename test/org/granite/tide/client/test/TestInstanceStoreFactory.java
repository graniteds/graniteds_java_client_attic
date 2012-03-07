package org.granite.tide.client.test;

import org.granite.tide.Context;
import org.granite.tide.InstanceStore;
import org.granite.tide.InstanceStoreFactory;

public class TestInstanceStoreFactory implements InstanceStoreFactory {

	@Override
	public InstanceStore createStore(Context context) {
		return new TestInstanceStore(context);
	}
}
