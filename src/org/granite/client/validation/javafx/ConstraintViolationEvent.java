package org.granite.client.validation.javafx;

import java.util.Set;

import javafx.event.Event;
import javafx.event.EventType;

import javax.validation.ConstraintViolation;


public class ConstraintViolationEvent extends Event {

	private static final long serialVersionUID = 1L;
	
	public static EventType<ConstraintViolationEvent> ANY = new EventType<ConstraintViolationEvent>(EventType.ROOT);
	public static EventType<ConstraintViolationEvent> CONSTRAINT_VIOLATION = new EventType<ConstraintViolationEvent>(ANY, "constraintViolation");

	private final Set<ConstraintViolation<?>> violations;
	
	public ConstraintViolationEvent(EventType<? extends ConstraintViolationEvent> type, Set<ConstraintViolation<?>> violations) {
		super(type);
		this.violations = violations;
	}

	public Set<ConstraintViolation<?>> getViolations() {
		return violations;
	}
}
