/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

package org.granite.client.platform;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.granite.client.configuration.Configuration;
import org.granite.client.configuration.DefaultConfiguration;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.apache.ApacheAsyncTransport;
import org.granite.client.persistence.Persistence;
import org.granite.messaging.jmf.reflect.Reflection;

/**
 * @author Franck WOLFF
 */
public class Platform {

	public static final String SYSTEM_PROPERTY_KEY = Platform.class.getName();
	
	protected static Platform instance = null;

	protected final Reflection reflection;
	protected final Persistence persistence;
	
	public static synchronized Platform getInstance() {
		
		if (instance == null) {
			String platformClassName = System.getProperty(SYSTEM_PROPERTY_KEY);
			
			if (platformClassName == null)
				initInstance((ClassLoader)null);
			
			if (instance == null)
				initInstance(platformClassName, null);
		}
		
		return instance;
	}
	
	public static synchronized Platform initInstance(ClassLoader platformClassLoader) {
		
		if (instance != null)
			throw new IllegalStateException("Platform already loaded");
		
		if (platformClassLoader == null)
			platformClassLoader = Thread.currentThread().getContextClassLoader();
		
		ServiceLoader<Platform> platformLoader = ServiceLoader.load(Platform.class, platformClassLoader);
		
		Iterator<Platform> platforms = null;
		try {
			platforms = platformLoader.iterator();
			
			if (platforms.hasNext())
				instance = platforms.next();
			
			if (platforms.hasNext())
				throw new PlatformConfigurationError("Multiple Platform services: " + instance + " / " + platforms.next());
		}
		catch (PlatformConfigurationError e) {
			throw e;
		}
		catch (Throwable t) {
			throw new PlatformConfigurationError("Could not load Platform service", t);
		}
		
		platformLoader.reload();
		
		return instance;
	}

	public static synchronized Platform initInstance(String platformClassName) {
		return initInstance(platformClassName, null);
	}

	public static synchronized Platform initInstance(String platformClassName, ClassLoader platformClassLoader) {
		return initInstance(platformClassName, platformClassLoader, null);
	}
	
	public static synchronized Platform initInstance(String platformClassName, ClassLoader platformClassLoader, ClassLoader reflectionClassLoader) {
		
		if (instance != null)
			throw new IllegalStateException("Platform already loaded");
		
		if (platformClassName == null)
			platformClassName = Platform.class.getName();
		if (platformClassLoader == null)
			platformClassLoader = Thread.currentThread().getContextClassLoader();
		
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Platform> platformClass = (Class<? extends Platform>)platformClassLoader.loadClass(platformClassName);
			instance = platformClass.getConstructor(ClassLoader.class).newInstance(reflectionClassLoader);
		}
		catch (Throwable t) {
			throw new PlatformConfigurationError("Could not create new Platform of type: " + platformClassName, t);
		}
		
		return instance;
	}
	
	public Platform() {
		this(new Reflection(null));
	}
	
	public Platform(ClassLoader reflectionClassLoader) {
		this(new Reflection(reflectionClassLoader));
	}
	
	public Platform(Reflection reflection) {
		if (reflection == null)
			throw new NullPointerException("reflection cannot be null");
		
		this.reflection = reflection;
		this.persistence = new Persistence(reflection);
	}
	
	public Reflection getReflection() {
		return reflection;
	}

	public Configuration newConfiguration() {
		return new DefaultConfiguration();
	}
	
	public Transport newRemotingTransport(Object context) {
		return new ApacheAsyncTransport();
	}
	
	public Transport newMessagingTransport(Object context) {
		return null;
	}

	public Persistence getPersistence() {
		return persistence;
	}

	public static Reflection reflection() {
		return getInstance().reflection;
	}
	
	public static Persistence persistence() {
		return getInstance().persistence;
	}
}
