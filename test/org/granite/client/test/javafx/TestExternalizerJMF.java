package org.granite.client.test.javafx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.jmf.DefaultClientSharedContext;
import org.granite.hibernate.jmf.EntityCodec;
import org.granite.messaging.jmf.CodecRegistry;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.DefaultSharedContext;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.jmf.SharedContext;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.hibernate.collection.PersistentSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestExternalizerJMF {
	
	private SharedContext serverSharedContext;
	private ClientSharedContext clientSharedContext;
	private ClientAliasRegistry clientAliasRegistry = new ClientAliasRegistry();
	
	@Before
	public void before() throws Exception {
		List<ExtendedObjectCodec> serverCodecs = new ArrayList<ExtendedObjectCodec>();
		serverCodecs.add(new EntityCodec());
		CodecRegistry serverCodecRegistry = new DefaultCodecRegistry(serverCodecs);
		serverSharedContext = new DefaultSharedContext(serverCodecRegistry);
		
		List<ExtendedObjectCodec> clientCodecs = new ArrayList<ExtendedObjectCodec>();
		//clientCodecs.add(new JavaFXEntityCodec());
		CodecRegistry clientCodecRegistry = new DefaultCodecRegistry(clientCodecs);
		clientAliasRegistry.registerAlias("org.granite.client.persistence.javafx.PersistentSet", "org.granite.client.persistence.collection.PersistentSet");
		clientAliasRegistry.registerAlias("org.granite.client.persistence.javafx.PersistentList", "org.granite.client.persistence.collection.PersistentList");
		clientAliasRegistry.registerAlias("org.granite.client.persistence.javafx.PersistentMap", "org.granite.client.persistence.collection.PersistentMap");
		
		clientSharedContext = new DefaultClientSharedContext(clientCodecRegistry, null, null, clientAliasRegistry);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationSetServerToClient() throws Exception {
		clientAliasRegistry.registerAlias("org.granite.client.test.javafx.FXEntity1b", "org.granite.client.test.javafx.Entity1b");
		clientAliasRegistry.registerAlias("org.granite.client.test.javafx.FXEntity2b", "org.granite.client.test.javafx.Entity2b");
		
		Entity1b entity1 = new Entity1b();
		entity1.setName("Test");
		entity1.setList(new PersistentSet(null, new HashSet<Entity2b>()));
		Entity2b entity2 = new Entity2b();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = new JMFSerializer(baos, serverSharedContext);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = new JMFDeserializer(bais, clientSharedContext);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof FXEntity1b);
	}

	@Test
	public void testExternalizationSetClientToServer() throws Exception {
		clientAliasRegistry.registerAlias("org.granite.client.test.javafx.Entity1b", "org.granite.client.test.javafx.FXEntity1b");
		clientAliasRegistry.registerAlias("org.granite.client.test.javafx.Entity2b", "org.granite.client.test.javafx.FXEntity2b");
		
		FXEntity1b entity1 = new FXEntity1b();
		entity1.setName("Test");
		FXEntity2b entity2 = new FXEntity2b();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = new JMFSerializer(baos, clientSharedContext);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		JMFDumper dumper = new JMFDumper(new ByteArrayInputStream(buf), clientSharedContext, System.out);
		dumper.dump();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = new JMFDeserializer(bais, serverSharedContext);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof Entity1b);
	}

