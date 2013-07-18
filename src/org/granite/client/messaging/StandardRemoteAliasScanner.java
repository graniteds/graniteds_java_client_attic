package org.granite.client.messaging;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.granite.logging.Logger;
import org.granite.scan.ScannedItem;
import org.granite.scan.ScannedItemHandler;
import org.granite.scan.Scanner;
import org.granite.scan.ScannerFactory;

public class StandardRemoteAliasScanner implements RemoteAliasScanner {
	
	private static final Logger log = Logger.getLogger(StandardRemoteAliasScanner.class);

	@Override
	public Set<Class<?>> scan(Set<String> packageNames) {
		Set<Class<?>> classes = new HashSet<Class<?>>();
		
		Scanner scanner = ScannerFactory.createScanner(new MessagingScannedItemHandler(packageNames, classes), null);
        try {
            scanner.scan();
        }
        catch (Exception e) {
            log.error(e, "Could not scan classpath for @RemoteAlias");
        }
		
        return classes;
	}
	
	class MessagingScannedItemHandler implements ScannedItemHandler {

		final String[] packageNames;
		final Set<Class<?>> classes;
		
		MessagingScannedItemHandler(Set<String> packageNames, Set<Class<?>> classes) {
			this.packageNames = new String[packageNames.size()];
			int i = 0;
			for (String packageName : packageNames)
				this.packageNames[i++] = packageName.replace('.', '/') + '/';
			
			this.classes = classes;
		}
		
		@Override
		public boolean handleMarkerItem(ScannedItem item) {
			return false;
		}

		@Override
		public void handleScannedItem(ScannedItem item) {
			if ("class".equals(item.getExtension())) {
				boolean scan = false;
				
				String path = item.getRelativePath();
				for (String packageName : packageNames) {
					if (path.startsWith(packageName)) {
						scan = true;
						break;
					}
				}
				
				if (scan) {
					try {
						Class<?> cls = item.loadAsClass();
						RemoteAlias alias = cls.getAnnotation(RemoteAlias.class);
						if (alias != null)
							classes.add(cls);
					}
					catch (ClassFormatError e) {
					}
					catch (ClassNotFoundException e) {
					}
					catch (IOException e) {
						log.error(e, "Could not load class: %s", item);
					}
				}
			}
		}
	}
}
