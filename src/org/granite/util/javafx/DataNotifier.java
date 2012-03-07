package org.granite.util.javafx;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;


public interface DataNotifier extends EventTarget {

	public <T extends Event> void addEventHandler(EventType<T> type, EventHandler<? super T> handler);
	
	public <T extends Event> void removeEventHandler(EventType<T> type, EventHandler<? super T> handler);
	
}
