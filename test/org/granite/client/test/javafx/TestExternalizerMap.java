package org.granite.client.test.javafx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.codec.MessagingCodec.ClientType;
import org.granite.client.platform.javafx.SimpleJavaFXConfiguration;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.SimpleGraniteContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestExternalizerMap {
	
	private ServicesConfig servicesConfig = null;
	private GraniteConfig graniteConfigJavaFX = null;
	private GraniteConfig graniteConfigHibernate = null;
	

	@Before
	public void before() throws Exception {
		Configuration configuration = new SimpleJavaFXConfiguration();
		configuration.load();
		graniteConfigJavaFX = configuration.getGraniteConfig();
		ClientAliasRegistry aliasRegistry = (ClientAliasRegistry)graniteConfigJavaFX.getAliasRegistry();
		aliasRegistry.registerAlias(FXEntity1.class);
		aliasRegistry.registerAlias(FXEntity2.class);
		aliasRegistry.registerAlias(FXEntity1b.class);
		aliasRegistry.registerAlias(FXEntity2b.class);
		aliasRegistry.registerAlias(FXEntity1c.class);
		aliasRegistry.registerAlias(FXEntity2c.class);
		InputStream is = getClass().getClassLoader().getResourceAsStream("org/granite/client/test/javafx/granite-config-hibernate.xml");
		graniteConfigHibernate = new GraniteConfig(null, is, null, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("test", 89L);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(map);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigJavaFX.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Map type", entity instanceof Map);
		Assert.assertEquals("Map value", new Long(89), ((Map<String, Long>)entity).get("test"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationMap2() throws Exception {
		Map<Integer, Long> map = new HashMap<Integer, Long>();
		map.put(34, 89L);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(map);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), ClientType.JAVA.toString());
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigJavaFX.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Map type", entity instanceof Map);
		Assert.assertEquals("Map value", new Long(89), ((Map<Integer, Long>)entity).get(34));
	}
}
