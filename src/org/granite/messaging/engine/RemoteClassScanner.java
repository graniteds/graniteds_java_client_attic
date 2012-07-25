package org.granite.messaging.engine;

import java.util.HashSet;
import java.util.Set;

import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;
import net.sf.extcos.spi.ResourceAccessor;
import net.sf.extcos.spi.ResourceType;

import org.granite.config.GraniteConfig;
import org.granite.messaging.amf.RemoteClass;


public class RemoteClassScanner implements Engine.Configurator {
	
	private Set<String> packageNames = new HashSet<String>();
	
	public void addPackageName(String packageName) {
		this.packageNames.add(packageName);
	}
	
	public void setPackageNames(Set<String> packageNames) {
		this.packageNames.clear();
		this.packageNames.addAll(packageNames);
	}
	
	public void configure(GraniteConfig graniteConfig) {
		ComponentScanner scanner = new ComponentScanner();
		
		final String[] basePackageNames = packageNames.toArray(new String[packageNames.size()]);
		
		for (Class<?> remoteClass : scanner.getClasses(new ComponentQuery() {
			@Override
			protected void query() {
				select(JavaClassResourceType.javaClasses()).from(basePackageNames).returning(allAnnotatedWith(RemoteClass.class));
			}
		})) {
			graniteConfig.registerClassAlias(remoteClass);
		}
	}	
	
	public static class JavaClassResourceType implements ResourceType {
		private static final String JAVA_CLASS_SUFFIX = "class";
		private static JavaClassResourceType instance;

		/**
		 * Always instantiate via the {@link #javaClasses()} method.
		 */
		private JavaClassResourceType() {
		}

		@Override
		public String getFileSuffix() {
			return JAVA_CLASS_SUFFIX;
		}

		/**
		 * EDSL method
		 */
		public static JavaClassResourceType javaClasses() {
			if (instance == null)
				instance = new JavaClassResourceType();

			return instance;
		}

		@Override
		public ResourceAccessor getResourceAccessor() {
			return new JavaResourceAccessor();
		}
	}
}
