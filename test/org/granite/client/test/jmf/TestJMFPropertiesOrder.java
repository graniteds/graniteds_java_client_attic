package org.granite.client.test.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.jmf.DefaultClientSharedContext;
import org.granite.client.messaging.jmf.ext.ClientEntityCodec;
import org.granite.client.test.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.client.test.jmf.Util.ByteArrayJMFDumper;
import org.granite.client.test.jmf.Util.ByteArrayJMFSerializer;
import org.granite.hibernate.jmf.EntityCodec;
import org.granite.hibernate.jmf.PersistentBagCodec;
import org.granite.hibernate.jmf.PersistentListCodec;
import org.granite.hibernate.jmf.PersistentMapCodec;
import org.granite.hibernate.jmf.PersistentSetCodec;
import org.granite.hibernate.jmf.PersistentSortedMapCodec;
import org.granite.hibernate.jmf.PersistentSortedSetCodec;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.DefaultSharedContext;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.messaging.jmf.SharedContext;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJMFPropertiesOrder {
	
	private SharedContext dumpSharedContext;
	private SharedContext serverSharedContext;
	private ClientSharedContext clientSharedContext;
	
	@Before
	public void before() {
		
		List<ExtendedObjectCodec> serverExtendedObjectCodecs = Arrays.asList((ExtendedObjectCodec)
			new EntityCodec(),
			new PersistentListCodec(),
			new PersistentSetCodec(),
			new PersistentBagCodec(),
			new PersistentMapCodec(),
			new PersistentSortedSetCodec(),
			new PersistentSortedMapCodec()
		);
		List<ExtendedObjectCodec> clientExtendedObjectCodecs = Arrays.asList((ExtendedObjectCodec)
			new ClientEntityCodec()
		);
		
		dumpSharedContext = new DefaultSharedContext(new DefaultCodecRegistry());
		
		serverSharedContext = new DefaultSharedContext(new DefaultCodecRegistry(serverExtendedObjectCodecs));
		
		clientSharedContext = new DefaultClientSharedContext(new DefaultCodecRegistry(clientExtendedObjectCodecs));
	}
	
	@After
	public void after() {
		dumpSharedContext = null;
		serverSharedContext = null;
		clientSharedContext = null;
	}

	@Test
	public void testPropertiesOrder() throws ClassNotFoundException, IOException {
		
		clientSharedContext.registerAlias(ClientConcretePersitableChild.class);

		ConcretePersitableChild entity = new ConcretePersitableChild();
		entity.setId(12);
		entity.setA("AAAA");
		entity.setZ("ZZZZZZZZZ");
		
		Object clientEntity = serializeAndDeserializeServerToClient(entity, true);
		Assert.assertTrue(clientEntity instanceof ClientConcretePersitableChild);
		Assert.assertEquals(entity.getId(), ((ClientConcretePersitableChild)clientEntity).getId());
		Assert.assertEquals(entity.getA(), ((ClientConcretePersitableChild)clientEntity).getA());
		Assert.assertEquals(entity.getZ(), ((ClientConcretePersitableChild)clientEntity).getZ());
		
		Object serverEntity = serializeAndDeserializeClientToServer(clientEntity, true);
		Assert.assertTrue(serverEntity instanceof ConcretePersitableChild);
		Assert.assertEquals(((ClientConcretePersitableChild)clientEntity).getId(), ((ConcretePersitableChild)serverEntity).getId());
		Assert.assertEquals(((ClientConcretePersitableChild)clientEntity).getA(), ((ConcretePersitableChild)serverEntity).getA());
		Assert.assertEquals(((ClientConcretePersitableChild)clientEntity).getZ(), ((ConcretePersitableChild)serverEntity).getZ());
	}
//	
//	private Object serializeAndDeserializeServerToServer(Object obj, boolean dump) throws ClassNotFoundException, IOException {
//		return serializeAndDeserialize(serverSharedContext, dumpSharedContext, serverSharedContext, obj, dump);
//	}
	
	private Object serializeAndDeserializeServerToClient(Object obj, boolean dump) throws ClassNotFoundException, IOException {
		return serializeAndDeserialize(serverSharedContext, dumpSharedContext, clientSharedContext, obj, dump);
	}
	
	private Object serializeAndDeserializeClientToServer(Object obj, boolean dump) throws ClassNotFoundException, IOException {
		return serializeAndDeserialize(clientSharedContext, dumpSharedContext, serverSharedContext, obj, dump);
	}
	
	private Object serializeAndDeserialize(
		SharedContext serializeSharedContext,
		SharedContext dumpSharedContext,
		SharedContext deserializeSharedContext,
		Object obj,
		boolean dump) throws ClassNotFoundException, IOException {
		
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(serializeSharedContext);
		serializer.writeObject(obj);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		
		PrintStream ps = Util.newNullPrintStream();
		if (dump) {
			System.out.println(bytes.length + "B. " + Util.toHexString(bytes));
			ps = System.out;
		}
		
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, dumpSharedContext, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, deserializeSharedContext);
		Object clone = deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
