package org.granite.client.test.javafx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

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
		servicesConfig = new ServicesConfig(null, null, false);
		InputStream is = getClass().getClassLoader().getResourceAsStream("org/granite/client/test/javafx/granite-config-javafx.xml");
		graniteConfigJavaFX = new GraniteConfig(null, is, null, null);
		graniteConfigJavaFX.registerClassAlias(FXEntity1.class);
		graniteConfigJavaFX.registerClassAlias(FXEntity2.class);
		graniteConfigJavaFX.registerClassAlias(FXEntity1b.class);
		graniteConfigJavaFX.registerClassAlias(FXEntity2b.class);
		graniteConfigJavaFX.registerClassAlias(FXEntity1c.class);
		graniteConfigJavaFX.registerClassAlias(FXEntity2c.class);
		graniteConfigJavaFX.registerClassAlias(org.granite.client.persistence.javafx.PersistentList.class);
		graniteConfigJavaFX.registerClassAlias(org.granite.client.persistence.javafx.PersistentSet.class);
		graniteConfigJavaFX.registerClassAlias(org.granite.client.persistence.javafx.PersistentMap.class);
		is = getClass().getClassLoader().getResourceAsStream("org/granite/client/test/javafx/granite-config-hibernate.xml");
		graniteConfigHibernate = new GraniteConfig(null, is, null, null);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExternalizationMap() throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("test", 89L);
		
		SimpleGraniteContext.createThreadInstance(graniteConfigHibernate, servicesConfig, new HashMap<String, Object>(), "java");
		ByteArrayOutputStream baos = new ByteArrayOutputStream(20000);
		ObjectOutput out = graniteConfigHibernate.newAMF3Serializer(baos);
		out.writeObject(map);
		
		byte[] buf = baos.toByteArray();
		
		SimpleGraniteContext.createThreadInstance(graniteConfigJavaFX, servicesConfig, new HashMap<String, Object>(), "java");
		ByteArrayInputStream bais = new ByteArrayInputStream(buf);
		ObjectInput in = graniteConfigJavaFX.newAMF3Deserializer(bais);
		Object entity = in.readObject();
		
		Assert.assertTrue("Map type", entity instanceof Map);
		Assert.assertEquals("Map value", new Long(89), ((Map<String, Long>)entity).get("test"));
	}
}