//	@Test
//	public void testExternalizationPersistentSetClientToServer() throws Exception {
//		FXEntity1b entity1 = new FXEntity1b();
//		entity1.setList(new org.granite.client.persistence.javafx.PersistentSet<FXEntity2b>());
//		entity1.setName("Test");
//		FXEntity2b entity2 = new FXEntity2b();
//		entity2.setName("Test2");
//		entity1.getList().add(entity2);
//		entity2.setEntity1(entity1);
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
//		ObjectOutput out = graniteConfigJavaFX.newAMF3Serializer(baos);
//		out.writeObject(entity1);
//		
//		byte[] buf = baos.toByteArray();
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
//		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
//		Object entity = in.readObject();
//		
//		Assert.assertTrue("Entity type", entity instanceof Entity1b);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testExternalizationListServerToClient() throws Exception {
//		Entity1 entity1 = new Entity1();
//		entity1.setName("Test");
//		entity1.setList(new PersistentList(null, new ArrayList<Entity2>()));
//		Entity2 entity2 = new Entity2();
//		entity2.setName("Test2");
//		entity1.getList().add(entity2);
//		entity2.setEntity1(entity1);
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
//		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
//		out.writeObject(entity1);
//		
//		byte[] buf = baos.toByteArray();
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
//		ObjectInput in = graniteConfigJavaFX.newAMF3Deserializer(bais);
//		Object entity = in.readObject();
//		
//		Assert.assertTrue("Entity type", entity instanceof FXEntity1);
//	}
//
//	@Test
//	public void testExternalizationListClientToServer() throws Exception {
//		FXEntity1 entity1 = new FXEntity1();
//		entity1.setName("Test");
//		FXEntity2 entity2 = new FXEntity2();
//		entity2.setName("Test2");
//		entity1.getList().add(entity2);
//		entity2.setEntity1(entity1);
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
//		ObjectOutput out = graniteConfigJavaFX.newAMF3Serializer(baos);
//		out.writeObject(entity1);
//		
//		byte[] buf = baos.toByteArray();
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
//		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
//		Object entity = in.readObject();
//		
//		Assert.assertTrue("Entity type", entity instanceof Entity1);
//	}
//
//	@Test
//	public void testExternalizationPersistentListClientToServer() throws Exception {
//		FXEntity1 entity1 = new FXEntity1();
//		entity1.setList(new org.granite.client.persistence.javafx.PersistentList<FXEntity2>());
//		entity1.setName("Test");
//		FXEntity2 entity2 = new FXEntity2();
//		entity2.setName("Test2");
//		entity1.getList().add(entity2);
//		entity2.setEntity1(entity1);
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
//		ObjectOutput out = graniteConfigJavaFX.newAMF3Serializer(baos);
//		out.writeObject(entity1);
//		
//		byte[] buf = baos.toByteArray();
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
//		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
//		Object entity = in.readObject();
//		
//		Assert.assertTrue("Entity type", entity instanceof Entity1);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testExternalizationMapServerToClient() throws Exception {
//		Entity1c entity1 = new Entity1c();
//		entity1.setName("Test");
//		entity1.setMap(new PersistentMap(null, new HashMap<String, Entity2c>()));
//		Entity2c entity2 = new Entity2c();
//		entity2.setName("Test2");
//		entity1.getMap().put("test", entity2);
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
//		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
//		out.writeObject(entity1);
//		
//		byte[] buf = baos.toByteArray();
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
//		ObjectInput in = graniteConfigJavaFX.newAMF3Deserializer(bais);
//		Object entity = in.readObject();
//		
//		Assert.assertTrue("Entity type", entity instanceof FXEntity1c);
//		Assert.assertEquals("Entity2 value", "Test2", ((FXEntity1c)entity).getMap().get("test").getName());
//	}
//
//	@Test
//	public void testExternalizationMapClientToServer() throws Exception {
//		FXEntity1c entity1 = new FXEntity1c();
//		entity1.setName("Test");
//		FXEntity2c entity2 = new FXEntity2c();
//		entity2.setName("Test2");
//		entity1.getMap().put("test", entity2);
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
//		ObjectOutput out = graniteConfigJavaFX.newAMF3Serializer(baos);
//		out.writeObject(entity1);
//		
//		byte[] buf = baos.toByteArray();
//		
//		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
//		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
//		Object entity = in.readObject();
//		
//		Assert.assertTrue("Entity type", entity instanceof Entity1c);
//		Assert.assertEquals("Entity2 value", "Test2", ((Entity1c)entity).getMap().get("test").getName());
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testExternalizationPerfServerToClient() throws Exception {
//		List<Entity1c> list = new ArrayList<Entity1c>(10000);
//		for (int i = 0; i < 200; i++) {
//			Entity1c entity1 = new Entity1c();
//			entity1.setName("Test" + i);
//			entity1.setValue(new BigDecimal((i+1)*67.89));
//			entity1.setValue2(new BigDecimal((i+1)*23.78));
//			entity1.setMap(new PersistentMap(null, new HashMap<String, Entity2c>()));
//			Entity2c entity2 = new Entity2c();
//			entity2.setName("Test" + i);
//			entity1.getMap().put("test" + i, entity2);
//			entity1.getMap().put("tost", entity2);
//			list.add(entity1);
//		}
//		
//		for (int test = 0; test < 5; test++) {
//			long time = System.nanoTime();
//			
//			SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//			ByteArrayOutputStream baos = new ByteArrayOutputStream(1000000);
//			ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
//			out.writeObject(list);
//			
//			byte[] buf = baos.toByteArray();
//			
//			long elapsedTimeServer = (System.nanoTime()-time)/1000000;
//			System.out.println("Elapsed time server: " + elapsedTimeServer);
//			time = System.nanoTime();
//			System.out.println("Buf size: " + buf.length);
//			
//			SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
//			ByteArrayInputStream bais = new ByteArrayInputStream(buf);
//			ObjectInput in = graniteConfigJavaFX.newAMF3Deserializer(bais);
//			Object read = in.readObject();
//			
//			long elapsedTimeClient = (System.nanoTime()-time)/1000000;
//			System.out.println("Elapsed time client: " + elapsedTimeClient);
//			
//			Assert.assertTrue("Result type", read instanceof List<?>);
//		}
//	}
}
