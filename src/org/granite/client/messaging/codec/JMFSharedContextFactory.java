package org.granite.client.messaging.codec;

import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.DefaultSharedContext;
import org.granite.messaging.jmf.SharedContext;
import org.granite.util.JMFAMFUtil;

public class JMFSharedContextFactory {

	private static SharedContext context = null;
	
	public static synchronized SharedContext getInstance() {
		if (context == null)
			context = new DefaultSharedContext(new DefaultCodecRegistry(), JMFAMFUtil.AMF_DEFAULT_STORED_STRINGS);
		return context;
	}
}
