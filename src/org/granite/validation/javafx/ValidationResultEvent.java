package org.granite.validation.javafx;

import java.util.List;

import org.granite.validation.ValidationResult;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;


public class ValidationResultEvent extends Event {

	private static final long serialVersionUID = 1L;
	
	public static EventType<ValidationResultEvent> ANY = new EventType<ValidationResultEvent>(Event.ANY);
	public static EventType<ValidationResultEvent> VALID = new EventType<ValidationResultEvent>(ANY, "valid");
	public static EventType<ValidationResultEvent> INVALID = new EventType<ValidationResultEvent>(ANY, "invalid");
	public static EventType<ValidationResultEvent> UNHANDLED = new EventType<ValidationResultEvent>(ANY, "unhandled");
	

	private final List<ValidationResult> errorResults;
	
	public ValidationResultEvent(Object source, EventTarget target, EventType<ValidationResultEvent> type, List<ValidationResult> errorResults) {
		super(source, target, type);
		this.errorResults = errorResults;
	}

	public List<ValidationResult> getErrorResults() {
		return errorResults;
	}
}
