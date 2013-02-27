package org.granite.client.test.javafx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.SimpleGraniteContext;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestExternalizer {
	
	private ServicesConfig servicesConfig = null;
	private GraniteConfig graniteConfigJavaFX = null;
	private GraniteConfig graniteConfigHibernate = null;
	

	@Before
	public void before() throws Exception {
		servicesConfig = new ServicesConfig(null, null, false);
		InputStream is = getClass().getClassLoader().getResourceAsStream("org/granite/client/test/javafx/granite-config-javafx.xml");
		graniteConfigJavaFX = new GraniteConfig(null, is, null, null);
		graniteConfigJavaFX.registerClassAlias(FXEntity1.class);
		graniteConfigJavaFX.registerClassAlias(FXEntity2.class);
		graniteConfigJavaFX.registerClassAlias(FXEntity1b.class);
		graniteConfigJavaFX.registerClassAlias(FXEntity2b.class);
		graniteConfigJavaFX.registerClassAlias(org.granite.client.persistence.javafx.PersistentList.class);
		graniteConfigJavaFX.registerClassAlias(org.granite.client.persistence.javafx.PersistentSet.class);
		is = getClass().getClassLoader().getResourceAsStream("org/granite/client/test/javafx/granite-config-hibernate.xml");
		graniteConfigHibernate = new GraniteConfig(null, is, null, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationSetServerToClient() throws Exception {
		Entity1b entity1 = new Entity1b();
		entity1.setName("Test");
		entity1.setList(new PersistentSet(null, new HashSet<Entity2>()));
		Entity2b entity2 = new Entity2b();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigJavaFX.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof FXEntity1b);
	}

	@Test
	public void testExternalizationSetClientToServer() throws Exception {
		FXEntity1b entity1 = new FXEntity1b();
		entity1.setName("Test");
		FXEntity2b entity2 = new FXEntity2b();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigJavaFX.newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof Entity1b);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationListServerToClient() throws Exception {
		Entity1 entity1 = new Entity1();
		entity1.setName("Test");
		entity1.setList(new PersistentList(null, new ArrayList<Entity2>()));
		Entity2 entity2 = new Entity2();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigJavaFX.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof FXEntity1);
	}

	@Test
	public void testExternalizationListClientToServer() throws Exception {
		FXEntity1 entity1 = new FXEntity1();
		entity1.setName("Test");
		FXEntity2 entity2 = new FXEntity2();
		entity2.setName("Test2");
		entity1.getList().add(entity2);
		entity2.setEntity1(entity1);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigJavaFX.newAMF3Serializer(baos);
		out.writeObject(entity1);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigHibernate.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Entity type", entity instanceof Entity1);
	}
}
